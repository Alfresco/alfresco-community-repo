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
 * Ensures that the <b>User Homes</b> folder is present beneath company home.
 * It inherits permissions from company home.
 * <p>
 * This uses the bootstrap importer to get the paths to look for.  If not present,
 * the required structures are created.
 * <p>
 * 
 * @author Andy Hind
 */
public class UserHomesFolderPatch extends AbstractPatch
{
    private static final String MSG_EXISTS = "patch.userHomesFolder.result.exists";
    private static final String MSG_CREATED = "patch.userHomesFolder.result.created";
    
    public static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    public static final String PROPERTY_USER_HOMES_FOLDER_CHILDNAME = "spaces.user_homes.childname";
    private static final String PROPERTY_USER_HOMES_FOLDER_NAME = "spaces.user_homes.name";
    private static final String PROPERTY_USER_HOMES_FOLDER_DESCRIPTION = "spaces.user_homes.description";
    private static final String PROPERTY_ICON = "space-icon-default";
    
    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;
    
    protected NodeRef companyHomeNodeRef;
    protected Properties configuration;
    protected NodeRef userHomesFolderNodeRef;
    
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
       
        String userHomesChildName = configuration.getProperty(PROPERTY_USER_HOMES_FOLDER_CHILDNAME);
        if (userHomesChildName == null || userHomesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_USER_HOMES_FOLDER_CHILDNAME + "' is not present");
        }
        
        // build the search string to get the company home node
        StringBuilder sb = new StringBuilder(512);
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
        
        // Now we have the optional part.  Check for the existence of the user homes folder
        xpath = userHomesChildName;
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
            this.userHomesFolderNodeRef = null;
        }
        else
        {
            // we have the user homes folder noderef
            this.userHomesFolderNodeRef = nodeRefs.get(0);
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
        if (userHomesFolderNodeRef == null)
        {
            // create it
            createFolder();
            msg = I18NUtil.getMessage(MSG_CREATED, userHomesFolderNodeRef);
        }
        else
        {
            // it already exists
            msg = I18NUtil.getMessage(MSG_EXISTS, userHomesFolderNodeRef);
        }
        // done
        return msg;
    }
    
    private void createFolder()
    {
        // get required properties
        String userHomesChildName = configuration.getProperty(PROPERTY_USER_HOMES_FOLDER_CHILDNAME);
        if (userHomesChildName == null)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_USER_HOMES_FOLDER_CHILDNAME + "' is not present");
        }
        
        String userHomesName = messageSource.getMessage(
                PROPERTY_USER_HOMES_FOLDER_NAME,
                null,
                I18NUtil.getLocale());
        if (userHomesName == null || userHomesName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_USER_HOMES_FOLDER_NAME + "' is not present");
        }

        String userHomesDescription = messageSource.getMessage(
                PROPERTY_USER_HOMES_FOLDER_DESCRIPTION,
                null,
                I18NUtil.getLocale());
        if (userHomesDescription == null || userHomesDescription.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_USER_HOMES_FOLDER_DESCRIPTION + "' is not present");
        }

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, userHomesName);
        properties.put(ContentModel.PROP_TITLE, userHomesName);
        properties.put(ContentModel.PROP_DESCRIPTION, userHomesDescription);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                companyHomeNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, userHomesChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        userHomesFolderNodeRef = childAssocRef.getChildRef();
        // add the required aspects
        nodeService.addAspect(userHomesFolderNodeRef, ApplicationModel.ASPECT_UIFACETS, null);
        
        // done
    }
}
