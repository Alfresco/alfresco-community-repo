/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.report.generator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.util.GUID;

/**
 * Declarative report generator.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class DeclarativeReportGenerator extends BaseReportGenerator
{
    protected static final NodeRef TEMPLATE_ROOT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_report_templates");
          
    /** content service */
    protected ContentService contentService;
    
    protected MimetypeService mimetypeService;
    
    protected FileFolderService fileFolderService;
    
    protected TemplateService templateService;
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportName(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected String generateReportName(NodeRef reportedUponNodeRef)
    {
        // TODO Auto-generated method stub
        return GUID.generate();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportContent(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    protected ContentReader generateReportContent(NodeRef reportedUponNodeRef, String mimetype)
    {
        // get the template
        NodeRef reportTemplateNodeRef = getReportTemplate(mimetype);
        
        // get the model
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        
        // run the template
        String result = templateService.processTemplate("freemarker", reportTemplateNodeRef.toString(), model);
        
        // create the temp content 
        ContentWriter contentWriter = contentService.getTempWriter();
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(mimetype);
        contentWriter.putContent(result);
        
        return contentWriter.getReader();
    }
    
    /**
     * 
     * @param mimetype
     * @return
     */
    private NodeRef getReportTemplate(String mimetype)
    {
        // check that the template root has been correctly bootstraped
        if (fileFolderService.exists(TEMPLATE_ROOT) == false)
        {
            throw new AlfrescoRuntimeException("Unable to get report template, because the template root folder does not exist in the data dictionary.");
        }
        
        String reportTemplateName = getReportTemplateName(mimetype);
        
        NodeRef reportTemplateNodeRef = fileFolderService.searchSimple(TEMPLATE_ROOT, reportTemplateName);
        if (reportTemplateNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Unable to get report template, because report template " + reportTemplateName + " does not exist.");
        }
        
        // get localise template
        return fileFolderService.getLocalizedSibling(reportTemplateNodeRef);
        
        
    }
    
    /**
     * 
     * @param mimetype
     * @return
     */
    private String getReportTemplateName(String mimetype)
    {
        String typePrefixName = reportType.getPrefixedQName(namespaceService).getPrefixString().replace(":", "_");
        String extension = mimetypeService.getExtension(mimetype);
        
        StringBuffer sb = new StringBuffer(128)
                                .append("report_")
                                .append(typePrefixName)
                                .append(".")
                                .append(extension)
                                .append(".ftl");
        
        return sb.toString();
    }

}
