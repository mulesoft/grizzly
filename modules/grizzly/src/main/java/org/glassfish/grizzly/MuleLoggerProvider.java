/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.glassfish.grizzly;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

import java.util.function.Supplier;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

class MuleLoggerProvider {

    private static final String DISABLE_LOG_SEPARATION_PROPERTY = "mule.disableLogSeparation";
    private static final String USE_SLF4J_PROPERTY = "mule.grizzly.useSLF4J";

    private static final boolean isMuleLogSeparationEnabled = getProperty(DISABLE_LOG_SEPARATION_PROPERTY) == null;

    // When log separation is enabled, the isLogEnabled() methods are expensive, and then we would be adding a
    // performance degradation. Then, by default we will use JUL if log separation is enabled, and SLF4J otherwise.
    //
    // If the user still wants to use SLF4J with log separation enabled, or use JUL with log separation disabled,
    // then they have to set the property mule.grizzly.useSLF4J
    private static final boolean useSlf4j = parseBoolean(getProperty(USE_SLF4J_PROPERTY, isMuleLogSeparationEnabled ? "false" : "true"));

    public static Logger getLogger(String name) {
        return useSlf4j ? LoggerFactory.getLogger(name) : new JulAsSlf4jAdapter(name);
    }

    private static class JulAsSlf4jAdapter implements org.slf4j.Logger {

        private final java.util.logging.Logger julDelegate;

        public JulAsSlf4jAdapter(String name) {
            julDelegate = java.util.logging.Logger.getLogger(name);
        }

        @Override
        public String getName() {
            return julDelegate.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return julDelegate.isLoggable(Level.FINEST);
        }

        @Override
        public void trace(String msg) {
            julDelegate.log(Level.FINEST, msg);
        }

        @Override
        public void trace(String format, Object arg) {
            julDelegate.log(Level.FINEST, new MessageSupplier(format, arg));
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            julDelegate.log(Level.FINEST, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void trace(String format, Object... arguments) {
            julDelegate.log(Level.FINEST, new MessageSupplier(format, arguments));
        }

        @Override
        public void trace(String msg, Throwable t) {
            julDelegate.log(Level.FINEST, msg, t);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return julDelegate.isLoggable(Level.FINEST);
        }

        @Override
        public void trace(Marker marker, String msg) {
            julDelegate.log(Level.FINEST, msg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg) {
            julDelegate.log(Level.FINEST, new MessageSupplier(format, arg));
        }

        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2) {
            julDelegate.log(Level.FINEST, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void trace(Marker marker, String format, Object... arguments) {
            julDelegate.log(Level.FINEST, new MessageSupplier(format, arguments));
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
            julDelegate.log(Level.FINEST, msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return julDelegate.isLoggable(Level.FINE);
        }

        @Override
        public void debug(String msg) {
            julDelegate.log(Level.FINE, msg);
        }

        @Override
        public void debug(String format, Object arg) {
            julDelegate.log(Level.FINE, new MessageSupplier(format, arg));
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            julDelegate.log(Level.FINE, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void debug(String format, Object... arguments) {
            julDelegate.log(Level.FINE, new MessageSupplier(format, arguments));
        }

        @Override
        public void debug(String msg, Throwable t) {
            julDelegate.log(Level.FINE, msg, t);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return julDelegate.isLoggable(Level.FINE);
        }

        @Override
        public void debug(Marker marker, String msg) {
            julDelegate.log(Level.FINE, msg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg) {
            julDelegate.log(Level.FINE, new MessageSupplier(format, arg));
        }

        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2) {
            julDelegate.log(Level.FINE, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void debug(Marker marker, String format, Object... arguments) {
            julDelegate.log(Level.FINE, new MessageSupplier(format, arguments));
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            julDelegate.log(Level.FINE, msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return julDelegate.isLoggable(Level.INFO);
        }

        @Override
        public void info(String msg) {
            julDelegate.log(Level.INFO, msg);
        }

        @Override
        public void info(String format, Object arg) {
            julDelegate.log(Level.INFO, new MessageSupplier(format, arg));
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            julDelegate.log(Level.INFO, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void info(String format, Object... arguments) {
            julDelegate.log(Level.INFO, new MessageSupplier(format, arguments));
        }

        @Override
        public void info(String msg, Throwable t) {
            julDelegate.log(Level.INFO, msg, t);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return julDelegate.isLoggable(Level.INFO);
        }

        @Override
        public void info(Marker marker, String msg) {
            julDelegate.log(Level.INFO, msg);
        }

        @Override
        public void info(Marker marker, String format, Object arg) {
            julDelegate.log(Level.INFO, new MessageSupplier(format, arg));
        }

        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2) {
            julDelegate.log(Level.INFO, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void info(Marker marker, String format, Object... arguments) {
            julDelegate.log(Level.INFO, new MessageSupplier(format, arguments));
        }

        @Override
        public void info(Marker marker, String msg, Throwable t) {
            julDelegate.log(Level.INFO, msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return julDelegate.isLoggable(Level.WARNING);
        }

        @Override
        public void warn(String msg) {
            julDelegate.log(Level.WARNING, msg);
        }

        @Override
        public void warn(String format, Object arg) {
            julDelegate.log(Level.WARNING, new MessageSupplier(format, arg));
        }

        @Override
        public void warn(String format, Object... arguments) {
            julDelegate.log(Level.WARNING, new MessageSupplier(format, arguments));
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            julDelegate.log(Level.WARNING, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void warn(String msg, Throwable t) {
            julDelegate.log(Level.WARNING, msg, t);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return julDelegate.isLoggable(Level.WARNING);
        }

        @Override
        public void warn(Marker marker, String msg) {
            julDelegate.log(Level.WARNING, msg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg) {
            julDelegate.log(Level.WARNING, new MessageSupplier(format, arg));
        }

        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2) {
            julDelegate.log(Level.WARNING, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void warn(Marker marker, String format, Object... arguments) {
            julDelegate.log(Level.WARNING, new MessageSupplier(format, arguments));
        }

        @Override
        public void warn(Marker marker, String msg, Throwable t) {
            julDelegate.log(Level.WARNING, msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return julDelegate.isLoggable(Level.SEVERE);
        }

        @Override
        public void error(String msg) {
            julDelegate.log(Level.SEVERE, msg);
        }

        @Override
        public void error(String format, Object arg) {
            julDelegate.log(Level.SEVERE, new MessageSupplier(format, arg));
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            julDelegate.log(Level.SEVERE, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void error(String format, Object... arguments) {
            julDelegate.log(Level.SEVERE, new MessageSupplier(format, arguments));
        }

        @Override
        public void error(String msg, Throwable t) {
            julDelegate.log(Level.SEVERE, msg, t);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return julDelegate.isLoggable(Level.SEVERE);
        }

        @Override
        public void error(Marker marker, String msg) {
            julDelegate.log(Level.SEVERE, msg);
        }

        @Override
        public void error(Marker marker, String format, Object arg) {
            julDelegate.log(Level.SEVERE, new MessageSupplier(format, arg));
        }

        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2) {
            julDelegate.log(Level.SEVERE, new MessageSupplier(format, arg1, arg2));
        }

        @Override
        public void error(Marker marker, String format, Object... arguments) {
            julDelegate.log(Level.SEVERE, new MessageSupplier(format, arguments));
        }

        @Override
        public void error(Marker marker, String msg, Throwable t) {
            julDelegate.log(Level.SEVERE, msg, t);
        }
    }

    private static class MessageSupplier implements Supplier<String> {

        private final Supplier<String> delegate;

        public MessageSupplier(String format, Object arg) {
            delegate = new Supplier<String>() {

                @Override
                public String get() {
                    return MessageFormatter.format(format, arg).getMessage();
                }
            };
        }

        public MessageSupplier(String format, Object... arguments) {
            delegate = new Supplier<String>() {

                @Override
                public String get() {
                    return MessageFormatter.arrayFormat(format, arguments).getMessage();
                }
            };
        }

        public MessageSupplier(String format, Object arg1, Object arg2) {
            delegate = new Supplier<String>() {

                @Override
                public String get() {
                    return MessageFormatter.format(format, arg1, arg2).getMessage();
                }
            };
        }

        @Override
        public String get() {
            return delegate.get();
        }
    }
}
