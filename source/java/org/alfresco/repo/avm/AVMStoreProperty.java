/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * Arbitrary properties associated with AVMStores.
 * @author britt
 */
public interface AVMStoreProperty
{
    /**
     * Set the AVMStore.
     * @param store The AVMStore to set.
     */
    public void setStore(AVMStore store);
    
    /**
     * Get the AVMStore.
     * @return The AVMStore this property belongs to.
     */
    public AVMStore getStore();
    
    /**
     * Set the property type.
     * 
     * @param qname       the store property QName
     */
    public void setQname(QName qname);
    
    /**
     * Get the property type.
     * 
     * @return              returns the store property QName
     */
    public QName getQname();
    
    /**
     * Set the actual property value.
     * @param value The PropertyValue to set.
     */
    public void setValue(PropertyValue value);
    
    /**
     * Get the actual property value.
     * @return The actual PropertyValue.
     */
    public PropertyValue getValue();
}
