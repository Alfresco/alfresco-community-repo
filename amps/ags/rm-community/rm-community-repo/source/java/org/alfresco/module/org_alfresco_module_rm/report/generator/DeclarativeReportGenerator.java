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

package org.alfresco.module.org_alfresco_module_rm.report.generator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Declarative report generator.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class DeclarativeReportGenerator extends BaseReportGenerator
{
    /** message lookups */
    protected static final String MSG_REPORT = "report.default";

    /** template lookup root */
    protected static final NodeRef TEMPLATE_ROOT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_report_templates");

    /** model keys */
    protected static final String KEY_NODE = "node";
    protected static final String KEY_CHILDREN = "children";

    /** applicable reported upon types */
    protected Set<QName> applicableTypes;

    /** content service */
    protected ContentService contentService;

    /** mimetype service */
    protected MimetypeService mimetypeService;

    /** file folder service */
    protected FileFolderService fileFolderService;

    /** template service */
    protected TemplateService templateService;

    /** repository helper */
    protected Repository repository;

    /** node service */
    protected NodeService nodeService;

    /** dictionary service */
    protected DictionaryService dictionaryService;

    /** sys admin params */
    protected SysAdminParams sysAdminParams;

    /**
     * @param applicableTypes   applicable types
     */
    public void setApplicableTypes(Set<QName> applicableTypes)
    {
        this.applicableTypes = applicableTypes;
    }

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
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportName(org.alfresco.service.cmr.repository.NodeRef, String)
     */
    @Override
    protected String generateReportName(NodeRef reportedUponNodeRef, String mimetype)
    {
        // get the file extension based on the mimetype
        String extension = mimetypeService.getExtension(mimetype);

        // get the name of the reported updon node ref
        String name = (String)nodeService.getProperty(reportedUponNodeRef, ContentModel.PROP_NAME);

        // build default report name
        StringBuilder builder = new StringBuilder();
        builder.append(getReportDisplayLabel());
        if (StringUtils.isNotBlank(name))
        {
            builder.append(" - ").append(name);
        }
        builder.append(".").append(extension);

        return builder.toString();
    }

    /**
     * Helper method to get the report types display label
     *
     * @return  {@link String}  report type display label
     */
    private String getReportDisplayLabel()
    {
        String result = I18NUtil.getMessage(MSG_REPORT);

        TypeDefinition typeDef = dictionaryService.getType(reportType);
        if (typeDef != null)
        {
            result = typeDef.getTitle(new StaticMessageLookup());
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportContent(NodeRef, String, Map)
     */
    @Override
    protected ContentReader generateReportContent(NodeRef reportedUponNodeRef, String mimetype, Map<String, Serializable> properties)
    {
        // get the template
        NodeRef reportTemplateNodeRef = getReportTemplate(mimetype);

        // get the model
        Map<String, Serializable> model = createTemplateModel(reportTemplateNodeRef, reportedUponNodeRef, properties);

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

    /**
     * Create template model.
     *
     * @param templateNodeRef
     * @param reportedUponNodeRef
     * @param properties
     * @return
     */
    protected Map<String, Serializable> createTemplateModel(NodeRef templateNodeRef, NodeRef reportedUponNodeRef, Map<String, Serializable> properties)
    {
        Map<String, Serializable> model = new HashMap<>();

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
        
        // who and when the report was generated
        model.put("reportUser", AuthenticationUtil.getRunAsUser());
        Calendar now = Calendar.getInstance(I18NUtil.getContentLocale());
        model.put("reportDate", SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(now.getTime()));

        // add additional properties
        model.put("properties", (Serializable) properties);

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
        if (!fileFolderService.exists(TEMPLATE_ROOT))
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

        StringBuilder sb = new StringBuilder(128)
                                .append("report_")
                                .append(typePrefixName)
                                .append(".")
                                .append(extension)
                                .append(".ftl");

        return sb.toString();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#checkReportApplicability(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void checkReportApplicability(NodeRef reportedUponNodeRef)
    {
        if (applicableTypes != null && applicableTypes.size() != 0)
        {
            boolean isTypeApplicable = false;
            QName type = nodeService.getType(reportedUponNodeRef);

            for (QName applicableType : applicableTypes)
            {
                if (dictionaryService.isSubClass(type, applicableType))
                {
                    isTypeApplicable = true;
                    break;
                }
            }

            if (!isTypeApplicable)
            {
                // throw an exception
                throw new AlfrescoRuntimeException("Can't generate report, because the provided reported upon node reference is type " + type.toString() +
                                                   " which is not an applicable type for a " + reportType.toString() + " report.");
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportTemplateContext(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Serializable> generateReportTemplateContext(NodeRef reportedUponNodeRef)
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.BaseReportGenerator#generateReportMetadata(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<QName, Serializable> generateReportMetadata(NodeRef reportedUponNodeRef)
    {
        return Collections.EMPTY_MAP;
    }
}
