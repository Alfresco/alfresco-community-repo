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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Update workflow notification templates patch
 * 
 * @author Roy Wetherall
 */
public class UpdateWorkflowNotificationTemplatesPatch extends AbstractPatch
{  
    private ContentService contentService;
    
    private FileFolderService fileFolderService;
    
    private static final String PATH = "alfresco/bootstrap/notification/";
    private static final String BASE_FILE = "wf-email.html.ftl";
    private static final String DE_FILE = "wf-email.html_de.ftl";
    private static final String ES_FILE = "wf-email.html_es.ftl";
    private static final String FR_FILE = "wf-email.html_fr.ftl";
    private static final String IT_FILE = "wf-email.html_it.ftl";
    private static final String JA_FILE = "wf-email.html_ja.ftl";
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        NodeRef baseTemplate = WorkflowNotificationUtils.WF_ASSIGNED_TEMPLATE;
        if (nodeService.exists(baseTemplate) == true)
        {
            updateContent(baseTemplate, PATH, BASE_FILE);
            updateSiblingContent(baseTemplate, PATH, DE_FILE);
            updateSiblingContent(baseTemplate, PATH, ES_FILE);
            updateSiblingContent(baseTemplate, PATH, FR_FILE);
            updateSiblingContent(baseTemplate, PATH, IT_FILE);
            updateSiblingContent(baseTemplate, PATH, JA_FILE);
        }
        
        return I18NUtil.getMessage("patch.updateWorkflowNotificationTemplates.result");
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
