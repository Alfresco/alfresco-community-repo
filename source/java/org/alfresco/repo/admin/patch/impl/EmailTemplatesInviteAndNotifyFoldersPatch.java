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

package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.context.MessageSource;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Ensures that the <b>invite email templates</b> and <b>notify email templates</b> folders are present.
 * <p>
 * This uses the bootstrap importer to get the paths to look for.  If not present,
 * the required structures are created.
 * <p>
 * 
 * @author valerysh
 *
 */
public class EmailTemplatesInviteAndNotifyFoldersPatch extends AbstractPatch {
    
    public static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    public static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    public static final String PROPERTY_EMAIL_TEMPLATES_FOLDER_CHILDNAME = "spaces.templates.email.childname";
    public static final String PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_CHILDNAME = "spaces.templates.email.notify.childname";
    public static final String PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_CHILDNAME = "spaces.templates.email.invite1.childname";
    private static final String PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_NAME = "spaces.notify_templates.email.name";
    private static final String PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_DESCRIPTION = "spaces.notify_templates.email.description";
    private static final String PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_NAME = "spaces.invite_templates.email.name";
    private static final String PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_DESCRIPTION = "spaces.invite_templates.email.description";
    
    private static final String SAMPLE_NOTIFY_TEMPLATE_NAME = "notify_user_email.ftl.sample";
    private static final String INVITE_TEMPLATE_NAME = "invite_user_email.ftl";
    
    private static final String MSG_EMAIL_INVITE_TEMPLATES_FOLDER_EXISTS  = "patch.emailInviteTemplatesFolder.result.exists";
    private static final String MSG_EMAIL_INVITE_TEMPLATES_FOLDER_CREATED = "patch.emailInviteTemplatesFolder.result.created";
    private static final String MSG_EMAIL_NOTIFY_TEMPLATES_FOLDER_EXISTS  = "patch.emailNotifyTemplatesFolder.result.exists";
    private static final String MSG_EMAIL_NOTIFY_TEMPLATES_FOLDER_CREATED = "patch.emailNotifyTemplatesFolder.result.created";
    
    private static final String PROPERTY_ICON = "space-icon-default";
    
    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;
    
    protected NodeRef emailNotifyTemplatesFolderNodeRef;
    protected NodeRef emailInviteTemplatesFolderNodeRef;
    
    protected Properties configuration;
    protected NodeRef emailTemplatesFolderNodeRef;
    
    private String emailTemplatesFolderXPath;
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }
    
    /**
     * Ensure that required common properties have been set
     */
    protected void checkCommonProperties() throws Exception
    {
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(messageSource, "messageSource");
    }

    /**
     * Extracts pertinent references and properties that are common to execution
     * of this and derived patches.
     */
    protected void setUp() throws Exception
    {
        // get the node store that we must work against
        StoreRef storeRef = importerBootstrap.getStoreRef();
        if (storeRef == null)
        {
            throw new PatchException("Bootstrap store has not been set");
        }
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        
        this.configuration = importerBootstrap.getConfiguration();
        
        // get the association names that form the path
        String companyHomeChildName = configuration.getProperty(PROPERTY_COMPANY_HOME_CHILDNAME);
        if (companyHomeChildName == null || companyHomeChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_COMPANY_HOME_CHILDNAME + "' is not present");
        }
        String dictionaryChildName = configuration.getProperty(PROPERTY_DICTIONARY_CHILDNAME);
        if (dictionaryChildName == null || dictionaryChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_DICTIONARY_CHILDNAME + "' is not present");
        }
        String emailTemplatesChildName = configuration.getProperty(PROPERTY_EMAIL_TEMPLATES_FOLDER_CHILDNAME);
        if (emailTemplatesChildName == null || emailTemplatesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_EMAIL_TEMPLATES_FOLDER_CHILDNAME + "' is not present");
        }
        
        String emailNotifyTemplatesChildName = configuration.getProperty(PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_CHILDNAME);
        if (emailNotifyTemplatesChildName == null || emailNotifyTemplatesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_CHILDNAME + "' is not present");
        }
        
        String emailInviteTemplatesChildName = configuration.getProperty(PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_CHILDNAME);
        if (emailInviteTemplatesChildName == null || emailInviteTemplatesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_CHILDNAME + "' is not present");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(companyHomeChildName)
          .append("/").append(dictionaryChildName)
          .append("/").append(emailTemplatesChildName);
        emailTemplatesFolderXPath = sb.toString();
        
        // get the email templates node
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, emailTemplatesFolderXPath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + emailTemplatesFolderXPath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + emailTemplatesFolderXPath + "\n" +
                    "   results: " + nodeRefs);
        }
        this.emailTemplatesFolderNodeRef = nodeRefs.get(0);
        
        emailNotifyTemplatesFolderNodeRef = searchFolder(emailNotifyTemplatesChildName);
        emailInviteTemplatesFolderNodeRef = searchFolder(emailInviteTemplatesChildName);
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // common properties must be set before we can continue
        checkCommonProperties();
        if (messageSource == null)
        {
            throw new PatchException("'messageSource' property has not been set");
        }
        
        setUp();
        
        // create the folder if needed - output a message to describe the result
        StringBuffer msg = new StringBuffer();
        if (emailNotifyTemplatesFolderNodeRef == null)
        {
            emailNotifyTemplatesFolderNodeRef = createFolderAndMoveTemplate(PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_CHILDNAME,
                    PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_NAME,
                    PROPERTY_EMAIL_NOTIFY_TEMPLATES_FOLDER_DESCRIPTION,
                    SAMPLE_NOTIFY_TEMPLATE_NAME);
            msg.append(I18NUtil.getMessage(MSG_EMAIL_NOTIFY_TEMPLATES_FOLDER_CREATED, emailNotifyTemplatesFolderNodeRef));
        }
        else
        {
            msg.append(I18NUtil.getMessage(MSG_EMAIL_NOTIFY_TEMPLATES_FOLDER_EXISTS, emailNotifyTemplatesFolderNodeRef));
        }
        msg.append("; ");
        if (emailInviteTemplatesFolderNodeRef == null)
        {
            emailInviteTemplatesFolderNodeRef = createFolderAndMoveTemplate(PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_CHILDNAME,
                    PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_NAME,
                    PROPERTY_EMAIL_INVITE_TEMPLATES_FOLDER_DESCRIPTION,
                    INVITE_TEMPLATE_NAME);
            msg.append(I18NUtil.getMessage(MSG_EMAIL_INVITE_TEMPLATES_FOLDER_CREATED, emailNotifyTemplatesFolderNodeRef));
        }
        else
        {
            msg.append(I18NUtil.getMessage(MSG_EMAIL_INVITE_TEMPLATES_FOLDER_EXISTS, emailNotifyTemplatesFolderNodeRef));
        }
        
        return msg.toString();
    }
    
    private NodeRef searchFolder(String xpath)
    {
        List<NodeRef> nodeRefs = searchService.selectNodes(emailTemplatesFolderNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   email templates node: " + emailTemplatesFolderNodeRef + "\n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            // the node does not exist
            return null;
        }
        else
        {
            return nodeRefs.get(0);
        }
    }
    
    private NodeRef createFolderAndMoveTemplate(String folderChildName, String folderName, String folderDescription, String templateName)
    {
        // get required properties
        String emailTemplatesChildName = configuration.getProperty(folderChildName);
        if (emailTemplatesChildName == null)
        {
            throw new PatchException("Bootstrap property '" + folderChildName + "' is not present");
        }
        
        String emailTemplatesName = messageSource.getMessage(
                folderName,
                null,
                I18NUtil.getLocale());
        if (emailTemplatesName == null || emailTemplatesName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + folderName + "' is not present");
        }
        
        String emailTemplatesDescription = messageSource.getMessage(
                folderDescription,
                null,
                I18NUtil.getLocale());
        if (emailTemplatesDescription == null || emailTemplatesDescription.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + folderDescription + "' is not present");
        }
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, emailTemplatesName);
        properties.put(ContentModel.PROP_TITLE, emailTemplatesName);
        properties.put(ContentModel.PROP_DESCRIPTION, emailTemplatesDescription);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                emailTemplatesFolderNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, emailTemplatesChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        NodeRef createdFolderNodeRef = childAssocRef.getChildRef();
        
        // add the required aspects
        nodeService.addAspect(createdFolderNodeRef, ApplicationModel.ASPECT_UIFACETS, null);
        
        //move template
        String xpath = emailTemplatesFolderXPath + "/cm:" + templateName;
        List<NodeRef> templateNodeRefs = searchService.selectNodes(emailTemplatesFolderNodeRef, xpath, null, namespaceService, false);
        for (NodeRef templateNodeRef : templateNodeRefs)
        {
            QName qname = nodeService.getPrimaryParent(templateNodeRef).getQName();
            nodeService.moveNode(
                    templateNodeRef,
                    createdFolderNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    qname);
        }
        
        return createdFolderNodeRef;
    }
}
