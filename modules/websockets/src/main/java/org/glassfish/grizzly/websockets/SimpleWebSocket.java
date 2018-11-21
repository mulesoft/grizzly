/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.grizzly.websockets;

import static org.glassfish.grizzly.utils.Futures.completable;
import static org.glassfish.grizzly.websockets.Utils.completedFrame;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.websockets.frametypes.PingFrameType;
import org.glassfish.grizzly.websockets.frametypes.PongFrameType;

/**
 * Generic WebSocket implementation.  Typically used by client implementations.
 */
public class SimpleWebSocket implements WebSocket {

  protected final Queue<WebSocketListener> listeners = new ConcurrentLinkedQueue<>();

  protected final ProtocolHandler protocolHandler;

  protected Broadcaster broadcaster = new DummyBroadcaster();

  protected enum State {
    NEW, CONNECTED, CLOSING, CLOSED
  }

  protected final EnumSet<State> connected = EnumSet.range(State.CONNECTED, State.CLOSING);
  protected final AtomicReference<State> state = new AtomicReference<>(State.NEW);


  public SimpleWebSocket(final ProtocolHandler protocolHandler,
                         final WebSocketListener... listeners) {

    this.protocolHandler = protocolHandler;
    for (WebSocketListener listener : listeners) {
      add(listener);
    }

    protocolHandler.setWebSocket(this);
  }

  public Collection<WebSocketListener> getListeners() {
    return listeners;
  }

  @Override
  public final boolean add(WebSocketListener listener) {
    return listeners.add(listener);
  }

  @Override
  public final boolean remove(WebSocketListener listener) {
    return listeners.remove(listener);
  }

  @Override
  public boolean isConnected() {
    return connected.contains(state.get());
  }

  public void setClosed() {
    state.set(State.CLOSED);
  }

  @Override
  public void onClose(final DataFrame frame) {
    if (state.compareAndSet(State.CONNECTED, State.CLOSING)) {
      final ClosingFrame closing = (ClosingFrame) frame;
      protocolHandler.close(closing.getCode(), closing.getTextPayload());
    } else {
      state.set(State.CLOSED);
      protocolHandler.doClose();
    }

    WebSocketListener listener;
    while ((listener = listeners.poll()) != null) {
      listener.onClose(this, frame);
    }
  }

  @Override
  public void onConnect() {
    state.set(State.CONNECTED);

    for (WebSocketListener listener : listeners) {
      listener.onConnect(this);
    }
  }

  @Override
  public void onFragment(boolean last, byte[] fragment) {
    for (WebSocketListener listener : listeners) {
      listener.onFragment(this, fragment, last);
    }
  }

  @Override
  public void onFragment(boolean last, String fragment) {
    for (WebSocketListener listener : listeners) {
      listener.onFragment(this, fragment, last);
    }
  }

  @Override
  public void onMessage(byte[] data) {
    for (WebSocketListener listener : listeners) {
      listener.onMessage(this, data);
    }
  }

  @Override
  public void onMessage(String text) {
    for (WebSocketListener listener : listeners) {
      listener.onMessage(this, text);
    }
  }

  @Override
  public void onPing(DataFrame frame) {
    send(new DataFrame(new PongFrameType(), frame.getBytes()));
    for (WebSocketListener listener : listeners) {
      listener.onPing(this, frame.getBytes());
    }
  }

  @Override
  public void onPong(DataFrame frame) {
    for (WebSocketListener listener : listeners) {
      listener.onPong(this, frame.getBytes());
    }
  }

  @Override
  public void close() {
    close(NORMAL_CLOSURE, null);
  }

  @Override
  public void close(int code) {
    close(code, null);
  }

  @Override
  public void close(int code, String reason) {
    if (state.compareAndSet(State.CONNECTED, State.CLOSING)) {
      protocolHandler.close(code, reason);
    }
  }

  @Override
  public CompletableFuture<DataFrame> completableClose(int code, String reason) {
    if (state.compareAndSet(State.CONNECTED, State.CLOSING)) {
      return completable(protocolHandler.close(code, reason));
    }

    return completedFrame(new byte[0], true);
  }

  @Override
  public GrizzlyFuture<DataFrame> send(byte[] data) {
    if (isConnected()) {
      return protocolHandler.send(data);
    } else {
      throw new RuntimeException("Socket is not connected.");
    }
  }

  @Override
  public GrizzlyFuture<DataFrame> send(String data) {
    if (isConnected()) {
      return protocolHandler.send(data);
    } else {
      throw new RuntimeException("Socket is not connected.");
    }
  }

  @Override
  public void broadcast(Iterable<? extends WebSocket> recipients,
                        String data) {
    if (state.get() == State.CONNECTED) {
      broadcaster.broadcast(recipients, data);
    } else {
      throw new RuntimeException("Socket is already closed.");
    }
  }

  @Override
  public void broadcast(Iterable<? extends WebSocket> recipients,
                        byte[] data) {
    if (state.get() == State.CONNECTED) {
      broadcaster.broadcast(recipients, data);
    } else {
      throw new RuntimeException("Socket is already closed.");
    }
  }

  @Override
  public void broadcastFragment(Iterable<? extends WebSocket> recipients,
                                String data, boolean last) {
    if (state.get() == State.CONNECTED) {
      broadcaster.broadcastFragment(recipients, data, last);
    } else {
      throw new RuntimeException("Socket is already closed.");
    }
  }

  @Override
  public void broadcastFragment(Iterable<? extends WebSocket> recipients,
                                byte[] data, boolean last) {
    if (state.get() == State.CONNECTED) {
      broadcaster.broadcastFragment(recipients, data, last);
    } else {
      throw new RuntimeException("Socket is already closed.");
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public GrizzlyFuture<DataFrame> sendPing(byte[] data) {
    return send(new DataFrame(new PingFrameType(), data));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GrizzlyFuture<DataFrame> sendPong(byte[] data) {
    return send(new DataFrame(new PongFrameType(), data));
  }

  private GrizzlyFuture<DataFrame> send(DataFrame frame) {
    if (isConnected()) {
      return protocolHandler.send(frame);
    } else {
      throw new RuntimeException("Socket is not connected.");
    }
  }

  @Override
  public GrizzlyFuture<DataFrame> stream(boolean last, String fragment) {
    if (isConnected()) {
      return protocolHandler.stream(last, fragment);
    } else {
      throw new RuntimeException("Socket is not connected.");
    }
  }

  @Override
  public GrizzlyFuture<DataFrame> stream(boolean last, byte[] bytes, int off, int len) {
    if (isConnected()) {
      return protocolHandler.stream(last, bytes, off, len);
    } else {
      throw new RuntimeException("Socket is not connected.");
    }
  }

  public byte[] toRawData(String text) {
    return toRawData(text, true);
  }

  public byte[] toRawData(byte[] binary) {
    return toRawData(binary, true);
  }

  public byte[] toRawData(String fragment, boolean last) {
    final DataFrame dataFrame = protocolHandler.toDataFrame(fragment, last);
    return protocolHandler.frame(dataFrame);
  }

  public byte[] toRawData(byte[] binary, boolean last) {
    final DataFrame dataFrame = protocolHandler.toDataFrame(binary, last);
    return protocolHandler.frame(dataFrame);
  }

  @SuppressWarnings("unchecked")
  public GrizzlyFuture<WriteResult> sendRaw(byte[] rawData) {
    final Connection connection = protocolHandler.getConnection();
    final MemoryManager mm = connection.getTransport().getMemoryManager();
    final Buffer buffer = Buffers.wrap(mm, rawData);
    buffer.allowBufferDispose(false);

    return connection.write(buffer);
  }

  protected Broadcaster getBroadcaster() {
    return broadcaster;
  }

  protected void setBroadcaster(Broadcaster broadcaster) {
    this.broadcaster = broadcaster;
  }
}
