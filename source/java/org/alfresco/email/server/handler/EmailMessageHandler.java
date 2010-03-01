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
package org.alfresco.email.server.handler;

import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface for email handler for processing email message.
 * 
 * @author maxim
 * @since 2.2
 */
public interface EmailMessageHandler
{
    /**
     * Method invokes for processing email message.
     * 
     * @param nodeRef Target node
     * @param message Email message
     * @exception EmailMessageException Exception is thrown if processing was failed
     * @exception DuplicateChildNodeNameException Exception is thrown if node name is duplicate.
     */
    void processMessage(NodeRef nodeRef, EmailMessage message);

}
