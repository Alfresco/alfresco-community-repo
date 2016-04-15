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
package org.alfresco.service.cmr.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Common exception thrown when an operation fails because of a name clash.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class FileExistsException extends AlfrescoRuntimeException
{
    private static final String MESSAGE_ID = "file_folder_service.file_exists_message";

    private static final long serialVersionUID = -4133713912784624118L;
    
    private NodeRef parentNodeRef;
    private String name;

    public FileExistsException(NodeRef parentNodeRef, String name)
    {
        super(MESSAGE_ID, new Object[] { name });
        this.parentNodeRef = parentNodeRef;
        this.name = name;
    }

    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }

    public String getName()
    {
        return name;
    }
}
