/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
/*
 * Copyright (C) 2005 Jesper Steen M�ller
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
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Test of the ActionExecuter for extracting metadata.
 * 
 * @author Jesper Steen Møller
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class ContentMetadataExtracterTest extends BaseSpringTest
{
    protected static final String QUICK_TITLE = "The quick brown fox jumps over the lazy dog";
    protected static final String QUICK_DESCRIPTION = "Pangram, fox, dog, Gym class featuring a brown fox and lazy dog";
    protected static final String QUICK_CREATOR = "Nevin Nollop";

    private NodeService nodeService;
    private ContentService contentService;
    private MetadataExtracterRegistry registry;
    private TransactionService transactionService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;

    private ContentMetadataExtracter executer;

    private final static String ID = GUID.generate();

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        registry = (MetadataExtracterRegistry) applicationContext.getBean("metadataExtracterRegistry");
        transactionService = (TransactionService) this.applicationContext.getBean("transactionService");

        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();

        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();

        // Setup the content from the PDF test data
        ContentWriter cw = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        cw.setMimetype(MimetypeMap.MIMETYPE_PDF);
        cw.putContent(AbstractContentTransformerTest.loadQuickTestFile("pdf"));

        // Get the executer instance
        this.executer = (ContentMetadataExtracter) this.applicationContext.getBean("extract-metadata");
    }

    /**
     * Test execution of the extraction itself
     */
    @Test
    public void testFromBlanks() throws Exception
    {
        // Test that the action writes properties when they don't exist or are
        // unset

        // Get the old props
        Map<QName, Serializable> props = this.nodeService.getProperties(this.nodeRef);
        props.remove(ContentModel.PROP_AUTHOR);
        props.put(ContentModel.PROP_TITLE, "");
        props.put(ContentModel.PROP_DESCRIPTION, null); // Wonder how this will
                                                        // be handled
        this.nodeService.setProperties(this.nodeRef, props);

        // Make the nodeRef visible to other transactions as it will need to be in async requests
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Execute the action
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
                executer.execute(action, nodeRef);
                return null;
            }
        });

        Thread.sleep(3000); // Need to wait for the async extract

        // Check that the properties have been set
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertEquals(QUICK_TITLE, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
                assertEquals(QUICK_DESCRIPTION, nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
                assertEquals(QUICK_CREATOR, nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR));
                return null;
            }
        });
    }
    
    private static final QName PROP_UNKNOWN_1 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "unkown1");
    private static final QName PROP_UNKNOWN_2 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "unkown2");
    private static class TestUnknownMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        public TestUnknownMetadataExtracter()
        {
            Properties mappingProperties = new Properties();
            mappingProperties.put("unknown1", PROP_UNKNOWN_1.toString());
            mappingProperties.put("unknown2", PROP_UNKNOWN_2.toString());
            setMappingProperties(mappingProperties);
        }
        @Override
        protected Map<String, Set<QName>> getDefaultMapping()
        {
            // No need to give anything back as we have explicitly set the mapping already
            return new HashMap<String, Set<QName>>(0);
        }
        @Override
        public boolean isSupported(String sourceMimetype)
        {
            return sourceMimetype.equals(MimetypeMap.MIMETYPE_BINARY);
        }

        public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            Map<String, Serializable> rawMap = newRawMap();
            rawMap.put("unknown1", new Integer(1));
            rawMap.put("unknown2", "TWO");
            return rawMap;
        }
    }
    
    @Test
    public void testUnknownProperties()
    {
        TestUnknownMetadataExtracter extracterUnknown = new TestUnknownMetadataExtracter();
        extracterUnknown.setRegistry(registry);
        extracterUnknown.register();
        // Now add some content with a binary mimetype
        ContentWriter cw = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        cw.setMimetype(MimetypeMap.MIMETYPE_BINARY);
        cw.putContent("Content for " + getName());
        
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
        executer.execute(action, this.nodeRef);
        
        // The unkown properties should be present
        Serializable prop1 = nodeService.getProperty(nodeRef, PROP_UNKNOWN_1);
        Serializable prop2 = nodeService.getProperty(nodeRef, PROP_UNKNOWN_2);
        
        assertNotNull("Unknown property is null", prop1);
        assertNotNull("Unknown property is null", prop2);
    }
    
    private static class TestNullPropMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        public TestNullPropMetadataExtracter()
        {
            Properties mappingProperties = new Properties();
            mappingProperties.put("title", ContentModel.PROP_TITLE.toString());
            mappingProperties.put("description", ContentModel.PROP_DESCRIPTION.toString());
            setMappingProperties(mappingProperties);
        }
        @Override
        protected Map<String, Set<QName>> getDefaultMapping()
        {
            // No need to give anything back as we have explicitly set the mapping already
            return new HashMap<String, Set<QName>>(0);
        }
        @Override
        public boolean isSupported(String sourceMimetype)
        {
            return sourceMimetype.equals(MimetypeMap.MIMETYPE_BINARY);
        }

        public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            Map<String, Serializable> rawMap = newRawMap();
            putRawValue("title", null, rawMap);
            putRawValue("description", "", rawMap);
            return rawMap;
        }
    }
    
    /**
     * Ensure that missing raw values result in node properties being removed
     * when running with {@link ContentMetadataExtracter#setCarryAspectProperties(boolean)}
     * set to <tt>false</tt>.
     */
    @Test
    public void testNullExtractedValues_ALF1823()
    {
        TestNullPropMetadataExtracter extractor = new TestNullPropMetadataExtracter();
        extractor.setRegistry(registry);
        extractor.register();
        // Now set the title and description
        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "TITLE");
        nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, "DESCRIPTION");
        // Now add some content with a binary mimetype
        ContentWriter cw = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        cw.setMimetype(MimetypeMap.MIMETYPE_BINARY);
        cw.putContent("Content for " + getName());
        
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
        executer.execute(action, this.nodeRef);
        
        // cm:titled properties should be present
        Serializable title = nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
        Serializable descr = nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
        
        assertNotNull("cm:title property is null", title);
        assertNotNull("cm:description property is null", descr);
        
        try
        {
            // Now change the setting to remove unset aspect properties
            executer.setCarryAspectProperties(false);
            // Extract again
            executer.execute(action, this.nodeRef);
            
            // cm:titled properties should *NOT* be present
            title = nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
            descr = nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
            
            assertNull("cm:title property is not null", title);
            assertNull("cm:description property is not null", descr);
        }
        finally
        {
            executer.setCarryAspectProperties(true);
        }
    }

    /**
     * Test execution of the pragmatic approach
     */
    @Test
    public void testFromPartial() throws Exception
    {
        // Test that the action does not overwrite properties that are already
        // set
        String myCreator = "Null-op";
        String myTitle = "The hot dog is eaten by the city fox";

        // Get the old props
        Map<QName, Serializable> props = this.nodeService.getProperties(this.nodeRef);
        props.put(ContentModel.PROP_AUTHOR, myCreator);
        props.put(ContentModel.PROP_TITLE, myTitle);
        props.remove(ContentModel.PROP_DESCRIPTION); // Allow this baby
        this.nodeService.setProperties(this.nodeRef, props);

        // Make the nodeRef visible to other transactions as it will need to be in async requests
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Execute the action
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
                executer.execute(action, nodeRef);
                return null;
            }
        });

        Thread.sleep(3000); // Need to wait for the async extract

        // Check that the properties have been preserved, but that description has been set
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertEquals(myTitle, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
                assertEquals(myCreator, nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR));

                assertEquals(QUICK_DESCRIPTION, nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
                return null;
            }
        });
    }
}
