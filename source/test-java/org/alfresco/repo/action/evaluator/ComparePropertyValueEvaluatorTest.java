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
package org.alfresco.repo.action.evaluator;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Compare property value evaluator test
 * 
 * @author Roy Wetherall
 */
public class ComparePropertyValueEvaluatorTest extends BaseSpringTest
{
    private static final String TEST_TYPE_NAMESPACE = "testNamespace";
    private static final QName TEST_TYPE_QNAME = QName.createQName(TEST_TYPE_NAMESPACE, "testType");
    private static final QName PROP_TEXT = QName.createQName(TEST_TYPE_NAMESPACE, "propText");
    private static final QName PROP_INT = QName.createQName(TEST_TYPE_NAMESPACE, "propInt");
    private static final QName PROP_DATETIME = QName.createQName(TEST_TYPE_NAMESPACE, "propDatetime");
    private static final QName PROP_NODEREF = QName.createQName(TEST_TYPE_NAMESPACE, "propNodeRef");
    private static final QName PROP_MULTI_VALUE = QName.createQName(TEST_TYPE_NAMESPACE, "propMultiValue");
    
    private static final String TEXT_VALUE = "myDocument.doc";
    private static final int INT_VALUE = 100;
    
    private Date beforeDateValue;
    private Date dateValue;
    private Date afterDateValue;
    private NodeRef nodeValue;
    
    private DictionaryDAO dictionaryDAO;
    private NodeService nodeService;
    private ContentService contentService;
    private ActionService actionService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private ComparePropertyValueEvaluator evaluator;
    
    /**
     * Sets the meta model DAO
     * 
     * @param dictionaryDAO  the meta model DAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        // Need to create model to contain our custom type
        createTestModel();
        
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
        actionService = (ActionService)applicationContext.getBean("actionService");
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        this.nodeValue = this.rootNodeRef;
        
        this.beforeDateValue = new Date();
        Thread.sleep(2000);
        this.dateValue = new Date();
        Thread.sleep(2000);
        this.afterDateValue = new Date();
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PROP_TEXT, TEXT_VALUE);
        props.put(PROP_INT, INT_VALUE);
        props.put(PROP_DATETIME, this.dateValue);
        props.put(PROP_NODEREF, this.nodeValue);
        props.put(PROP_MULTI_VALUE, TEXT_VALUE);
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                TEST_TYPE_QNAME,
                props).getChildRef();
        
        this.evaluator = (ComparePropertyValueEvaluator)this.applicationContext.getBean(ComparePropertyValueEvaluator.NAME);
    }
    
    public void testCheckParamDefintionWithConstraint()
    {
        ActionConditionDefinition def = evaluator.getActionConditionDefintion();        
        assertEquals(ComparePropertyValueEvaluator.NAME, def.getName());
        ParameterDefinition paramDef = def.getParameterDefintion(ComparePropertyValueEvaluator.PARAM_OPERATION);
        assertNotNull(paramDef);
        assertEquals(ComparePropertyValueEvaluator.PARAM_OPERATION, paramDef.getName());
        String constraintName = paramDef.getParameterConstraintName();
        assertNotNull(constraintName);
        ParameterConstraint paramConstraint = actionService.getParameterConstraint(constraintName);
        assertNotNull(paramConstraint);
        assertEquals("ac-compare-operations", paramConstraint.getName());
    }
    
    /**
     * Test numeric comparisions
     */
    public void testNumericComparison()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, PROP_INT);
        
        // Test the default operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, INT_VALUE);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 101);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test equals operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, INT_VALUE);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 101);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));  
        
        // Test equals greater than operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 99);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 101);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));  
        
        // Test equals greater than operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN_EQUAL.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 99);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 100);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 101);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test equals less than operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 101);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 99);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));  
        
        // Test equals less than equals operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN_EQUAL.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 101);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 100);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 99);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Ensure other operators are invalid
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.BEGINS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {exception.printStackTrace();};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.CONTAINS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};  
    }
    
    /**
     * Test date comparison
     */
    public void testDateComparison()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, PROP_DATETIME);
        
        // Test the default operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.dateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, new Date());
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test the equals operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.dateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, new Date());
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test equals greater than operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.beforeDateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.afterDateValue);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));  
        
        // Test equals greater than operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN_EQUAL.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.beforeDateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.dateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.afterDateValue);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test equals less than operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.afterDateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.beforeDateValue);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));  
        
        // Test equals less than equals operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN_EQUAL.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.afterDateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.dateValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.beforeDateValue);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Ensure other operators are invalid
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.BEGINS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {exception.printStackTrace();};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.CONTAINS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};  
    }
    
    /**
     * Test text comparison
     */
    public void testTextComparison()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, PROP_TEXT);
        
        // Test default operations implied by presence and position of *
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.xls");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "my*");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bad*");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "Document");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bobbins");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test equals operator
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, TEXT_VALUE);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bobbins");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));        
        
        // Test contains operator
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.CONTAINS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "Document");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bobbins");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef)); 
        
        // Test begins operator
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.BEGINS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "my");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bobbins");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef)); 
        
        // Test ends operator
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "doc");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bobbins");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef)); 
        
        // Ensure other operators are invalid
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {exception.printStackTrace();};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN_EQUAL.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN_EQUAL.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
    }
    
    /**
     * Test some combinations of test file names that had been failing 
     */
    public void testTempFileNames()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, PROP_TEXT);
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "~*.doc");
        this.nodeService.setProperty(this.nodeRef, PROP_TEXT, "~1234.doc");
        
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
    }
    
    /**
     * Test comparison of properties that do not have a registered comparitor
     */
    public void testOtherComparison()
    {
        NodeRef badNodeRef = new NodeRef(this.testStoreRef, "badId");
        
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, PROP_NODEREF);
        
        // Test default operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.nodeValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, badNodeRef);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "this isn't even the correct type!");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test equals operation
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, this.nodeValue);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, badNodeRef);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));  
        
        // Ensure other operators are invalid
        
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.BEGINS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) { exception.printStackTrace();};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.CONTAINS.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.GREATER_THAN_EQUAL.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN_EQUAL.toString());
        try { this.evaluator.evaluate(condition, this.nodeRef); fail("An exception should have been raised here."); } catch (ActionServiceException exception) {};
        
    }
    
    public void testContentPropertyComparisons()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
        
        // What happens if you do this and the node has no content set yet !!
        
        // Add some content to the node reference
        ContentWriter contentWriter = this.contentService.getWriter(this.nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.putContent("This is some test content.");        
        
        // Test matching the mimetype
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.MIME_TYPE.toString());
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_HTML);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test matching the encoding
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.ENCODING.toString());
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "UTF-8");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "UTF-16");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        // Test comparision to the size of the content
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.SIZE.toString());
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.LESS_THAN.toString());
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 50);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, 2);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
        
        
    }

    public void testMultiValuedPropertyComparisons()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, PROP_MULTI_VALUE);

        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.CONTAINS.toString());

        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "Document");
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));

        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "bobbins");
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));

    }
    
    private void createTestModel()
    {
        M2Model model = M2Model.createModel("test:comparepropertyvalueevaluatortest");
        model.createNamespace(TEST_TYPE_NAMESPACE, "test");
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, NamespaceService.DICTIONARY_MODEL_PREFIX);
        model.createImport(NamespaceService.SYSTEM_MODEL_1_0_URI, NamespaceService.SYSTEM_MODEL_PREFIX);
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);

        M2Type testType = model.createType("test:" + TEST_TYPE_QNAME.getLocalName());
        testType.setParentName("cm:" + ContentModel.TYPE_CONTENT.getLocalName());
        
        M2Property prop1 = testType.createProperty("test:" + PROP_TEXT.getLocalName());
        prop1.setMandatory(false);
        prop1.setType("d:" + DataTypeDefinition.TEXT.getLocalName());
        prop1.setMultiValued(false);
        
        M2Property prop2 = testType.createProperty("test:" + PROP_INT.getLocalName());
        prop2.setMandatory(false);
        prop2.setType("d:" + DataTypeDefinition.INT.getLocalName());
        prop2.setMultiValued(false);
        
        M2Property prop3 = testType.createProperty("test:" + PROP_DATETIME.getLocalName());
        prop3.setMandatory(false);
        prop3.setType("d:" + DataTypeDefinition.DATETIME.getLocalName());
        prop3.setMultiValued(false);
        
        M2Property prop4 = testType.createProperty("test:" + PROP_NODEREF.getLocalName());
        prop4.setMandatory(false);
        prop4.setType("d:" + DataTypeDefinition.NODE_REF.getLocalName());
        prop4.setMultiValued(false);

        M2Property prop5 = testType.createProperty("test:" + PROP_MULTI_VALUE.getLocalName());
        prop5.setMandatory(false);
        prop5.setType("d:" + DataTypeDefinition.TEXT.getLocalName());
        prop5.setMultiValued(true);

        dictionaryDAO.putModel(model);
    }
}
