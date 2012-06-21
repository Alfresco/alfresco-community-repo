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
package org.alfresco.repo.admin.patch.impl;

import java.io.InputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Updates show_audit.ftl file for upgrade from v3.3.5 to v3.4.x (ALF-13929)
 * @author alex.malinovsky
 *
 */
public class UpdateAuditTemplatePatch  extends AbstractPatch
{
    private static final String ERR_MULTIPLE_FOUND = "Multiple files for replacement were found";
    public static final String TEXT_CONTENT_MIMETYPE = "text/plain";
    private static final String MSG_CREATED = "patch.show.audit.success";
    
    private ImporterBootstrap importerBootstrap;
    private ContentService contentService;
    private String copyPath;
    private String fileName;
    

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setCopyPath(String copyPath)
    {
        this.copyPath = copyPath;
    }
    
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        StoreRef storeRef = importerBootstrap.getStoreRef();
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> results = searchService.selectNodes(rootNodeRef, copyPath, null, namespaceService, true);
        if (results.size() > 1)
        {
            throw new PatchException(ERR_MULTIPLE_FOUND, copyPath);
        }
        else if (results.size() == 1)
        {
            makeCopy(results.get(0));
            return I18NUtil.getMessage(MSG_CREATED);
        }
       
        
        return null;
    }

    private void makeCopy(NodeRef nodeRef)
    {
        InputStream resource = getClass().getClassLoader().getResourceAsStream(fileName);
        if (resource != null)
        {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setEncoding("UTF-8");
            writer.setMimetype(TEXT_CONTENT_MIMETYPE);
            writer.putContent(resource);
        }
        else throw new PatchException("Resource '"+fileName+"' not found");
    }


}
