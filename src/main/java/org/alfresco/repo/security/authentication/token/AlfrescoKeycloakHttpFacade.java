/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authentication.token;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.adapters.servlet.ServletHttpFacade;

/**
 * Keycloak HttpFacade wrapper so we can re-use Keycloak authenticator classes.
 *
 * @author Gavin Cornwell
 */
public class AlfrescoKeycloakHttpFacade extends ServletHttpFacade
{
    public AlfrescoKeycloakHttpFacade(HttpServletRequest request)
    {
        super(request, null);
    }

    @Override
    public Response getResponse()
    {
        // return our dummy NoOp implementation so we don't effect the ACS response
        return new NoOpResponseFacade();
    }
    
    /**
     * NoOp implementation of Keycloak Response interface.
     */
    private class NoOpResponseFacade implements Response
    {

        @Override
        public void setStatus(int status)
        {
        }

        @Override
        public void addHeader(String name, String value)
        {
        }

        @Override
        public void setHeader(String name, String value)
        {
        }

        @Override
        public void resetCookie(String name, String path)
        {
        }

        @Override
        public void setCookie(String name, String value, String path, String domain, int maxAge,
                    boolean secure, boolean httpOnly)
        {
        }

        @Override
        public OutputStream getOutputStream()
        {
            return new ByteArrayOutputStream();
        }

        @Override
        public void sendError(int code)
        {
        }

        @Override
        public void sendError(int code, String message)
        {
        }

        @Override
        public void end()
        {
        }
    }
}
