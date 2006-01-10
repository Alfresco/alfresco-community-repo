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
package org.alfresco.repo.dictionary;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;

/**
 * Dictionary model type unit test
 * 
 * @author Roy Wetherall
 */
public class DictionaryModelTypeTest extends BaseAlfrescoSpringTest
{
    /** QName of the test model */
    private static final QName TEST_MODEL_ONE = QName.createQName("{http://www.alfresco.org/test/testmodel1/1.0}testModelOne");
    
    /** Test model XML */
    public static final String MODEL_ONE_XML = 
        "<model name='test1:testModelOne' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model one</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "   </imports>" +
    
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel1/1.0' prefix='test1'/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name='test1:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
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
        "   </imports>" +
    
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel1/1.0' prefix='test1'/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name='test1:base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <properties>" +
        "           <property name='test1:prop1'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name='test1:prop2'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";

    /** Services used in tests */
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private CheckOutCheckInService cociService;
    private DictionaryDAO dictionaryDAO;
    
    /**
     * On setup in transaction override
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        
        super.onSetUpInTransaction();
        
        // Get the required services
        this.dictionaryService = (DictionaryService)this.applicationContext.getBean("dictionaryService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("namespaceService");
        this.cociService = (CheckOutCheckInService)this.applicationContext.getBean("checkOutCheckInService");
        this.dictionaryDAO = (DictionaryDAO)this.applicationContext.getBean("dictionaryDAO");
        
    }
    
    /**
     * Test the creation of dictionary model nodes
     */
    public void testCreateAndUpdateDictionaryModelNodeContent()
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
        contentWriter.putContent(MODEL_ONE_XML);
        
        // End the transaction to force update
        setComplete();
        endTransaction();
        
        final NodeRef workingCopy = TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
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
                assertEquals("Test model one", modelDefinition2.getDescription());
                
                // Check that the namespace has been added to the namespace service
                String uri2 = DictionaryModelTypeTest.this.namespaceService.getNamespaceURI("test1");
                assertEquals(uri2, "http://www.alfresco.org/test/testmodel1/1.0");
                
                // Lets check the node out and update the content
                NodeRef workingCopy = DictionaryModelTypeTest.this.cociService.checkout(modelNode);
                ContentWriter contentWriter2 = DictionaryModelTypeTest.this.contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
                contentWriter2.putContent(MODEL_ONE_MODIFIED_XML);
                
                return workingCopy;
            }
        });
        
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Check that the policy has not been fired since we have updated a working copy
                assertEquals("1.0", DictionaryModelTypeTest.this.nodeService.getProperty(workingCopy, ContentModel.PROP_MODEL_VERSION));
                
                // Now check the model changed back in
                DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                return null;
            }
        });
   
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {                        
                // Now check that the model has been updated
                assertEquals("1.1", DictionaryModelTypeTest.this.nodeService.getProperty(modelNode, ContentModel.PROP_MODEL_VERSION));
                return null;
            }
        });
       
    }
    
    public void testIsActiveFlagAndDelete()
    {
        this.dictionaryDAO.removeModel(TEST_MODEL_ONE);
        
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
        setComplete();
        endTransaction();
        
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
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
        
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {      
                // The model should now be loaded
                assertNotNull(DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE));
                
                // Set the isActive flag
                DictionaryModelTypeTest.this.nodeService.setProperty(modelNode, ContentModel.PROP_MODEL_ACTIVE, false);
                
                return null;
            }
        });
        
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
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
        
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {      
                // The model should now be loaded
                assertNotNull(DictionaryModelTypeTest.this.dictionaryService.getModel(TEST_MODEL_ONE));
                
                DictionaryModelTypeTest.this.nodeService.deleteNode(modelNode);
                
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
}
