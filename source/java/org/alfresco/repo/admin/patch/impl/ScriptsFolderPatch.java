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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;

/**
 * Ensures that the <b>scripts</b> folder is present.
 * <p>
 * This uses the bootstrap importer to get the paths to look for.  If not present,
 * the required structures are created.
 * <p>
 * This class should be replaced with a more generic <code>ImporterPatch</code>
 * that can do conditional importing into given locations.
 * <p>
 * JIRA: {@link http://www.alfresco.org/jira/browse/AR-342 AR-342}
 * 
 * @author Kevin Roast
 */
public class ScriptsFolderPatch extends AbstractPatch
{
    private static final String MSG_EXISTS = "patch.scriptsFolder.result.exists";
    private static final String MSG_CREATED = "patch.scriptsFolder.result.created";
    
    public static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    public static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    public static final String PROPERTY_SCRIPTS_FOLDER_CHILDNAME = "spaces.scripts.childname";
    private static final String PROPERTY_SCRIPTS_FOLDER_NAME = "spaces.scripts.name";
    private static final String PROPERTY_SCRIPTS_FOLDER_DESCRIPTION = "spaces.scripts.description";
    private static final String PROPERTY_ICON = "space-icon-default";
    
    private ImporterBootstrap importerBootstrap;
    private ImporterService importerService;
    private MessageSource messageSource;
    
    protected NodeRef dictionaryNodeRef;
    protected Properties configuration;
    protected NodeRef scriptsFolderNodeRef;
    
    private String scriptsACP;
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }
    
    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }

    public void setScriptsACP(String scriptsACP)
    {
        this.scriptsACP = scriptsACP;
    }

   /**
     * Ensure that required common properties have been set
     */
    protected void checkCommonProperties() throws Exception
    {
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(importerService, "importerService");
        checkPropertyNotNull(messageSource, "messageSource");
        if (namespaceService == null)
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
        checkPropertyNotNull(scriptsACP, "scriptsACP");
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
        String scriptsChildName = configuration.getProperty(PROPERTY_SCRIPTS_FOLDER_CHILDNAME);
        if (scriptsChildName == null || scriptsChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SCRIPTS_FOLDER_CHILDNAME + "' is not present");
        }
        
        // build the search string to get the dictionary node
        StringBuilder sb = new StringBuilder(256);
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
        
        // Now we have the optional part.  Check for the existence of the scripts folder
        xpath = scriptsChildName;
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
            this.scriptsFolderNodeRef = null;
        }
        else
        {
            // we have the scripts folder noderef
            this.scriptsFolderNodeRef = nodeRefs.get(0);
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
        if (scriptsFolderNodeRef == null)
        {
            // create it
            createFolder();
            
            // import the content
            try
            {
               authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
               
               importContent();
            }
            finally
            {
               authenticationComponent.clearCurrentSecurityContext();
            }
            
            msg = I18NUtil.getMessage(MSG_CREATED, scriptsFolderNodeRef);
        }
        else
        {
            // it already exists
            msg = I18NUtil.getMessage(MSG_EXISTS, scriptsFolderNodeRef);
        }
        // done
        return msg;
    }
    
    private void createFolder()
    {
        // get required properties
        String scriptsChildName = configuration.getProperty(PROPERTY_SCRIPTS_FOLDER_CHILDNAME);
        if (scriptsChildName == null)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SCRIPTS_FOLDER_CHILDNAME + "' is not present");
        }
        
        String folderName = messageSource.getMessage(
                PROPERTY_SCRIPTS_FOLDER_NAME,
                null,
                I18NUtil.getLocale());
        if (folderName == null || folderName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SCRIPTS_FOLDER_NAME + "' is not present");
        }

        String folderDescription = messageSource.getMessage(
                PROPERTY_SCRIPTS_FOLDER_DESCRIPTION,
                null,
                I18NUtil.getLocale());
        if (folderDescription == null || folderDescription.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SCRIPTS_FOLDER_DESCRIPTION + "' is not present");
        }

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, folderName);
        properties.put(ContentModel.PROP_TITLE, folderName);
        properties.put(ContentModel.PROP_DESCRIPTION, folderDescription);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(
                dictionaryNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.resolveToQName(namespaceService, scriptsChildName),
                ContentModel.TYPE_FOLDER,
                properties);
        scriptsFolderNodeRef = childAssocRef.getChildRef();
        
        // finally add the required aspects
        nodeService.addAspect(scriptsFolderNodeRef, ApplicationModel.ASPECT_UIFACETS, null);
    }
    
    private void importContent() throws IOException
    {
        // import the content
        ClassPathResource acpResource = new ClassPathResource(this.scriptsACP);
        ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(acpResource.getFile(), null);
        Location importLocation = new Location(this.scriptsFolderNodeRef);
        importerService.importView(acpHandler, importLocation, null, null);
    }
}
