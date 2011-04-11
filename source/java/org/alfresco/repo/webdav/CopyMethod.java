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
package org.alfresco.repo.webdav;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implements the WebDAV COPY method
 * 
 * @author Derek Hulley
 */
public class CopyMethod extends AbstractMoveOrCopyMethod
{
    /**
     * Default constructor
     */
    public CopyMethod()
    {
    }

    @Override
    protected void moveOrCopy(
            FileFolderService fileFolderService,
            NodeRef sourceNodeRef,
            NodeRef sourceParentNodeRef,
            NodeRef destParentNodeRef,
            String name) throws Exception
    {
        fileFolderService.copy(sourceNodeRef, destParentNodeRef, name);
    }
}
