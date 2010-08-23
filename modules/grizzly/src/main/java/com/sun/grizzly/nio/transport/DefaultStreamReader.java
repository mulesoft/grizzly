/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.grizzly.nio.transport;

import java.io.IOException;
import com.sun.grizzly.Buffer;
import com.sun.grizzly.CompletionHandler;
import com.sun.grizzly.Connection;
import com.sun.grizzly.Interceptor;
import com.sun.grizzly.ReadResult;
import com.sun.grizzly.Reader;
import com.sun.grizzly.Transport;
import com.sun.grizzly.streams.AbstractStreamReader;
import com.sun.grizzly.streams.BufferedInput;

/**
 *
 * @author Alexey Stashok
 */
public final class DefaultStreamReader extends AbstractStreamReader {

    public DefaultStreamReader(Connection connection) {
        super(connection, new Input());
        ((Input) input).parentStreamReader = this;
    }

    public Input getSource() {
        return (Input) input;
    }

    public static final class Input extends BufferedInput {

        private DefaultStreamReader parentStreamReader;
        private boolean isDone;

        @Override
        protected void onOpenInputSource() throws IOException {
            isDone = false;
            final Connection connection = parentStreamReader.getConnection();
            final Transport transport = connection.getTransport();
            final Reader reader = transport.getReader(connection);

            reader.read(connection, null, null,
                    new Interceptor() {

                        @Override
                        public int intercept(int event, Object context, Object result) {
                            if (event == Reader.READ_EVENT) {
                                final ReadResult readResult = (ReadResult) result;
                                final Buffer buffer = (Buffer) readResult.getMessage();
                                readResult.setMessage(null);

                                if (buffer == null) {
                                    return Interceptor.INCOMPLETED;
                                }

                                buffer.trim();
                                append(buffer);
                                if (isDone) {
                                    return Interceptor.COMPLETED;
                                }

                                return Interceptor.INCOMPLETED
                                        | Interceptor.RESET;
                            }

                            return Interceptor.DEFAULT;
                        }
                    });
        }

        @Override
        protected void onCloseInputSource() throws IOException {
            isDone = true;
        }

        @Override
        protected void notifyCompleted(final CompletionHandler<Integer> completionHandler) {
            if (completionHandler != null) {
                completionHandler.completed(compositeBuffer.remaining());
            }
        }

        @Override
        protected void notifyFailure(final CompletionHandler<Integer> completionHandler,
                final Throwable failure) {
            if (completionHandler != null) {
                completionHandler.failed(failure);
            }
        }
    }
}
