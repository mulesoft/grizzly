/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.grizzly.threadpool;

import java.util.concurrent.TimeUnit;
import com.sun.grizzly.ThreadCache;
import com.sun.grizzly.ThreadCache.ObjectCache;
import com.sun.grizzly.attributes.AttributeBuilder;
import com.sun.grizzly.attributes.AttributeHolder;
import com.sun.grizzly.attributes.IndexedAttributeHolder;
import com.sun.grizzly.memory.DefaultMemoryManager.ThreadLocalPool;

/**
 * Default Grizzly worker thread implementation
 * 
 * @author Alexey Stashok
 */
public class DefaultWorkerThread extends Thread implements WorkerThread {

    private final AttributeBuilder attrBuilder;
    private final AttributeHolder attributes;

    private final ThreadLocalPool memoryPool = new ThreadLocalPool();
    
    private final ObjectCache objectCache = new ObjectCache();
    
    private long transactionTimeoutMillis =
            WorkerThread.UNLIMITED_TRANSACTION_TIMEOUT;

    private long transactionStartedTimeMillis;

    private boolean isSelectorThread;

    public DefaultWorkerThread(AttributeBuilder attrBuilder, String name,
            Runnable runTask) {
        super(runTask, name);
        this.attrBuilder = attrBuilder;
        attributes = createAttributeHolder();
    }

    @Override
    public Thread getThread() {
        return this;
    }

    @Override
    public AttributeHolder getAttributes() {
        return attributes;
    }

    public ThreadLocalPool getMemoryPool() {
        return memoryPool;
    }

    public final <E> E takeFromCache(ThreadCache.CachedTypeIndex<E> index) {
        return objectCache.get(index);
    }

    public final <E> void putToCache(ThreadCache.CachedTypeIndex<E> index, E o) {
        objectCache.put(index, o);
    }

    @Override
    public long getTransactionTimeout(TimeUnit timeunit) {
        return timeunit.convert(transactionTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setTransactionTimeout(long timeout, TimeUnit timeunit) {
        this.transactionTimeoutMillis =
                TimeUnit.MILLISECONDS.convert(timeout, timeunit);
    }

    public long getTransactionStartedTime(TimeUnit timeunit) {
        return timeunit.convert(transactionTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    protected void onBeforeRun() {
        transactionStartedTimeMillis = System.currentTimeMillis();
    }
    
    protected void onAfterRun() {
        transactionStartedTimeMillis = 0;
    }
    
    protected AttributeHolder createAttributeHolder() {
        return new IndexedAttributeHolder(attrBuilder);
    }

    @Override
    public boolean isSelectorThread() {
        return isSelectorThread;
    }

    @Override
    public void setSelectorThread(boolean isSelectorThread) {
        this.isSelectorThread = isSelectorThread;
    }
}
