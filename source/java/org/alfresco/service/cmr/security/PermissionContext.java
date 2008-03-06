/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
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
    
    
    
}
