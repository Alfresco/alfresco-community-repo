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
package org.alfresco.service.cmr.lock;

import java.text.MessageFormat;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Runtime exception class
 * 
 * @author Roy Wetherall
 */
public class UnableToReleaseLockException extends RuntimeException
{
    /**
     * Serial verison UID
     */
    private static final long serialVersionUID = 3257565088071432244L;
    
    /**
     * Error message
     */
    private static final String ERROR_MESSAGE_1 = I18NUtil.getMessage("lock_service.insufficent_privileges");
    private static final String ERROR_MESSAGE_2 = I18NUtil.getMessage("lock_service.unlock_checkedout");

    /**
     * Constructor
     */
    public UnableToReleaseLockException(NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE_1, new Object[]{nodeRef.getId()}));
    }
    
    public enum CAUSE { INSUFFICIENT, CHECKED_OUT };
    
    private static String createMessage(NodeRef nodeRef, CAUSE cause)
    {
        if (cause == null)
        {
            return MessageFormat.format(ERROR_MESSAGE_1, new Object[] { nodeRef.getId() });
        }

        switch (cause)
        {
        case INSUFFICIENT:
            return MessageFormat.format(ERROR_MESSAGE_1, new Object[] { nodeRef.getId() });
        case CHECKED_OUT:
            return MessageFormat.format(ERROR_MESSAGE_2, new Object[] { nodeRef.getId() });
        default:
            return MessageFormat.format(ERROR_MESSAGE_1, new Object[] { nodeRef.getId() });
        }
    }
    
    public UnableToReleaseLockException(NodeRef nodeRef, CAUSE cause)
    {
        super(createMessage(nodeRef, cause));
    }
    
}
