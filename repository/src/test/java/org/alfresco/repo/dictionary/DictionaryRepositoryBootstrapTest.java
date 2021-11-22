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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

@Category(BaseSpringTestsCategory.class)
@Transactional
public class DictionaryRepositoryBootstrapTest extends BaseSpringTest
{
    public static final String TEMPLATE_MODEL_XML = 
        "<model name={0} xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>{1}</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "      {2} " +
        "   </imports>" +
    
        "   <namespaces>" +
        "      <namespace uri={3} prefix={4}/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name={5}>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <properties>" +
        "           <property name={6}>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    public static final String MESSAGES_KEY = "my_bootstrap_test";
    public static final String MESSAGES_VALUE = "My Message";
    public static final String MESSAGES_VALUE_FR = "Mon message";
    public static final String FOLDERNAME_MODELS = "models";
    public static final String FOLDERNAME_MESSAGES = "messages";
    public static final String BUNDLENAME_MESSAGES = "testBootstap";
    public static final String FILENAME_MESSAGES_EXT = ".properties";

    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;

    /** The bootstrap service */
    private DictionaryRepositoryBootstrap bootstrap;
    
    /** The dictionary DAO */
    private DictionaryDAO dictionaryDAO;
    
    /** The transaction service */
    private TransactionService transactionService;
    
    /** The tenant deployer service */
    private TenantAdminService tenantAdminService;
    
    /** The namespace service */
    private NamespaceService namespaceService;
    
    /** The message service */
    private MessageService messageService;
    
    private PolicyComponent policyComponent;
    private ContentService contentService;
    private NodeService nodeService;
    private MutableAuthenticationService authenticationService;
    protected AuthenticationComponent authenticationComponent;

    private UserTransaction txn;
    private StoreRef storeRef;
    private NodeRef rootModelsNodeRef;
    private NodeRef rootMessagesNodeRef;

    @Before
    public void before() throws Exception
    {
        // Get the behaviour filter
        this.behaviourFilter = (BehaviourFilter)this.applicationContext.getBean("policyBehaviourFilter");

        this.authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("authenticationService");
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
        this.dictionaryDAO = (DictionaryDAO)this.applicationContext.getBean("dictionaryDAO");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.tenantAdminService = (TenantAdminService)this.applicationContext.getBean("tenantAdminService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("namespaceService");
        this.messageService = (MessageService)this.applicationContext.getBean("messageService");
        this.policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");

        this.authenticationComponent = (AuthenticationComponent) this.applicationContext
                .getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();

        txn = transactionService.getUserTransaction();
        // Create the store in a separate transaction to run successfully on MS SQL Server
        txn.begin();

        // Create the store and get the root node
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());

        NodeRef rootNodeRef = this.nodeService.getRootNode(this.storeRef);
        this.rootModelsNodeRef = this.nodeService
                .createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, FOLDERNAME_MODELS), ContentModel.TYPE_FOLDER)
                .getChildRef();

        this.rootMessagesNodeRef = this.nodeService
                .createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, FOLDERNAME_MESSAGES), ContentModel.TYPE_FOLDER)
                .getChildRef();

        txn.commit();

        txn = transactionService.getUserTransaction();
        txn.begin();

        this.bootstrap = new DictionaryRepositoryBootstrap();
        this.bootstrap.setContentService(this.contentService);
        this.bootstrap.setDictionaryDAO(this.dictionaryDAO);
        this.bootstrap.setTransactionService(this.transactionService);
        this.bootstrap.setTenantAdminService(this.tenantAdminService); 
        this.bootstrap.setNodeService(this.nodeService);
        this.bootstrap.setNamespaceService(this.namespaceService);
        this.bootstrap.setMessageService(this.messageService);
        this.bootstrap.setPolicyComponent(this.policyComponent);

        RepositoryLocation modelsLocation = new RepositoryLocation(this.storeRef,
                this.nodeService.getPath(rootModelsNodeRef).toPrefixString(namespaceService), RepositoryLocation.LANGUAGE_PATH);
        RepositoryLocation messagesLocation = new RepositoryLocation(this.storeRef,
                this.nodeService.getPath(rootMessagesNodeRef).toPrefixString(namespaceService), RepositoryLocation.LANGUAGE_PATH);

        List<RepositoryLocation> modelsLocations = new ArrayList<RepositoryLocation>();
        modelsLocations.add(modelsLocation);
        List<RepositoryLocation> messagesLocations = new ArrayList<RepositoryLocation>();
        messagesLocations.add(messagesLocation);

        this.bootstrap.setRepositoryModelsLocations(modelsLocations);
        this.bootstrap.setRepositoryMessagesLocations(messagesLocations);

        // register with dictionary service
        this.bootstrap.register();
        txn.commit();
    }

    @After
    public void after() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
    }
    
    /**
     * Test bootstrap
     */
    @Test
    public void testBootstrap() throws Exception
    {
        txn = transactionService.getUserTransaction();
        txn.begin();
        // turn the behaviour off for the model type
        this.behaviourFilter.disableBehaviour(ContentModel.TYPE_DICTIONARY_MODEL);
        createModelNode(
                "http://www.alfresco.org/model/test2DictionaryBootstrapFromRepo/1.0",
                "test2",
                "testModel2",
                " <import uri=\"http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0\" prefix=\"test1\"/> ",
                "Test model two",
                "base2",
                "prop2");
        createModelNode(
                "http://www.alfresco.org/model/test3DictionaryBootstrapFromRepo/1.0",
                "test3",
                "testModel3",
                " <import uri=\"http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0\" prefix=\"test1\"/> ",
                "Test model three",
                "base3",
                "prop3");
        createModelNode(
                "http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0",
                "test1",
                "testModel1",
                "",
                "Test model one",
                "base1",
                "prop1");

        // Create a message file for the default locale
        NodeRef messageNodeDefaultLoc = createMessagesNode(null, null);
        // Create a message file for the french locale
        createMessagesNode(Locale.FRANCE.toString(), MESSAGES_VALUE_FR);
        // Construct baseBundleName for validation
        String baseBundleName = storeRef.toString()
                + messageService.getBaseBundleName(nodeService.getPath(messageNodeDefaultLoc).toPrefixString(namespaceService));

        // Check that the model is not in the dictionary yet
        try
        {
            this.dictionaryDAO.getModel(
                    QName.createQName("http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0", "testModel1"));
            fail("The model should not be there.");
        }
        catch (DictionaryException exception)
        {
            // Ignore since we where expecting this
        }        
        
        // Now do the bootstrap
        this.bootstrap.init();
        
        // Check that the model is now there
        ModelDefinition modelDefinition1 = this.dictionaryDAO.getModel(
                QName.createQName("http://www.alfresco.org/model/test1DictionaryBootstrapFromRepo/1.0", "testModel1"));
        assertNotNull(modelDefinition1);
        ModelDefinition modelDefinition2 = this.dictionaryDAO.getModel(
                QName.createQName("http://www.alfresco.org/model/test2DictionaryBootstrapFromRepo/1.0", "testModel2"));
        assertNotNull(modelDefinition2);
        ModelDefinition modelDefinition3 = this.dictionaryDAO.getModel(
                QName.createQName("http://www.alfresco.org/model/test3DictionaryBootstrapFromRepo/1.0", "testModel3"));
        assertNotNull(modelDefinition3);

        // Check if the messages were registered correctly
        assertTrue("The message bundle should be registered", messageService.getRegisteredBundles().contains(baseBundleName));
        assertEquals("The default message value is not as expected", MESSAGES_VALUE, messageService.getMessage(MESSAGES_KEY));
        assertEquals("The message value in french is not as expected", MESSAGES_VALUE_FR,
                messageService.getMessage(MESSAGES_KEY, Locale.FRANCE));

        txn.commit();
    }

    /**
     * Create model node 
     * 
     * @param uri String
     * @param prefix String
     * @param modelLocalName String
     * @param importStatement String
     * @param description String
     * @param typeName String
     * @param propertyName String
     * @return NodeRef
     */
    private NodeRef createModelNode(
            String uri, 
            String prefix, 
            String modelLocalName, 
            String importStatement, 
            String description, 
            String typeName,
            String propertyName)
    {
        // Create a model node
        NodeRef model = this.nodeService.createNode(
                this.rootModelsNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}models"),
                ContentModel.TYPE_DICTIONARY_MODEL).getChildRef();
        ContentWriter contentWriter1 = this.contentService.getWriter(model, ContentModel.PROP_CONTENT, true);
        contentWriter1.setEncoding("UTF-8");
        contentWriter1.setMimetype(MimetypeMap.MIMETYPE_XML);
        String modelOne = getModelString(
                    uri,
                    prefix,
                    modelLocalName,
                    importStatement,
                    description,
                    typeName,
                    propertyName);        
        contentWriter1.putContent(modelOne);
        
        // activate the model
        nodeService.setProperty(model, ContentModel.PROP_MODEL_ACTIVE, new Boolean(true));
        
        return model;
    }
    
    /**
     * Create messages node
     * 
     * @return NodeRef
     */
    private NodeRef createMessagesNode(String locale, String localeValue)
    {
        String filename = BUNDLENAME_MESSAGES + FILENAME_MESSAGES_EXT;
        String messageValue = MESSAGES_VALUE;

        if (locale != null)
        {
            filename = BUNDLENAME_MESSAGES + "_" + locale + FILENAME_MESSAGES_EXT;
            messageValue = localeValue;
        }
        // Create a model node
        NodeRef messageNode = this.nodeService.createNode(
                this.rootMessagesNodeRef, 
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, filename), 
                ContentModel.TYPE_CONTENT,
                Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, filename)
                ).getChildRef();

        ContentWriter contentWriter = this.contentService.getWriter(messageNode, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        String messagesString = MESSAGES_KEY + "=" + messageValue;
        contentWriter.putContent(messagesString);

        return messageNode;
    }
    
    /**
     * 
     * Gets the model string 
     * 
     * @param uri String
     * @param prefix String
     * @param modelLocalName String
     * @param importStatement String
     * @param description String
     * @param typeName String
     * @param propertyName String
     * @return String
     */
    private String getModelString(
            String uri, 
            String prefix, 
            String modelLocalName, 
            String importStatement, 
            String description, 
            String typeName,
            String propertyName)
    {
        return MessageFormat.format( 
                TEMPLATE_MODEL_XML, 
                new Object[]{
                        "'" + prefix +":" + modelLocalName + "'",
                        description,
                        importStatement,
                        "'" + uri + "'",
                        "'" + prefix + "'",
                        "'" + prefix + ":" + typeName + "'",
                        "'" + prefix + ":" + propertyName + "'"});
    }
}
