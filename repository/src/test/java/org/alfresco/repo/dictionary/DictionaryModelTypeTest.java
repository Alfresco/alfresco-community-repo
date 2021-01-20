/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.UserTransaction;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

/**
 * Dictionary model type unit test
 * 
 * @author Roy Wetherall, janv
 */
@Category({BaseSpringTestsCategory.class, LuceneTests.class})
@Transactional
public class DictionaryModelTypeTest extends BaseSpringTest
{
    /** QNames of the test models */
    
    private static final QName TEST_MODEL_ONE = QName.createQName("{http://www.alfresco.org/test/testmodel1/1.0}testModelOne");
    private static final QName TEST_MODEL_TWO = QName.createQName("{http://www.alfresco.org/test/testmodel2/1.0}testModelTwo");
    private static final QName TEST_MODEL_THREE = QName.createQName("{http://www.alfresco.org/test/testmodel3/1.0}testModelThree");
    
    /** Test model XMLs */
    
    public static final String MODEL_ONE_XML = 
        "<model name='test1:testModelOne' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model one</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel1/1.0' prefix='test1'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='test1:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test1:prop1'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MODEL_ONE_MODIFIED_XML = 
        "<model name='test1:testModelOne' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model one (updated)</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.1</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel1/1.0' prefix='test1'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='test1:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test1:prop1'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name='test1:prop2'>" +
        "              <type>d:boolean</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MODEL_ONE_MODIFIED2_XML = 
        "<model name='test1:testModelOne' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model one (updated 2)</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.2</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel1/1.0' prefix='test1'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='test1:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test1:prop1'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name='test1:prop99'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MODEL_TWO_XML = 
        "<model name='test2:testModelTwo' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model two</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2010-01-13</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel2/1.0' prefix='test2'/>" +
        "   </namespaces>" +
        
        "   <constraints>" +
        "      <constraint name='test2:con1' type='LIST'>" +
        "         <parameter name='allowedValues'>" +
        "            <list>" +
        "               <value>alfresco</value>" +
        "               <value>file</value>" +
        "            </list>" +
        "         </parameter>" +
        "      </constraint>" +
        "   </constraints>" +
        
        "   <types>" +
        
        "      <type name='test2:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test2:prop2'>" +
        "              <type>d:text</type>" +
        "              <constraints>" +
        "                 <constraint ref='test2:con1'/>" +
        "              </constraints>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MODEL_TWO_INVALID_XML = 
        "<model name='test2:testModelTwo' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model two</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2010-01-14</published>" +
        "   <version>1.1</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel2/1.0' prefix='test2'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='test2:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test2:prop2'>" +
        "              <type>d:text</type>" +
        "              <constraints>" +
        "                 <constraint ref='test2:con1'/>" +
        "              </constraints>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MODEL_TWO_MODIFIED_XML = 
        "<model name='test2:testModelTwo' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model two - modified</description>" +
        "   <author>Alfresco - modified</author>" +
        "   <published>2010-01-14</published>" +
        "   <version>1.1</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel2/1.0' prefix='test2'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='test2:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test2:prop2'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MODEL_THREE_XML = 
            "<model name='test3:testModelThree' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
            
            "   <description>Test model three</description>" +
            "   <author>Alfresco</author>" +
            "   <published>2005-05-30</published>" +
            "   <version>1.0</version>" +
            
            "   <imports>" +
            "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
            "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
            "   </imports>" +
            
            "   <namespaces>" +
            "      <namespace uri='http://www.alfresco.org/test/testmodel3/1.0' prefix='test3'/>" +
            "   </namespaces>" +
            
            "   <types>" +
            
            "      <type name='test3:base'>" +
            "        <title>Base</title>" +
            "        <description>The Base Type</description>" +
            "        <parent>cm:content</parent>" +
            "        <properties>" +
            "           <property name='test3:prop1'>" +
            "              <type>d:text</type>" +
            "              <mandatory enforced='false'>true</mandatory>" +
            "           </property>" +
            "        </properties>" +
            "      </type>" +
            
            "      <type name='test3:base-override'>" +
            "        <title>Base</title>" +
            "        <description>The Base Type</description>" +
            "        <parent>test3:base</parent>" +
            "        <overrides>" +
            "           <property name='test3:prop1'>" +
            "              <mandatory enforced='true'>true</mandatory>" +
            "           </property>" +
            "        </overrides>" +
            "      </type>" +
            
            "   </types>" +
            
            "</model>";
    
    /** Services used in tests */
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private CheckOutCheckInService cociService;
    private DictionaryDAO dictionaryDAO;
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ContentService contentService;
    private MutableAuthenticationService authenticationService;
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private ActionService actionService;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;

    @Before
    public void before() throws Exception
    {
        TestTransaction.flagForCommit();
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");

        // Authenticate as the admin user for dictionaryModelType
        // "System" user should also be allowed to create models
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Create the store and get the root node
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.storeRef);

        // Get the required services
        this.dictionaryService = (DictionaryService)this.applicationContext.getBean("dictionaryService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("namespaceService");
        this.cociService = (CheckOutCheckInService)this.applicationContext.getBean("checkOutCheckInService");
        this.dictionaryDAO = (DictionaryDAO)this.applicationContext.getBean("dictionaryDAO");
        this.nodeService = (NodeService)this.applicationContext.getBean("NodeService");
        this.policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");

        TenantAdminService tenantAdminService = (TenantAdminService)this.applicationContext.getBean("tenantAdminService");
        MessageService messageService = (MessageService)this.applicationContext.getBean("messageService");

        TestTransaction.end();
        TestTransaction.start();
        TestTransaction.flagForCommit();

        DictionaryRepositoryBootstrap bootstrap = new DictionaryRepositoryBootstrap();
        bootstrap.setContentService(this.contentService);
        bootstrap.setDictionaryDAO(this.dictionaryDAO);
        bootstrap.setTransactionService(this.transactionService);
        bootstrap.setTenantAdminService(tenantAdminService);
        bootstrap.setNodeService(this.nodeService);
        bootstrap.setNamespaceService(this.namespaceService);
        bootstrap.setMessageService(messageService);
        bootstrap.setPolicyComponent(policyComponent);

        RepositoryLocation location = new RepositoryLocation();
        location.setStoreProtocol(this.storeRef.getProtocol());
        location.setStoreId(this.storeRef.getIdentifier());
        location.setQueryLanguage(RepositoryLocation.LANGUAGE_PATH);
        // NOTE: we are not setting the path for now .. in doing so we are searching the root node only

        List<RepositoryLocation> locations = new ArrayList<RepositoryLocation>();
        locations.add(location);

        bootstrap.setRepositoryModelsLocations(locations);

        // register with dictionary service
        bootstrap.register();

        TestTransaction.end();
        TestTransaction.start();
    }

    @After
    public void after() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /**
     * Test the creation of dictionary model nodes
     */
    @Test
    public void testCreateAndUpdateDictionaryModelNodeContent() throws Exception
    {
        // just to make sure we don't regress ACE-5852, some tests should run as System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        try
        {
            // Check that the model has not yet been loaded into the dictionary
            this.dictionaryService.getModel(TEST_MODEL_ONE);
            fail("This model has not yet been loaded into the dictionary service");
        }
        catch (DictionaryException exception)
        {
            // We expect this exception
        }

        // Check that the namespace is not yet in the namespace service
        String uri = this.namespaceService.getNamespaceURI("test1");
        assertNull(uri);
        
        // Create a model node
        final NodeRef modelNode = transactionService.getRetryingTransactionHelper().doInTransaction(() ->
        {
            PropertyMap properties = new PropertyMap(1);
            properties.put(ContentModel.PROP_MODEL_ACTIVE, true);
            NodeRef model = this.nodeService.createNode(
                    this.rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                    ContentModel.TYPE_DICTIONARY_MODEL,
                    properties).getChildRef();
            assertNotNull(model);

            // Add the model content to the model node
            ContentWriter contentWriter = this.contentService.getWriter(model, ContentModel.PROP_CONTENT, true);
            contentWriter.setEncoding("UTF-8");
            contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
            contentWriter.putContent(MODEL_ONE_XML);
            return model;
        }, false, true);

        final NodeRef workingCopy = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                // Check that the namespace is in the namespace service
                String uri = namespaceService.getNamespaceURI("test1");
                assertNotNull(uri);
                
                // Check that the meta data has been extracted from the model
                assertEquals(QName.createQName("{http://www.alfresco.org/test/testmodel1/1.0}testModelOne"), 
                             DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_NAME));
                assertEquals("Test model one", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_DESCRIPTION));
                assertEquals("Alfresco", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_AUTHOR));
                //System.out.println(this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_PUBLISHED_DATE));
                assertEquals("1.0", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_VERSION));
                
                // Check that the model is now available from the dictionary
                ModelDefinition modelDefinition2 = DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE);
                assertNotNull(modelDefinition2);
                assertEquals("Test model one", modelDefinition2.getDescription(DictionaryModelTypeTest.this.dictionaryService));
                
                // Check that the namespace has been added to the namespace service
                String uri2 = DictionaryModelTypeTest.this.namespaceService.getNamespaceURI("test1");
                assertEquals(uri2, "http://www.alfresco.org/test/testmodel1/1.0");
                
                // Lets check the node out and update the content
                NodeRef workingCopy = DictionaryModelTypeTest.this.cociService.checkout(modelNode);
                ContentWriter contentWriter2 = DictionaryModelTypeTest.this.contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent(MODEL_ONE_MODIFIED_XML);
                
                return workingCopy;
            }
        }, false, true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Check that the policy has not been fired since we have updated a working copy
                assertEquals("1.0", DictionaryModelTypeTest.this.nodeService.getProperty(workingCopy, ContentModel.PROP_MODEL_VERSION));
                
                // Check-in the model change
                DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                return null;
            }
        }, false, true);
   
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Now check that the model has been updated
                assertEquals("1.1", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_VERSION));
                return null;
            }
        }, false, true);
        
        // create node using new type
        final NodeRef node1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                NodeRef node = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/model/system/1.0", "node1"),
                        QName.createQName("http://www.alfresco.org/test/testmodel1/1.0", "base"),
                        null).getChildRef(); 
                assertNotNull(node);
                return node;
            }
        }, false, true);
        
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                    return null;
                }
            }, false, true);
            
            fail("Unexpected - should not be able to delete model");
        }
        catch (AlfrescoRuntimeException are)
        {
            // expected
        }
        
        // delete node
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                nodeService.deleteNode(node1);
                return null;
            }
        }, false, true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                return null;
            }
        }, false, true);
    }
    
    @Test
    public void testUpdateDictionaryModelPropertyDelete() throws Exception
    {
        // just to make sure we don't regress ACE-5852, some tests should run as System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        try
        {
            // Check that the model has not yet been loaded into the dictionary
            this.dictionaryService.getModel(TEST_MODEL_ONE);
            fail("This model has not yet been loaded into the dictionary service");
        }
        catch (DictionaryException exception)
        {
            // We expect this exception
        }
        
        // Check that the namespace is not yet in the namespace service
        String uri = this.namespaceService.getNamespaceURI("test1");
        assertNull(uri);

        // Create a model node
        PropertyMap properties = new PropertyMap(1);
        properties.put(ContentModel.PROP_MODEL_ACTIVE, true);
        final NodeRef modelNode = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                properties).getChildRef(); 
        assertNotNull(modelNode);
        
        // Add the model content to the model node
        ContentWriter contentWriter = this.contentService.getWriter(modelNode, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        contentWriter.putContent(MODEL_ONE_MODIFIED_XML);
        
        // End the transaction to force update
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // create node using new type
        final NodeRef node1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                // Check that the namespace is in the namespace service
                String uri = namespaceService.getNamespaceURI("test1");
                assertNotNull(uri);
                
                // Create a model node
                PropertyMap properties = new PropertyMap(1);
                properties.put(QName.createQName("http://www.alfresco.org/test/testmodel1/1.0", "prop2"), "false"); // boolean
                
                NodeRef node = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.ALFRESCO_URI, "node1"),
                        QName.createQName("http://www.alfresco.org/test/testmodel1/1.0", "base"),
                        properties).getChildRef();
                assertNotNull(node);
                return node;
            }
        }, false, true);
        
        final NodeRef workingCopy = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                // Update model
                NodeRef workingCopy = DictionaryModelTypeTest.this.cociService.checkout(modelNode);
                ContentWriter contentWriter2 = DictionaryModelTypeTest.this.contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent(MODEL_ONE_MODIFIED2_XML);
                
                return workingCopy;
            }
        }, false, true);
        
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    // Check that the policy has not been fired since we have updated a working copy
                    assertEquals("1.1", DictionaryModelTypeTest.this.nodeService.getProperty(workingCopy, ContentModel.PROP_MODEL_VERSION));
                    
                    // Check-in the model change
                    DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                    return null;
                }
            });
            
            fail("Unexpected - should not be able to update model");
        }
        catch (AlfrescoRuntimeException are)
        {
            // expected
        }
        
        // delete node
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                nodeService.deleteNode(node1);
                return null;
            }
        }, false, true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Check that the policy has not been fired since we have updated a working copy
                assertEquals("1.1", DictionaryModelTypeTest.this.nodeService.getProperty(workingCopy, ContentModel.PROP_MODEL_VERSION));
                
                // Check-in the model change
                DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                return null;
            }
        }, false, true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Now check that the model has been updated
                assertEquals("1.2", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_VERSION));
                return null;
            }
        }, false, true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                return null;
            }
        }, false, true);
    }
    
    @Test
    public void testUpdateDictionaryModelConstraintDelete() throws Exception
    {
        try
        {
            // Check that the model has not yet been loaded into the dictionary
            this.dictionaryService.getModel(TEST_MODEL_TWO);
            fail("This model has not yet been loaded into the dictionary service");
        }
        catch (DictionaryException exception)
        {
            // We expect this exception
        }
        
        // Check that the namespace is not yet in the namespace service
        String uri = this.namespaceService.getNamespaceURI("test2");
        assertNull(uri);

        // Create a model node
        PropertyMap properties = new PropertyMap(1);
        properties.put(ContentModel.PROP_MODEL_ACTIVE, true);
        final NodeRef modelNode = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                properties).getChildRef(); 
        assertNotNull(modelNode);
        
        // Add the model content to the model node
        ContentWriter contentWriter = this.contentService.getWriter(modelNode, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        contentWriter.putContent(MODEL_TWO_XML);
        
        // End the transaction to force update
        TestTransaction.flagForCommit();
        TestTransaction.end();

        final NodeRef workingCopy = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
             // Check that the namespace is in the namespace service
                String uri = namespaceService.getNamespaceURI("test2");
                assertNotNull(uri);
                
                Collection<ConstraintDefinition> constraints = dictionaryService.getConstraints(TEST_MODEL_TWO, true);
                assertEquals(1, constraints.size());
                assertEquals("test2:con1", constraints.iterator().next().getName().getPrefixString());
                
                // Update model
                NodeRef workingCopy = DictionaryModelTypeTest.this.cociService.checkout(modelNode);
                ContentWriter contentWriter2 = DictionaryModelTypeTest.this.contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent(MODEL_TWO_INVALID_XML);
                
                return workingCopy;
            }
        });
        
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    // Check that the policy has not been fired since we have updated a working copy
                    assertEquals("1.0", DictionaryModelTypeTest.this.nodeService.getProperty(workingCopy, ContentModel.PROP_MODEL_VERSION));
                    
                    // Check-in the model change
                    DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                    return null;
                }
            });
            
            fail("Unexpected - should not be able to update model");
        }
        catch (AlfrescoRuntimeException are)
        {
            assertTrue(are.getMessage().contains("Failed to validate constraint delete"));
        }
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Check that the policy has not been fired since the previous update was invalid
                assertEquals("1.0", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_VERSION));
                
                // Update model
                ContentWriter contentWriter2 = DictionaryModelTypeTest.this.contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent(MODEL_TWO_MODIFIED_XML);
                
                // Check-in the model change
                DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Now check that the model has been updated
                
                Collection<ConstraintDefinition> constraints = dictionaryService.getConstraints(TEST_MODEL_TWO, true);
                assertEquals(0, constraints.size());
                
                assertEquals("1.1", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_VERSION));
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                return null;
            }
        });
    }
    
    @Test
    public void testIsActiveFlagAndDelete() throws Exception
    {
        try
        {
            // Check that the model has not yet been loaded into the dictionary
            this.dictionaryService.getModel(TEST_MODEL_ONE);
            fail("This model has not yet been loaded into the dictionary service");
        }
        catch (DictionaryException exception)
        {
            // We expect this exception
        }

        // Create a model node
        PropertyMap properties = new PropertyMap(1);
        final NodeRef modelNode = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                properties).getChildRef();
        assertNotNull(modelNode);
        
        // Add the model content to the model node
        ContentWriter contentWriter = this.contentService.getWriter(modelNode, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        contentWriter.putContent(MODEL_ONE_XML);
        
        // End the transaction to force update
        TestTransaction.flagForCommit();
        TestTransaction.end();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // The model should not yet be loaded
                try
                {
                    // Check that the model has not yet been loaded into the dictionary
                    DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE);
                    fail("This model has not yet been loaded into the dictionary service");
                }
                catch (DictionaryException exception)
                {
                    // We expect this exception
                }
                
                // Set the isActive flag
                DictionaryModelTypeTest.this.nodeService.setProperty(modelNode, ContentModel.PROP_MODEL_ACTIVE, true);
                
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // The model should now be loaded
                assertNotNull(DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE));
                
                // Set the isActive flag
                DictionaryModelTypeTest.this.nodeService.setProperty(modelNode, ContentModel.PROP_MODEL_ACTIVE, false);
                
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // The model should not be loaded
                try
                {
                    // Check that the model has not yet been loaded into the dictionary
                    DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE);
                    fail("This model has not yet been loaded into the dictionary service");
                }
                catch (DictionaryException exception)
                {
                    // We expect this exception
                }
                
                // Set the isActive flag
                DictionaryModelTypeTest.this.nodeService.setProperty(modelNode, ContentModel.PROP_MODEL_ACTIVE, true);
                
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // The model should now be loaded
                assertNotNull(DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE));
                
                // Delete the model
                DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // The model should not be loaded
                try
                {
                    // Check that the model has not yet been loaded into the dictionary
                    DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE);
                    fail("This model has not yet been loaded into the dictionary service");
                }
                catch (DictionaryException exception)
                {
                    // We expect this exception
                }
                
                return null;
            }
        });
    }
    
    /**
     * Test for MNT-11653
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testOverrideMandatoryProperty() throws Exception
    {
        try
        {
            // Check that the model has not yet been loaded into the dictionary
            this.dictionaryService.getModel(TEST_MODEL_THREE);
            fail("This model has not yet been loaded into the dictionary service");
        }
        catch (DictionaryException exception)
        {
            // We expect this exception
        }
        
        // Check that the namespace is not yet in the namespace service
        String uri = this.namespaceService.getNamespaceURI("test3");
        assertNull(uri);
        
        // Create a model node
        PropertyMap properties = new PropertyMap(1);
        properties.put(ContentModel.PROP_MODEL_ACTIVE, true);

        final NodeRef modelNode = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                properties).getChildRef(); 
        assertNotNull(modelNode);
        
        // Add the model content to the model node
        ContentWriter contentWriter = this.contentService.getWriter(modelNode, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        contentWriter.putContent(MODEL_THREE_XML);
        
        // End the transaction to force update

        TestTransaction.flagForCommit();
        TestTransaction.end();

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // Validate the model
                ContentReader reader = DictionaryModelTypeTest.this.contentService.getReader(modelNode, ContentModel.PROP_CONTENT);
                Source transferReportSource = new StreamSource(reader.getContentInputStream());
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final String schemaLocation = "classpath:alfresco/model/modelSchema.xsd";
                Schema schema = sf.newSchema(ResourceUtils.getURL(schemaLocation));
                Validator validator = schema.newValidator();
                validator.validate(transferReportSource);
                return null;
            }
        });
        
        // create node using new type
        final NodeRef node1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(QName.createQName("http://www.alfresco.org/test/testmodel3/1.0", "prop1"), "testvalue");
                NodeRef node = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/model/system/1.0", "node1"),
                        QName.createQName("http://www.alfresco.org/test/testmodel3/1.0", "base-override"),
                        properties).getChildRef(); 
                assertNotNull(node);
                return node;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // Delete the node
                DictionaryModelTypeTest.this.nodeService.deleteNode(node1);
                return null;
            }
        });
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {      
                // Delete the model
                DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                return null;
            }
        });
    }

    /* MNT-15345 test */
    @Test
    public void testImportingSameNamespaceFails() throws Exception
    {
        // just to make sure we don't regress ACE-5852, some tests should run as System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        // Create model
        final NodeRef modelNode = createActiveModel("dictionary/testModel.xml");
        TestTransaction.flagForCommit();
        TestTransaction.end();

        //  add second model to introduce self referencing dependency
        TestTransaction.start();
        PropertyMap properties = new PropertyMap(1);
        properties.put(ContentModel.PROP_MODEL_ACTIVE, true);
        final NodeRef modelNode2 = this.nodeService.createNode(
                 this.rootNodeRef,
                 ContentModel.ASSOC_CHILDREN,
                 QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                 ContentModel.TYPE_DICTIONARY_MODEL,
                 properties).getChildRef();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Exception {
                        ContentWriter contentWriter = DictionaryModelTypeTest.this.contentService.getWriter(modelNode2, ContentModel.PROP_CONTENT, true);
                        contentWriter.putContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("dictionary/testModel2.xml"));
                        return null;
                    }
            });
            fail("Validation should fail as a circular dependency was introduced");
        } catch(AlfrescoRuntimeException e)
        {
            assertTrue(e.getCause().getMessage().contains("URI mage.model cannot be imported as it is already contained in the model's namespaces"));
        }

        //delete model
        finally
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<Object>() {
                        public Object execute() throws Exception {
                            // Delete the model
                            DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                            return null;
                        }
                    });
        }
    }

    /* MNT-15345 test */
    @Test(expected=AlfrescoRuntimeException.class)
    public void testImportingSameNamespaceFailsWithinSingleModel() throws Exception
    {
        // Create model
        PropertyMap properties = new PropertyMap(1);
        properties.put(ContentModel.PROP_MODEL_ACTIVE, true);
        final NodeRef modelNode = this.nodeService.createNode(
                 this.rootNodeRef,
                 ContentModel.ASSOC_CHILDREN,
                 QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                 ContentModel.TYPE_DICTIONARY_MODEL,
                 properties).getChildRef(); 
        assertNotNull(modelNode);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // update model to introduce self referencing dependency
        try
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(1);
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Exception {

                        ContentWriter contentWriter = contentService.getWriter(modelNode, ContentModel.PROP_CONTENT, true);
                        contentWriter.setEncoding("UTF-8");
                        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
                        contentWriter.putContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("dictionary/modelWithCurrentNamespaceImported.xml"));
                        return null;
                    }
            }, false, true);
            fail("Validation should fail as a circular dependency was introduced");
        } catch(AlfrescoRuntimeException e)
        {
            assertTrue(e.getCause().getMessage().contains("URI http://www.alfresco.org/model/dictionary/1.0 cannot be imported as it is already contained in the model's namespaces"));
            throw e;
        }

        //delete model
        finally
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<Object>() {
                        public Object execute() throws Exception {
                            // Delete the model
                            DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                            return null;
                        }
                    }, false, true);
        }
    }

    @Test
    public void testCircularDependencyDetected() throws Exception
    {
        // Create modelA
        final NodeRef modelANode = createActiveModel("dictionary/modelA.xml");
        TestTransaction.flagForCommit();
        TestTransaction.end();

        //Create modelB 
        TestTransaction.start();
        final NodeRef modelBNode = createActiveModel("dictionary/modelB.xml");
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // update model A to introduce circular dependency
        try
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(3);
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Exception {
                        ContentWriter contentWriter = DictionaryModelTypeTest.this.contentService.getWriter(modelANode, ContentModel.PROP_CONTENT, true);
                        contentWriter.putContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("dictionary/modelAupdated.xml"));
                        return null;
                    }
            });
            fail("Validation should fail as a circular dependency was introduced");
        } catch(AlfrescoRuntimeException e)
        {
            assertTrue(e.getMessage().contains("Failed to validate model update - found circular dependency. You can't set parent model-B as it's model already depends on prefixA:model-A"));
        }
        
        // update model A to introduce circular dependency - also add a second namespace
        try
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(3);
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    public Void execute() throws Exception {
                        ContentWriter contentWriter = DictionaryModelTypeTest.this.contentService.getWriter(modelANode, ContentModel.PROP_CONTENT, true);
                        contentWriter.putContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("dictionary/modelAupdatedWithSecondNamespace.xml"));
                        return null;
                    }
            });
            fail("Validation should fail as a circular dependency was introduced");
        } catch(AlfrescoRuntimeException e)
        {
            assertTrue(e.getMessage().contains("Failed to validate model update - found circular dependency. You can't set parent model-B as it's model already depends on prefixA:model-A"));
        }

        //delete both created models
        finally {
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<Object>() {
                        public Object execute() throws Exception {
                            // Delete the model
                            DictionaryModelTypeTest.this.nodeService.deleteNode(modelANode);
                            DictionaryModelTypeTest.this.nodeService.deleteNode(modelBNode);
                            return null;
                        }
                    });
        }
    }

    private NodeRef createActiveModel(String contentFile)
    {
        PropertyMap properties = new PropertyMap(1);
        properties.put(ContentModel.PROP_MODEL_ACTIVE, true);
        NodeRef modelNode = this.nodeService.createNode(
                 this.rootNodeRef,
                 ContentModel.ASSOC_CHILDREN,
                 QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryModels"),
                 ContentModel.TYPE_DICTIONARY_MODEL,
                 properties).getChildRef();
         assertNotNull(modelNode);

         ContentWriter contentWriter = this.contentService.getWriter(modelNode, ContentModel.PROP_CONTENT, true);
         contentWriter.setEncoding("UTF-8");
         contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
         contentWriter.putContent(Thread.currentThread().getContextClassLoader().getResourceAsStream(contentFile));
         return modelNode;
    }
}
