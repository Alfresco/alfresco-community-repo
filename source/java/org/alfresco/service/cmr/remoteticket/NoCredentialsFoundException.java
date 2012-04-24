/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.remoteticket;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 * Exception thrown if no credentials could be found when
 *  attempting to perform an authentication request to
 *  a remote system.
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class NoCredentialsFoundException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -1167368337984937185L;

    public NoCredentialsFoundException() 
    {
        super("No Credentials Found");
    }
    
    public NoCredentialsFoundException(String remoteSystemId) 
    {
        super("No Credentials Found for " + AuthenticationUtil.getRunAsUser() + " for Remote System '" + remoteSystemId + "'");
    }
}