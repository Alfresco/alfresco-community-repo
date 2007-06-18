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
package org.alfresco.repo.content.http;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * A read-only store using HTTP to access content from a remote Alfresco application.
 * <p>
 * The primary purpose of this component is to allow clustered content sharing without
 * having to have shared access to the binary data on the various machines.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class HttpAlfrescoStore extends AbstractContentStore
{
    AuthenticationComponent authenticationComponent;
    AuthenticationService authenticationService;
    private String baseHttpUrl;

    /**
     * Default constructor for bean instantiation.
     */
    public HttpAlfrescoStore()
    {
    }

    /**
     * @param authenticationComponent       the authentication compoent
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * @param authenticationService         used to retrieve authentication ticket
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Set the base HTTP URL of the remote Alfresco application.
     * 
     * @param baseHttpUrl       the remote HTTP address including the <b>.../alfresco</b>
     */
    public void setBaseHttpUrl(String baseHttpUrl)
    {
        if (baseHttpUrl.endsWith("/"))
        {
            baseHttpUrl = baseHttpUrl.substring(0, baseHttpUrl.length() - 1);
        }
        this.baseHttpUrl = baseHttpUrl;
    }

    /**
     * This <b>is</b> a read only store.
     * 
     * @return                  <tt>false</tt> always
     */
    public boolean isWriteSupported()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public ContentReader getReader(String contentUrl)
    {
        ContentReader reader = new HttpAlfrescoContentReader(
                authenticationService,
                authenticationComponent,
                baseHttpUrl,
                contentUrl);
        return reader;
    }
}
