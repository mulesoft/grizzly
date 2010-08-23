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

package com.sun.grizzly.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link AttributeBuilder} implementation.
 *
 * @see AttributeBuilder
 * 
 * @author Alexey Stashok
 */
public class DefaultAttributeBuilder implements AttributeBuilder {
    protected List<Attribute> attributes = new ArrayList<Attribute>();
    protected Map<String, Attribute> name2Attribute = new HashMap<String, Attribute>();
    
    /**
     * Method sets the index to the Attribute
     * Could be used by classes, which extend DefaultAttributeBuilder and don't
     * have access to the protected Attribute.setIndex() method.
     * 
     * @param attribute
     * @param index
     */
    protected static void setAttributeIndex(Attribute attribute, int index) {
        attribute.setIndex(index);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T> Attribute<T> createAttribute(String name) {
        return createAttribute(name, (T) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T> Attribute<T> createAttribute(String name, 
            T defaultValue) {
        Attribute<T> attribute = name2Attribute.get(name);
        if (attribute == null) {
            attribute = new Attribute<T>(this, name, defaultValue);
            attribute.setIndex(attributes.size());
            attributes.add(attribute);
            name2Attribute.put(name, attribute);
        }
        
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized <T> Attribute<T> createAttribute(String name, 
            NullaryFunction<T> initializer) {
        Attribute<T> attribute = name2Attribute.get(name);
        if (attribute == null) {
            attribute = new Attribute<T>(this, name, initializer);
            attribute.setIndex(attributes.size());
            attributes.add(attribute);
            name2Attribute.put(name, attribute);
        }
        
        return attribute;
    }

    protected Attribute getAttributeByName(String name) {
        return name2Attribute.get(name);
    }

    protected Attribute getAttributeByIndex(int index) {
        return attributes.get(index);
    }
}
