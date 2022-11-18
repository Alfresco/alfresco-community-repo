/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.core.exceptions;

/**
 * The addressed entity was not found
 *
 * @author Gethin James
 */
public class EntityNotFoundException extends NotFoundException
{
    private static final long serialVersionUID = -1198595000441207734L;
    public static String DEFAULT_MESSAGE_ID = "framework.exception.EntityNotFound";

    /**
     * The entity id param will be shown in the default error message.
     * @param entityId String
     */
    public EntityNotFoundException(String entityId)
    {
        super(DEFAULT_MESSAGE_ID, new String[] {entityId});
    }

    /**
     * The entity id param will be shown in the default error message.
     *
     * @param msgId The message template.
     * @param parameters The message template parameters.
     */
    public EntityNotFoundException(String msgId, String[] parameters)
    {
        super(msgId, parameters);
    }

    public EntityNotFoundException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
