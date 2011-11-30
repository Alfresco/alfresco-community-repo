/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public abstract class GenericEMailTemplateUpdatePatch extends AbstractPatch
{
    protected static final String[] LOCALES = new String[] {"de", "es", "fr", "it", "ja"};
    
    protected ContentService contentService;
    
    protected FileFolderService fileFolderService;
    
    private boolean createSiblingIfMissing = true;
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setCreateSiblingIfMissing(boolean createSiblingIfMissing)
    {
        this.createSiblingIfMissing = createSiblingIfMissing;
    }

    protected void updateTemplates() throws Exception
    {
        NodeRef baseTemplate = getBaseTemplate();
        if (nodeService.exists(baseTemplate) == true)
        {
            updateContent(baseTemplate, getPath(), getBaseFileName());
            
            for (String siblingFile : getSiblingFiles())
            {
                updateSiblingContent(baseTemplate, getPath(), siblingFile);
            }            
        }
    }
    
    protected abstract NodeRef getBaseTemplate();
    
    protected abstract String getPath();
    
    protected abstract String getBaseFileName();
    
    protected String[] getLocales()
    {
        return LOCALES;
    }
    
    protected List<String> getSiblingFiles()
    {
        List<String> siblingFiles = new ArrayList<String>(LOCALES.length);
        for (String locale : LOCALES)
        {
            siblingFiles.add(makeSiblingFileName(getBaseFileName(), locale));
        }
        return siblingFiles;
    }
    
    private String makeSiblingFileName(String baseFileName, String locale)
    {
        int index = baseFileName.lastIndexOf(".");
        StringBuilder builder = new StringBuilder();
        builder.append(baseFileName.substring(0, index))
               .append("_")
               .append(locale)
               .append(baseFileName.substring(index));
        return builder.toString();        
    }
    
    private void updateSiblingContent(NodeRef nodeRef, String path, String fileName)
    {
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent != null)
        {
            NodeRef sibling = fileFolderService.searchSimple(parent, fileName);
            if (sibling != null)
            {
                updateContent(sibling, path, fileName);
            }
            else if (createSiblingIfMissing == true)
            {
                sibling = fileFolderService.create(parent, fileName, ContentModel.TYPE_CONTENT).getNodeRef();
                updateContent(sibling, path, fileName);
            }
        }
    }
    
    private void updateContent(NodeRef nodeRef, String path, String fileName)
    {
        // Make versionable
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        
        // Update content
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path + fileName);
        if (is != null)
        {
            ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            contentWriter.putContent(is);
        }
    }

}
