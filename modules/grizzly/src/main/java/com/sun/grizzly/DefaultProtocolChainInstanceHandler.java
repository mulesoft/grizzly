/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
 *
 */

package com.sun.grizzly;

import com.sun.grizzly.util.DataStructures;
import java.util.Queue;

/**
 * Default implementation of an ProtocolChainInstanceHandler. 
 * {@link ProtocolChain} are cached using a ConcurrentLinkedQueue. When
 * the queue becomes empty, a new instance of {@link ProtocolChain}
 * is created.
 *
 * @author Jeanfrancois Arcand
 */
public class DefaultProtocolChainInstanceHandler 
            implements ProtocolChainInstanceHandler{
    

    /**
     * List used to cache instance of ProtocolChain.
     */
    protected final Queue<ProtocolChain> protocolChains;
        
    
    public DefaultProtocolChainInstanceHandler() {
        protocolChains = DataStructures.getCLQinstance(ProtocolChain.class);
    }

    
    /**
     * Return a pooled instance of ProtocolChain. If the pool is empty,
     * a new instance of ProtocolChain will be returned.
     * @return <tt>ProtocolChain</tt>
     */
    public ProtocolChain poll() {
        ProtocolChain protocolChain = protocolChains.poll();
        if (protocolChain == null){
            protocolChain = new DefaultProtocolChain();
        }
        return protocolChain;       
    }

    
    /**
     * Offer (add) an instance of ProtocolChain to this instance pool.
     * @param protocolChain - <tt>ProtocolChain</tt> to offer / add to the pool
     * @return boolean, if <tt>ProtocolChain</tt> was successfully added 
     *          to the pool
     */
    public boolean offer(ProtocolChain protocolChain) {
        return protocolChains.offer(protocolChain);
    }
    
}
