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
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.context.MessageSource;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Ensures that the <b>Web Projects</b> and <b>Web Forms</b>folders are present.
 * <p>
 * This uses the bootstrap importer to get the paths to look for.  If not present,
 * the required structures are created.
 * <p>
 * 
 * @author Kevin Roast
 */
public class WCMFoldersPatch extends AbstractPatch
{
    private static final String MSG_WEBPROJECTS_EXISTS = "patch.wcmFolders.webprojects.result.exists";
    private static final String MSG_WEBPROJECTS_CREATED = "patch.wcmFolders.webprojects.result.created";
    private static final String MSG_WEBFORMS_EXISTS = "patch.wcmFolders.webforms.result.exists";
    private static final String MSG_WEBFORMS_CREATED = "patch.wcmFolders.webforms.result.created";
    
    private static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    private static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    public static final String PROPERTY_WEBPROJECTS_FOLDER_CHILDNAME = "spaces.wcm.childname";
    private static final String MSG_WEBPROJECTS_FOLDER_NAME = "spaces.wcm.name";
    private static final String MSG_WEBPROJECTS_FOLDER_DESCRIPTION = "spaces.wcm.description";
    public static final String PROPERTY_WEBFORMS_FOLDER_CHILDNAME = "spaces.wcm_content_forms.childname";
    private static final String MSG_WEBFORMS_FOLDER_NAME = "spaces.wcm_content_forms.name";
    private static final String MSG_WEBFORMS_FOLDER_DESCRIPTION = "spaces.wcm_content_forms.description";
    
    private static final String PROPERTY_ICON = "space-icon-default";
    
    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;
    
    protected NodeRef companyHomeNodeRef;
    protected NodeRef dictionaryNodeRef;
    protected Properties configuration;
    protected NodeRef wcmProjectsFolderNodeRef;
    protected NodeRef wcmFormsFolderNodeRef;
    
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
        if (importerBootstrap == null)
        {
            throw new PatchException("'importerBootstrap' property has not been set");
        }
        else if (namespaceService == null)
        {
            throw new PatchException("'namespaceService' property has not been set");
        }
        else if (searchService == null)
        {
            throw new PatchException("'searchService' property has not been set");
        }
        else if (nodeService == null)
        {
            throw new PatchException("'nodeService' property has not been set");
        }
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
        String wcmProjectsChildName = configuration.getProperty(PROPERTY_WEBPROJECTS_FOLDER_CHILDNAME);
        if (wcmProjectsChildName == null || wcmProjectsChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_WEBPROJECTS_FOLDER_CHILDNAME + "' is not present");
        }
        String wcmFormsChildName = configuration.getProperty(PROPERTY_WEBFORMS_FOLDER_CHILDNAME);
        if (wcmFormsChildName == null || wcmFormsChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_WEBFORMS_FOLDER_CHILDNAME + "' is not present");
        }
        
        // build the search string to get the company home node
        StringBuilder sb = new StringBuilder(256);
        sb.append("/").append(companyHomeChildName);
        String xpath = sb.toString();
        // get the company home
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        this.companyHomeNodeRef = nodeRefs.get(0);
        
        // build the search string to get the dictionary node
        sb.append("/").append(dictionaryChildName);
        xpath = sb.toString();
        // get the dictionary node
        nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        this.dictionaryNodeRef = nodeRefs.get(0);
        
        // Check for the existence of the 'Web Projects' folder
        xpath = wcmProjectsChildName;
        nodeRefs = searchService.selectNodes(companyHomeNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   company home node: " + companyHomeNodeRef + "\n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            // the node does not exist
            this.wcmProjectsFolderNodeRef = null;
        }
        else
        {
            // we have the 'Web Projects' folder noderef
            this.wcmProjectsFolderNodeRef = nodeRefs.get(0);
        }
        
        // Check for the existence of the 'Web Forms' folder
        xpath = wcmFormsChildName;
        nodeRefs = searchService.selectNodes(dictionaryNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" +
                    "   dictionary node: " + dictionaryNodeRef + "\n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            // the node does not exist
            this.wcmFormsFolderNodeRef = null;
        }
        else
        {
            // we have the 'Web Forms' folder noderef
            this.wcmFormsFolderNodeRef = nodeRefs.get(0);
        }
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // properties must be set
        checkCommonProperties();
        if (messageSource == null)
        {
            throw new PatchException("'messageSource' property has not been set");
        }
        
        // get useful values
        setUp();
        
        String msg = null;
        if (wcmProjectsFolderNodeRef == null)
        {
            // create it
            createWebProjectsFolder();
            msg = I18NUtil.getMessage(MSG_WEBPROJECTS_CREATED, wcmProjectsFolderNodeRef);
        }
        else
        {
            // it already exists
            msg = I18NUtil.getMessage(MSG_WEBPROJECTS_EXISTS, wcmProjectsFolderNodeRef);
        }
        
        if (wcmFormsFolderNodeRef == null)
        {
            // create it
            createWebFormsFolder();
            msg = msg + "\r\n" + I18NUtil.getMessage(MSG_WEBFORMS_CREATED, wcmProjectsFolderNodeRef);
        }
        else
        {
            // it already exists
            msg = msg + "\r\n" + I18NUtil.getMessage(MSG_WEBFORMS_EXISTS, wcmProjectsFolderNodeRef);
        }
        
        // done
        return msg;
    }
    
    private void createWebProjectsFolder()
    {
        // get required properties
        String wcmProjectsChildName = configuration.getProperty(PROPERTY_WEBPROJECTS_FOLDER_CHILDNAME);
        if (wcmProjectsChildName == null)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_WEBPROJECTS_FOLDER_CHILDNAME + "' is not present");
        }
        
        String name = messageSource.getMessage(
                MSG_WEBPROJECTS_FOLDER_NAME,
                null,
                I18NUtil.getLocale());
        if (name == null || name.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + MSG_WEBPROJECTS_FOLDER_NAME + "' is not present");
        }

        String description = messageSource.getMessage(
                MSG_WEBPROJECTS_FOLDER_DESCRIPTION,
                null,
                I18NUtil.getLocale());
        if (description == null || description.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + MSG_WEBPROJECTS_FOLDER_DESCRIPTION + "' is not present");
        }
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, name);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                companyHomeNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, wcmProjectsChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        wcmProjectsFolderNodeRef = childAssocRef.getChildRef();
        
        // add the required aspects
        nodeService.addAspect(wcmProjectsFolderNodeRef, ApplicationModel.ASPECT_UIFACETS, null);

        // ALF-906: ensure that DM rules are not inherited by web projects
        nodeService.addAspect(wcmProjectsFolderNodeRef, RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);
    }
    
    private void createWebFormsFolder()
    {
        // get required properties
        String wcmFormsChildName = configuration.getProperty(PROPERTY_WEBFORMS_FOLDER_CHILDNAME);
        if (wcmFormsChildName == null)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_WEBFORMS_FOLDER_CHILDNAME + "' is not present");
        }
        
        String name = messageSource.getMessage(
                MSG_WEBFORMS_FOLDER_NAME,
                null,
                I18NUtil.getLocale());
        if (name == null || name.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + MSG_WEBFORMS_FOLDER_NAME + "' is not present");
        }

        String description = messageSource.getMessage(
                MSG_WEBFORMS_FOLDER_DESCRIPTION,
                null,
                I18NUtil.getLocale());
        if (description == null || description.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + MSG_WEBFORMS_FOLDER_DESCRIPTION + "' is not present");
        }
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, name);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                dictionaryNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, wcmFormsChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        wcmFormsFolderNodeRef = childAssocRef.getChildRef();
        
        // add the required aspects
        nodeService.addAspect(wcmFormsFolderNodeRef, ApplicationModel.ASPECT_UIFACETS, null);
    }
}
