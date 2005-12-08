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
package org.alfresco.example.webservice;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.alfresco.example.webservice.authentication.AuthenticationResult;
import org.alfresco.example.webservice.authentication.AuthenticationServiceLocator;
import org.alfresco.example.webservice.authentication.AuthenticationServiceSoapBindingStub;
import org.alfresco.example.webservice.content.Content;
import org.alfresco.example.webservice.content.ContentServiceLocator;
import org.alfresco.example.webservice.content.ContentServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.RepositoryServiceLocator;
import org.alfresco.example.webservice.repository.RepositoryServiceSoapBindingStub;
import org.alfresco.example.webservice.repository.UpdateResult;
import org.alfresco.example.webservice.types.CML;
import org.alfresco.example.webservice.types.CMLCreate;
import org.alfresco.example.webservice.types.ContentFormat;
import org.alfresco.example.webservice.types.NamedValue;
import org.alfresco.example.webservice.types.ParentReference;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.example.webservice.types.Store;
import org.alfresco.example.webservice.types.StoreEnum;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseTest;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for all web service system tests that need to authenticate. The
 * setUp method calls the AuthenticationService and authenticates as
 * admin/admin, the returned ticket is then stored in
 * <code>TicketHolder.ticket</code> so that all subclass implementations can
 * use it to call other services.
 * 
 * @see junit.framework.TestCase#setUp()
 * @author gavinc
 */
public abstract class BaseWebServiceSystemTest extends BaseTest
{
    private static Log logger = LogFactory
            .getLog(BaseWebServiceSystemTest.class);

    protected static final String USERNAME = "admin";
    protected static final String PASSWORD = "admin";
    
    private Properties properties;
    private Store store;
    private Reference rootNodeReference;
    private Reference contentReference;
    
    protected RepositoryServiceSoapBindingStub repositoryService;
    protected ContentServiceSoapBindingStub contentService;

    public BaseWebServiceSystemTest()
    {
        try
        {
            EngineConfiguration config = new FileProvider(getResourcesDir(), "client-deploy.wsdd");
            this.contentService = (ContentServiceSoapBindingStub)new ContentServiceLocator(config).getContentService();
            assertNotNull(this.contentService);
            this.contentService.setTimeout(60000);
            
            this.repositoryService = (RepositoryServiceSoapBindingStub)new RepositoryServiceLocator(config).getRepositoryService();
            assertNotNull(this.repositoryService);
            this.repositoryService.setTimeout(60000);
        }
        catch (Exception e)
        {
            fail("Could not instantiate the content service" + e.toString());
        }
    }
    
    /**
     * Calls the AuthenticationService to retrieve a ticket for all tests to
     * use.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        AuthenticationServiceSoapBindingStub authSvc = null;
        try
        {
            authSvc = (AuthenticationServiceSoapBindingStub) new AuthenticationServiceLocator()
                    .getAuthenticationService();
        } catch (ServiceException jre)
        {
            if (jre.getLinkedCause() != null)
            {
                jre.getLinkedCause().printStackTrace();
            }

            throw new AssertionFailedError("JAX-RPC ServiceException caught: "
                    + jre);
        }
        assertNotNull("authSvc is null", authSvc);

        // Time out after a minute
        authSvc.setTimeout(60000);

        // call the authenticate method and retrieve the ticket
        AuthenticationResult result = authSvc.startSession(USERNAME, PASSWORD);
        assertNotNull("result is null", result);

        String ticket = result.getTicket();
        assertNotNull("ticket is null", ticket);

        TicketHolder.ticket = ticket;
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved and stored ticket: " + TicketHolder.ticket);
        }
    }

    protected Store getStore()
    {
        if (this.store == null)
        {
            String strStoreRef = getProperties().getProperty(
                    WebServiceBootstrapSystemTest.PROP_STORE_REF);
            StoreRef storeRef = new StoreRef(strStoreRef);
            this.store = new Store(StoreEnum.fromValue(storeRef.getProtocol()),
                    storeRef.getIdentifier());
        }
        return this.store;
    }

    protected Reference getRootNodeReference()
    {
        if (this.rootNodeReference == null)
        {
            String strNodeRef = getProperties().getProperty(
                    WebServiceBootstrapSystemTest.PROP_ROOT_NODE_REF);
            NodeRef rootNodeRef = new NodeRef(strNodeRef);
            this.rootNodeReference = new Reference(getStore(), rootNodeRef
                    .getId(), null);
        }
        return this.rootNodeReference;
    }
    
    protected Reference getContentReference()
    {
        if (this.contentReference == null)
        {
            String strNodeRef = getProperties().getProperty(WebServiceBootstrapSystemTest.PROP_CONTENT_NODE_REF);
            NodeRef nodeRef = new NodeRef(strNodeRef);
            this.contentReference = new Reference(getStore(), nodeRef.getId(), null);
        }
        return this.contentReference;
    }
    
    protected ParentReference getFolderParentReference(QName assocName)
    {
        NodeRef folderNodeRef = getFolderNodeRef();
        ParentReference parentReference = new ParentReference();
        parentReference.setStore(getStore());
        parentReference.setUuid(folderNodeRef.getId());
        parentReference.setAssociationType(ContentModel.ASSOC_CONTAINS.toString());
        parentReference.setChildName(assocName.toString());
        return parentReference;
    }

    protected Reference createContentAtRoot(String name, String contentValue) throws Exception
    {
        ParentReference parentRef = new ParentReference();
        parentRef.setStore(getStore());
        parentRef.setUuid(getRootNodeReference().getUuid());
        parentRef.setAssociationType(ContentModel.ASSOC_CHILDREN.toString());
        parentRef.setChildName(ContentModel.ASSOC_CHILDREN.toString());
        
        NamedValue[] properties = new NamedValue[]{new NamedValue(ContentModel.PROP_NAME.toString(), name)};
        CMLCreate create = new CMLCreate("1", parentRef, ContentModel.TYPE_CONTENT.toString(), properties);
        CML cml = new CML();
        cml.setCreate(new CMLCreate[]{create});
        UpdateResult[] result = this.repositoryService.update(cml);     
        
        Reference newContentNode = result[0].getDestination();
        
        Content content = this.contentService.write(newContentNode, ContentModel.PROP_CONTENT.toString(), contentValue.getBytes(), new ContentFormat("text/plain", "UTF-8"));
                
        assertNotNull(content);
        assertNotNull(content.getFormat());
        assertEquals("text/plain", content.getFormat().getMimetype());
        
        return content.getNode();
    }    
    
    /**
     * Get the content from the download servlet 
     * 
     * @param url
     * @return
     */
    protected String getContentAsString(String strUrl) throws Exception
    {
        // Add the ticket to the url
        strUrl += "?ticket=" + TicketHolder.ticket;
        
        StringBuilder readContent = new StringBuilder();
        URL url = new URL(strUrl);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        int read = is.read();
        while (read != -1)
        {
           readContent.append((char)read);
           read = is.read();
        }
        
        return readContent.toString();
    }
    
    protected Predicate convertToPredicate(Reference reference)
    {
        Predicate predicate = new Predicate();
        predicate.setNodes(new Reference[] {reference});
        return predicate;
    }

    private Properties getProperties()
    {
        if (this.properties == null)
        {
            this.properties = WebServiceBootstrapSystemTest
                    .getBootstrapProperties();
        }
        return this.properties;
    }
    
    private NodeRef getFolderNodeRef()
    {
        String strNodeRef = getProperties().getProperty(WebServiceBootstrapSystemTest.PROP_FOLDER_NODE_REF);
        return new NodeRef(strNodeRef);
    }
}
