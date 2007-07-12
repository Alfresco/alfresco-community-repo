/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
        
        final NodeRef workingCopy = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
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
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                // Check that the policy has not been fired since we have updated a working copy
                assertEquals("1.0", DictionaryModelTypeTest.this.nodeService.getProperty(workingCopy, ContentModel.PROP_MODEL_VERSION));
                
                // Now check the model changed back in
                DictionaryModelTypeTest.this.cociService.checkin(workingCopy, null);
                return null;
            }
        });
   
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
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
