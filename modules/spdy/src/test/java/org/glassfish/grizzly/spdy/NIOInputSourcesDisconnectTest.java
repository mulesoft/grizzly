/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.spdy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.io.NIOInputStream;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.Exceptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case to exercise <code>AsyncStreamReader</code>.
 */
@RunWith(Parameterized.class)
public class NIOInputSourcesDisconnectTest extends AbstractSpdyTest {

    private static final char[] ALPHA = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int PORT = 18302;

    private final SpdyVersion spdyVersion;
    private final SpdyMode spdyMode;
    private final boolean isSecure;

    public NIOInputSourcesDisconnectTest(final SpdyVersion spdyVersion,
                                         final SpdyMode spdyMode,
                                         final boolean isSecure) {
        this.spdyVersion = spdyVersion;
        this.spdyMode = spdyMode;
        this.isSecure = isSecure;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getSpdyModes() {
        return AbstractSpdyTest.getSpdyModes();
    }

    @Before
    public void setUp() {
        ByteBufferWrapper.DEBUG_MODE = true;
    }

    // ------------------------------------------------------------ Test Methods

    /**
     * Test ReadHandler.onError to be notified, when client unexpectedly
     * terminates the connection
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void testDisconnect() throws Throwable {

        final AtomicInteger bytesRead = new AtomicInteger();
        final FutureImpl<Integer> resultFuture = SafeFutureImpl.create();

        final TCPNIOTransport clientTransport = TCPNIOTransportBuilder.newInstance().build();
        clientTransport.setProcessor(
                createClientFilterChain(spdyVersion, spdyMode, isSecure));

        final HttpHandler httpHandler = new HttpHandler() {

            @Override
            public void service(final Request request,
                                final Response response) throws Exception {
                response.suspend();
                final NIOInputStream inputStream = (NIOInputStream) request.getInputStream();

                inputStream.notifyAvailable(new ReadHandler() {

                    @Override
                    public void onDataAvailable() throws IOException {
                        final int readyData = inputStream.readyData();
                        inputStream.skip(readyData);
                        bytesRead.addAndGet(readyData);

                        inputStream.notifyAvailable(this);
                    }


                    @Override
                    public void onAllDataRead() throws IOException {
                        final int readyData = inputStream.readyData();
                        inputStream.skip(readyData);
                        bytesRead.addAndGet(readyData);
                        resultFuture.failure(new IllegalStateException("Connection should have been terminated"));

                        response.resume();
                    }

                    @Override
                    public void onError(Throwable t) {
                        resultFuture.failure(t);

                        response.resume();
                    }
                });
            }

        };

        final HttpServer server = createWebServer(httpHandler);

        try {
            server.start();
            clientTransport.start();

            Future<Connection> connectFuture = clientTransport.connect("localhost", PORT);
            Connection connection = null;
            try {
                connection = connectFuture.get(10, TimeUnit.SECONDS);
                HttpRequestPacket packet = (HttpRequestPacket) createRequest(PORT, "POST", null, null);
                packet.setContentLength(5000);
                connection.write(packet);

                HttpContent content = HttpContent.builder(packet).content(
                        Buffers.wrap(null, buildString(2500))).build();

                connection.write(content, new EmptyCompletionHandler<WriteResult>() {

                    @Override
                    public void completed(WriteResult result) {
                        result.getConnection().closeSilently();
                    }
                });

                try {
                    final Integer i = resultFuture.get(10, TimeUnit.SECONDS);
                    fail("Wrapped EOFException expected");
                } catch (ExecutionException e) {
                    assertEquals("NOT EOF Exception:\n" +
                                    Exceptions.getStackTraceAsString(e.getCause()),
                            EOFException.class, e.getCause().getClass());
                }
            } finally {
                // Close the client connection
                if (connection != null) {
                    connection.closeSilently();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } finally {
            clientTransport.shutdownNow();
            server.shutdownNow();
        }
    }

    // --------------------------------------------------------- Private Methods


    private HttpServer createWebServer(final HttpHandler httpHandler) {
        final HttpServer httpServer = createServer(null, PORT, spdyVersion,
                spdyMode, isSecure,
                HttpHandlerRegistration.of(httpHandler, "/path/*"));

        final NetworkListener listener = httpServer.getListener("grizzly");
        listener.getKeepAlive().setIdleTimeoutInSeconds(-1);

        return httpServer;

    }

    private String buildString(int len) {

        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0, j = 0; i < len; i++, j++) {
            if (j > 25) {
                j = 0;
            }
            sb.append(ALPHA[j]);
        }
        return sb.toString();

    }
}
