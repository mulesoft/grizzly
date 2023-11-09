/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.glassfish.grizzly.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility class useful for find a dynamic port in test cases.
 * Use {@link #findFreePort()} for that purpose.
 */
public class FreePortFinder {

    /**
     * Finds a port number safe to be bound without getting an "AddressAlreadyInUse" error.
     * @return the free port number.
     */
    public static int findFreePort() {
        try {
            ServerSocket dummySocket = new ServerSocket(0);
            int freePort = dummySocket.getLocalPort();
            dummySocket.setReuseAddress(true);
            dummySocket.close();
            return freePort;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
