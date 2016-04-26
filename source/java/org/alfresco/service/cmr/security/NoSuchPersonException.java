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
package org.alfresco.service.cmr.security;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.security.person.PersonException;

/**
 * Thrown when a person doesn't exist and can't be created.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class NoSuchPersonException extends PersonException
{
    private static final long serialVersionUID = -8514361120995433997L;

    private final String userName;
    
    public NoSuchPersonException(String userName)
    {
        super(String.format("User does not exist and could not be created: %s", userName));
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }
}
