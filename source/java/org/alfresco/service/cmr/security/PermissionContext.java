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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

public class PermissionContext 
{
    private QName type;
    
    private HashSet<QName> aspects = new HashSet<QName>();
    
    private Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    
    private Map<String, Set<String>> dynamicAuthorityAssignment = new HashMap<String, Set<String>>();
    
    private Map<String, Object> additionalContext = new HashMap<String, Object>();
    
    private Long storeAcl = null;
    
    public PermissionContext(QName type)
    {
        this.type = type;
    }

    public HashSet<QName> getAspects()
    {
        return aspects;
    }

    public Map<String, Set<String>> getDynamicAuthorityAssignment()
    {
        return dynamicAuthorityAssignment;
    }

    public void addDynamicAuthorityAssignment(String user, String dynamicAuthority)
    {
        Set<String> dynamicAuthorities = dynamicAuthorityAssignment.get(user);
        if(dynamicAuthorities == null)
        {
            dynamicAuthorities = new HashSet<String>();
            dynamicAuthorityAssignment.put(user, dynamicAuthorities);
        }
        dynamicAuthorities.add(dynamicAuthority);
    }
    
    public Map<String, Object> getAdditionalContext()
    {
        return additionalContext;
    }

    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }

    public QName getType()
    {
        return type;
    }

    public Long getStoreAcl()
    {
        return storeAcl;
    }

    public void setStoreAcl(Long storeAcl)
    {
        this.storeAcl = storeAcl;
    }
    
    
    
}
