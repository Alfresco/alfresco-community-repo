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
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;

/**
 * Ensures that the <b>email templates</b> are imported into the default folder.
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
public class EmailTemplatesContentPatch extends AbstractPatch
{
    private static final String MSG_CREATED = "patch.emailTemplatesContent.result";
    
    public static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    public static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    public static final String PROPERTY_EMAIL_TEMPLATES_CHILDNAME = "spaces.templates.email.childname";
    
    private ImporterBootstrap importerBootstrap;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private ImporterService importerService;
    private AuthenticationComponent authComponent;
    private NodeService nodeService;
    private MessageSource messageSource;
    
    protected Properties configuration;
    protected NodeRef emailTemplatesNodeRef;
    
    private String templatesACP;
    
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
    
    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setAuthenticationComponent(AuthenticationComponent authComponent)
    {
        this.authComponent = authComponent;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }
    
    /**
     * @param templatesACP    The templates ACP file to import.
     */
    public void setTemplatesACP(String templatesACP)
    {
        this.templatesACP = templatesACP;
    }

    /**
     * Ensure that required properties have been set
     */
    protected void checkRequiredProperties() throws Exception
    {
        if (importerBootstrap == null)
        {
            throw new PatchException("'importerBootstrap' property has not been set");
        }
        if (namespaceService == null)
        {
            throw new PatchException("'namespaceService' property has not been set");
        }
        if (searchService == null)
        {
            throw new PatchException("'searchService' property has not been set");
        }
        if (nodeService == null)
        {
            throw new PatchException("'nodeService' property has not been set");
        }
        if (importerService == null)
        {
           throw new PatchException("'importerService' property has not been set");
        }
        if (messageSource == null)
        {
            throw new PatchException("'messageSource' property has not been set");
        }
        if (templatesACP == null || templatesACP.length() == 0)
        {
           throw new PatchException("'templatesACP' property has not been set");
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
        String emailTemplatesChildName = configuration.getProperty(PROPERTY_EMAIL_TEMPLATES_CHILDNAME);
        if (emailTemplatesChildName == null || emailTemplatesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_EMAIL_TEMPLATES_CHILDNAME + "' is not present");
        }
        
        // build the search string to get the email templates node
        StringBuilder sb = new StringBuilder(128);
        sb.append("/").append(companyHomeChildName)
          .append("/").append(dictionaryChildName)
          .append("/").append(emailTemplatesChildName);
        String xpath = sb.toString();
        
        // get the templates node
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("Unable to locate Email Templates folder: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("Found too many Email Templates folder results: \n" +
                    "   root: " + storeRootNodeRef + "\n" +
                    "   xpath: " + xpath + "\n" +
                    "   results: " + nodeRefs);
        }
        this.emailTemplatesNodeRef = nodeRefs.get(0);
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // common properties must be set before we can continue
        checkRequiredProperties();
        
        setUp();
        
        // import the content
        try
        {
           authComponent.setCurrentUser(authComponent.getSystemUserName());
           
           importContent();
        }
        finally
        {
           authComponent.clearCurrentSecurityContext();
        }
        
        // output a message to describe the result
        return I18NUtil.getMessage(MSG_CREATED);
    }
    
    private void importContent() throws IOException
    {
        // import the content
        ClassPathResource acpResource = new ClassPathResource(this.templatesACP);
        ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(acpResource.getFile(), null);
        Location importLocation = new Location(this.emailTemplatesNodeRef);
        importerService.importView(acpHandler, importLocation, null, null);
    }
}
