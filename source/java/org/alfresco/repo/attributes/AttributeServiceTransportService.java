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

package org.alfresco.repo.attributes;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.remote.AttributeServiceTransport;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.surf.util.Pair;

/**
 * Server side implementation of transport for AttributeService.
 * @author britt
 */
public class AttributeServiceTransportService implements
        AttributeServiceTransport
{
    private AttributeService fService;

    private AuthenticationService fAuthService;
    
    public AttributeServiceTransportService()
    {
    }
    
    public void setAttributeService(AttributeService service)
    {
        fService = service;
    }
    
    public void setAuthenticationService(AuthenticationService service)
    {
        fAuthService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#addAttribute(java.lang.String, java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void addAttribute(String ticket, String path, Attribute value)
    {
        fAuthService.validate(ticket, null);
        fService.addAttribute(path, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#addAttribute(java.lang.String, java.util.List, org.alfresco.repo.attributes.Attribute)
     */
    public void addAttribute(String ticket, List<String> keys, Attribute value)
    {
        fAuthService.validate(ticket, null);
        fService.addAttribute(keys, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#getAttribute(java.lang.String, java.lang.String)
     */
    public Attribute getAttribute(String ticket, String path)
    {
        fAuthService.validate(ticket, null);
        return fService.getAttribute(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#getAttribute(java.lang.String, java.util.List)
     */
    public Attribute getAttribute(String ticket, List<String> keys)
    {
        fAuthService.validate(ticket, null);
        return fService.getAttribute(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#getKeys(java.lang.String, java.lang.String)
     */
    public List<String> getKeys(String ticket, String path)
    {
        fAuthService.validate(ticket, null);
        return fService.getKeys(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#getKeys(java.lang.String, java.util.List)
     */
    public List<String> getKeys(String ticket, List<String> keys)
    {
        fAuthService.validate(ticket, null);
        return fService.getKeys(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#query(java.lang.String, java.lang.String, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public List<Pair<String, Attribute>> query(String ticket, String path,
            AttrQuery query)
    {
        fAuthService.validate(ticket, null);
        return fService.query(path, query);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#query(java.lang.String, java.util.List, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public List<Pair<String, Attribute>> query(String ticket,
            List<String> keys, AttrQuery query)
    {
        fAuthService.validate(ticket, null);
        return fService.query(keys, query);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#removeAttribute(java.lang.String, java.lang.String, java.lang.String)
     */
    public void removeAttribute(String ticket, String path, String name)
    {
        fAuthService.validate(ticket, null);
        fService.removeAttribute(path, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#removeAttribute(java.lang.String, java.util.List, java.lang.String)
     */
    public void removeAttribute(String ticket, List<String> keys, String name)
    {
        fAuthService.validate(ticket, null);
        fService.removeAttribute(keys, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#removeAttribute(java.lang.String, java.lang.String, int)
     */
    public void removeAttribute(String ticket, String path, int index)
    {
        fAuthService.validate(ticket, null);
        fService.removeAttribute(path, index);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#removeAttribute(java.lang.String, java.util.List, int)
     */
    public void removeAttribute(String ticket, List<String> keys, int index)
    {
        fAuthService.validate(ticket, null);
        fService.removeAttribute(keys, index);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#setAttribute(java.lang.String, java.lang.String, java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String ticket, String path, String name,
            Attribute value)
    {
        fAuthService.validate(ticket, null);
        fService.setAttribute(path, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#setAttribute(java.lang.String, java.util.List, java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String ticket, List<String> keys, String name,
            Attribute value)
    {
        fAuthService.validate(ticket, null);
        fService.setAttribute(keys, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#setAttribute(java.lang.String, java.lang.String, int, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String ticket, String path, int index,
            Attribute value)
    {
        fAuthService.validate(ticket, null);
        fService.setAttribute(path, index, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeServiceTransport#setAttribute(java.lang.String, java.util.List, int, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String ticket, List<String> keys, int index,
            Attribute value)
    {
        fAuthService.validate(ticket, null);
        fService.setAttribute(keys, index, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#exists(java.lang.String, java.util.List)
     */
    public boolean exists(String ticket, List<String> keys)
    {
        fAuthService.validate(ticket, null);
        return fService.exists(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#exists(java.lang.String, java.lang.String)
     */
    public boolean exists(String ticket, String path)
    {
        fAuthService.validate(ticket, null);
        return fService.exists(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#getCount(java.lang.String, java.util.List)
     */
    public int getCount(String ticket, List<String> keys)
    {
        fAuthService.validate(ticket, null);
        return fService.getCount(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#getCount(java.lang.String, java.lang.String)
     */
    public int getCount(String ticket, String path)
    {
        fAuthService.validate(ticket, null);
        return fService.getCount(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#addAttributes(java.lang.String, java.util.List, java.util.List)
     */
    public void addAttributes(String ticket, List<String> keys, List<Attribute> values)
    {
        fAuthService.validate(ticket, null);
        fService.addAttributes(keys, values);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#addAttributes(java.lang.String, java.lang.String, java.util.List)
     */
    public void addAttributes(String ticket, String path, List<Attribute> values)
    {
        fAuthService.validate(ticket, null);
        fService.addAttributes(path, values);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#setAttributes(java.lang.String, java.util.List, java.util.Map)
     */
    public void setAttributes(String ticket, List<String> keys, Map<String, Attribute> entries)
    {
        fAuthService.validate(ticket, null);
        fService.setAttributes(keys, entries);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#setAttributes(java.lang.String, java.lang.String, java.util.Map)
     */
    public void setAttributes(String ticket, String path, Map<String, Attribute> entries)
    {
        fAuthService.validate(ticket, null);
        fService.setAttributes(path, entries);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#removeEntries(java.lang.String, java.util.List, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public void removeEntries(String ticket, List<String> keys, AttrQuery query)
    {
        fAuthService.validate(ticket, null);
        fService.removeEntries(keys, query);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.AttributeServiceTransport#removeEntries(java.lang.String, java.lang.String, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public void removeEntries(String ticket, String path, AttrQuery query)
    {
        fAuthService.validate(ticket, null);
        fService.removeEntries(path, query);
    }
}
