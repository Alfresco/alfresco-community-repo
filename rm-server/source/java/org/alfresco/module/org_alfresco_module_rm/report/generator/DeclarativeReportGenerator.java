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
import org.alfresco.repo.action.parameter.ParameterProcessorComponent;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.util.UrlUtil;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Declarative report generator.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class DeclarativeReportGenerator extends BaseReportGenerator
{
    /** template lookup root */
    protected static final NodeRef TEMPLATE_ROOT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_report_templates");
          
    /** model keys */
    protected static final String KEY_NODE = "node";
    protected static final String KEY_CHILDREN = "children";
    
    /** content service */
    protected ContentService contentService;
    
    /** mimetype service */
    protected MimetypeService mimetypeService;
    
    /** file folder service */
    protected FileFolderService fileFolderService;
    
    /** template service */
    protected TemplateService templateService;
    
    /** node service */
    protected NodeService nodeService;
    
    /** repository helper */
    protected Repository repository;
    
    /** parameter processor component */
    protected ParameterProcessorComponent parameterProcessorComponent;
    
    /** sys admin params */
    protected SysAdminParams sysAdminParams;
    
    /**
     * @param mimetypeService   mimetype service
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * @param templateService   template service
     */
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
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param parameterProcessorComponent   parameter processor component
     */
    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }
    
    /**
     * @param repository    repository helper
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    /**
     * @param sysAdminParams    sys admin params
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportName(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected String generateReportName(NodeRef reportedUponNodeRef)
    {
        String reportTypeName = reportType.getPrefixedQName(namespaceService).getPrefixString().replace(":", "_"); 
        String value = I18NUtil.getMessage("report." + reportTypeName + ".name");
        return parameterProcessorComponent.process(value, reportedUponNodeRef);
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
        Map<String, Serializable> model = createTemplateModel(reportTemplateNodeRef, reportedUponNodeRef);
        
        // run the template
        String result = templateService.processTemplate("freemarker", reportTemplateNodeRef.toString(), model);
        
        // create the temp content 
        ContentWriter contentWriter = contentService.getTempWriter();
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(mimetype);
        contentWriter.putContent(result);
        
        // return the reader to the temp content
        return contentWriter.getReader();
    }
    
    protected Map<String, Serializable> createTemplateModel(NodeRef templateNodeRef, NodeRef reportedUponNodeRef)
    {
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        
        // build the default model
        NodeRef person = repository.getPerson();
        templateService.buildDefaultModel(person, 
                                          repository.getCompanyHome(), 
                                          repository.getUserHome(person), 
                                          templateNodeRef, 
                                          null);
        
        // put the reported upon node reference in the model
        model.put(KEY_NODE, reportedUponNodeRef);
        
        // context url's (handy for images and links)
        model.put("url", UrlUtil.getAlfrescoUrl(sysAdminParams));
        model.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(sysAdminParams));
        
        return model;
    }
    
    /**
     * Get's the report template based on the type and mimetype.
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
     * Gets the template name based on the type and mimetype.
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
