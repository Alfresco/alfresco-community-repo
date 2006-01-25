/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

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
    private static final String MSG_EXISTS = "The saved searches folder already exists: %s.";
    private static final String MSG_CREATED = "The saved searches folder successfully created: %s.";
    
    private static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    private static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    private static final String PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME = "spaces.savedsearches.childname";
    private static final String PROPERTY_SAVED_SEARCHES_FOLDER_NAME = "spaces.savedsearches.name";
    private static final String PROPERTY_SAVED_SEARCHES_FOLDER_DESCRIPTION = "spaces.savedsearches.description";
    private static final String PROPERTY_ICON = "space-icon-default";
    
    private ImporterBootstrap importerBootstrap;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private NodeService nodeService;
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected String applyInternal() throws Exception
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
        
        // get the node store that we must work against
        StoreRef storeRef = importerBootstrap.getStoreRef();
        if (storeRef == null)
        {
            throw new PatchException("Bootstrap store has not been set");
        }
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

        Properties configuration = importerBootstrap.getConfiguration();
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
        NodeRef dictionaryNodeRef = nodeRefs.get(0);
        
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
        String msg = null;
        if (nodeRefs.size() == 1)
        {
            // it already exists
            msg = String.format(MSG_EXISTS, new Object[] {nodeRefs.get(0)});
        }
        else
        {
            // create it
            NodeRef savedSearchesFolderNodeRef = createFolder(dictionaryNodeRef, configuration);
            msg = String.format(MSG_CREATED, new Object[] {savedSearchesFolderNodeRef});
        }
        // done
        return msg;
    }
    
    private NodeRef createFolder(NodeRef dictionaryNodeRef, Properties configuration)
    {
        // get required properties
        String savedSearchesChildName = configuration.getProperty(
                PROPERTY_SAVED_SEARCHES_FOLDER_CHILDNAME,
                "app:saved_searches");
        String savedSearchesName = configuration.getProperty(
                PROPERTY_SAVED_SEARCHES_FOLDER_NAME,
                "Saved Searches");
        String savedSearchesDescription = configuration.getProperty(
                PROPERTY_SAVED_SEARCHES_FOLDER_DESCRIPTION,
                "Saved searches");
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, savedSearchesName);
        properties.put(ContentModel.PROP_TITLE, savedSearchesName);
        properties.put(ContentModel.PROP_DESCRIPTION, savedSearchesDescription);
        properties.put(ContentModel.PROP_ICON, PROPERTY_ICON);
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                dictionaryNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, savedSearchesChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        NodeRef nodeRef = childAssocRef.getChildRef();
        // add the required aspects
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, null);
        
        // done
        return nodeRef;
    }
}
