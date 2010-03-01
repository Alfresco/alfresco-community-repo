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

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Node locked exception class
 * 
 * @author Roy Wetherall
 */
public class NodeLockedException extends AlfrescoRuntimeException
{    
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3762254149525582646L;
    
    /**
     * Error message
     */
    private static final String ERROR_MESSAGE = I18NUtil.getMessage("lock_service.no_op");
    private static final String ERROR_MESSAGE_2 = I18NUtil.getMessage("lock_service.no_op2");

    /**
     * @param message
     */
    public NodeLockedException(NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{nodeRef.getId()}));
    }   
    
    public NodeLockedException(NodeRef nodeRef, String operation)
    {
        super(MessageFormat.format(ERROR_MESSAGE_2, new Object[]{operation, nodeRef.getId()}));
    }
}
