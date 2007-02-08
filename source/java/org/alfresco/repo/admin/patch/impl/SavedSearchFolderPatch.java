/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.i18n.I18NUtil;
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

/**
 * Ensures that the <b>savedsearches</b> folder is present.
 * <p>
 * This uses the bootstrap importer to get the paths to look for.  If not present,
 * the required structures are created.
 * <p>
 * This class should be replaced with a more generic <code>ImporterPatch</code>
 * that can do conditional importing into given locations.
 * <p>
 * JIRA: {@link http://www.alfresco.org/jira/browse/AR-342 AR-342}
 * 
 * @author Derek Hulley
 */
public class SavedSearchFolderPatch extends AbstractPatch
{
    private static final String MSG_EXISTS = "patch.savedSearchesFolder.result.exists";
    private static final String MSG_CREATED = "patch.savedSearchesFolder.result.created";
    
    public static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    public static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    public static final String PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME = "spaces.savedsearches.childname";
    private static final String PROPERTY_SAVED_SEARCHES_FOLDER_NAME = "spaces.savedsearches.name";
    private static final String PROPERTY_SAVED_SEARCHES_FOLDER_DESCRIPTION = "spaces.savedsearches.description";
    private static final String PROPERTY_ICON = "space-icon-default";
    
    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;
    
    protected NodeRef dictionaryNodeRef;
    protected Properties configuration;
    protected NodeRef savedSearchesFolderNodeRef;
    
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
        String savedSearchesChildName = configuration.getProperty(PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME);
        if (savedSearchesChildName == null || savedSearchesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME + "' is not present");
        }
        
        // build the search string to get the dictionary node
        StringBuilder sb = new StringBuilder(512);
        sb.append("/").append(companyHomeChildName)
          .append("/").append(dictionaryChildName);
        String xpath = sb.toString();
        // get the dictionary node
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
        this.dictionaryNodeRef = nodeRefs.get(0);
        
        // Now we have the optional part.  Check for the existence of the saved searches folder
        xpath = savedSearchesChildName;
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
            this.savedSearchesFolderNodeRef = null;
        }
        else
        {
            // we have the saved searches folder noderef
            this.savedSearchesFolderNodeRef = nodeRefs.get(0);
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
        if (savedSearchesFolderNodeRef == null)
        {
            // create it
            createFolder();
            msg = I18NUtil.getMessage(MSG_CREATED, savedSearchesFolderNodeRef);
        }
        else
        {
            // it already exists
            msg = I18NUtil.getMessage(MSG_EXISTS, savedSearchesFolderNodeRef);
        }
        // done
        return msg;
    }
    
    private void createFolder()
    {
        // get required properties
        String savedSearchesChildName = configuration.getProperty(PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME);
        if (savedSearchesChildName == null)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME + "' is not present");
        }
        
        String savedSearchesName = messageSource.getMessage(
                PROPERTY_SAVED_SEARCHES_FOLDER_NAME,
                null,
                I18NUtil.getLocale());
        if (savedSearchesName == null || savedSearchesName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SAVED_SEARCHES_FOLDER_NAME + "' is not present");
        }

        String savedSearchesDescription = messageSource.getMessage(
                PROPERTY_SAVED_SEARCHES_FOLDER_DESCRIPTION,
                null,
                I18NUtil.getLocale());
        if (savedSearchesDescription == null || savedSearchesDescription.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SAVED_SEARCHES_FOLDER_DESCRIPTION + "' is not present");
        }

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, savedSearchesName);
        properties.put(ContentModel.PROP_TITLE, savedSearchesName);
        properties.put(ContentModel.PROP_DESCRIPTION, savedSearchesDescription);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                dictionaryNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, savedSearchesChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        savedSearchesFolderNodeRef = childAssocRef.getChildRef();
        // add the required aspects
        nodeService.addAspect(savedSearchesFolderNodeRef, ApplicationModel.ASPECT_UIFACETS, null);
        
        // done
    }
}
