/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.model.filefolder;

import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public class ExtendedFileFolderServiceImpl extends FileFolderServiceImpl
{
    protected RecordService recordService;

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName)
    {
        return create(parentNodeRef, name, typeQName, null);
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName)
    {
        FileInfo result = null;

        recordService.disablePropertyEditableCheck();
        try
        {
            result = super.create(parentNodeRef, name, typeQName, assocQName);
        }
        finally
        {
            recordService.enablePropertyEditableCheck();
            if (result != null)
            {
                recordService.disablePropertyEditableCheck(result.getNodeRef());
            }
        }

        return result;
    }
}
