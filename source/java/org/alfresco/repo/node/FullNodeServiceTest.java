/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Tests the fully-intercepted version of the NodeService
 * 
 * @see NodeService
 * 
 * @author Derek Hulley
 */
public class FullNodeServiceTest extends BaseNodeServiceTest
{
    protected NodeService getNodeService()
    {
        // Force cascading
        DbNodeServiceImpl dbNodeServiceImpl = (DbNodeServiceImpl) applicationContext.getBean("dbNodeServiceImpl");
        dbNodeServiceImpl.setCascadeInTransaction(true);
        
        return (NodeService) applicationContext.getBean("NodeService");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
    }

    public void testMLTextValues() throws Exception
    {
        // Set the server default locale
        Locale.setDefault(Locale.ENGLISH);
        
        MLText mlTextProperty = new MLText();
        mlTextProperty.addValue(Locale.ENGLISH, "Very good!");
        mlTextProperty.addValue(Locale.FRENCH, "Très bon!");
        mlTextProperty.addValue(Locale.GERMAN, "Sehr gut!");

        nodeService.setProperty(
                rootNodeRef,
                BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE,
                mlTextProperty);
        
        // Check filterered property retrieval
        Serializable textValueFiltered = nodeService.getProperty(
                rootNodeRef,
                BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE);
        assertEquals(
                "Default locale value not taken for ML text",
                mlTextProperty.getValue(Locale.ENGLISH),
                textValueFiltered);
        
        // Check filtered mass property retrieval
        Map<QName, Serializable> propertiesFiltered = nodeService.getProperties(rootNodeRef);
        assertEquals(
                "Default locale value not taken for ML text in Map",
                mlTextProperty.getValue(Locale.ENGLISH),
                propertiesFiltered.get(BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
    }
    
    public void testLongMLTextValues() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4096; i++)
        {
            sb.append(" ").append(i);
        }
        String longString = sb.toString();
        // Set the server default locale
        Locale.setDefault(Locale.ENGLISH);

        // Set it as a normal string
        nodeService.setProperty(
                rootNodeRef,
                BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE,
                longString);
        
        MLText mlTextProperty = new MLText();
        mlTextProperty.addValue(Locale.ENGLISH, longString);
        mlTextProperty.addValue(Locale.FRENCH, longString);
        mlTextProperty.addValue(Locale.GERMAN, longString);

        // Set it as MLText
        nodeService.setProperty(
                rootNodeRef,
                BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE,
                mlTextProperty);
    }
    
    public void testNullMLText() throws Exception
    {
        Map<QName, Serializable> properties = nodeService.getProperties(rootNodeRef);
        // Set an ML value to null
        properties.put(BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, null);
        nodeService.setProperty(rootNodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, null);
        // Get them again
        Serializable mlTextSer = nodeService.getProperty(rootNodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE);
        MLText mlText = DefaultTypeConverter.INSTANCE.convert(MLText.class, mlTextSer);
        assertNull("Value returned is not null", mlText);
    }

    public void testMLValuesOnCreate() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        fillProperties(BaseNodeServiceTest.TYPE_QNAME_TEST_MANY_PROPERTIES, properties);
        // Replace the MLText value with a plain string
        properties.put(BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, "Bonjour");
        // Now switch to French
        I18NUtil.setContentLocale(Locale.FRENCH);
        // Create a node with the value
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                BaseNodeServiceTest.ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, getName()),
                BaseNodeServiceTest.TYPE_QNAME_TEST_MANY_PROPERTIES,
                properties).getChildRef();
        // Now switch to English
        I18NUtil.setContentLocale(Locale.ENGLISH);
        // Set the english property
        nodeService.setProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, "Hello");
        
        // Switch back to French and get the value
        I18NUtil.setContentLocale(Locale.FRENCH);
        assertEquals(
                "Expected French value property",
                "Bonjour",
                nodeService.getProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
        
        // Switch back to English and get the value
        I18NUtil.setContentLocale(Locale.ENGLISH);
        assertEquals(
                "Expected English value property",
                "Hello",
                nodeService.getProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
    }

    public void testMLValuesOnAddAspect() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        fillProperties(BaseNodeServiceTest.TYPE_QNAME_TEST_MANY_PROPERTIES, properties);
        // Replace the MLText value with a plain string
        properties.put(BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, "Bonjour");
        // Now switch to French
        I18NUtil.setContentLocale(Locale.FRENCH);
        // Add an aspect
        NodeRef nodeRef = rootNodeRef;
        nodeService.addAspect(
                nodeRef,
                BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED,
                properties);
        // Now switch to English
        I18NUtil.setContentLocale(Locale.ENGLISH);
        // Set the english property
        nodeService.setProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, "Hello");
        
        // Switch back to French and get the value
        I18NUtil.setContentLocale(Locale.FRENCH);
        assertEquals(
                "Expected French value property",
                "Bonjour",
                nodeService.getProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
        
        // Switch back to English and get the value
        I18NUtil.setContentLocale(Locale.ENGLISH);
        assertEquals(
                "Expected English value property",
                "Hello",
                nodeService.getProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
    }

    public void testMLValuesOnAddProperties() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        fillProperties(BaseNodeServiceTest.TYPE_QNAME_TEST_MANY_PROPERTIES, properties);
        // Replace the MLText value with a plain string
        properties.put(BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, "Bonjour");
        // Now switch to French
        I18NUtil.setContentLocale(Locale.FRENCH);
        // Add an aspect
        NodeRef nodeRef = rootNodeRef;
        nodeService.addProperties(nodeRef, properties);
        // Now switch to English
        I18NUtil.setContentLocale(Locale.ENGLISH);
        // Set the english property
        nodeService.setProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE, "Hello");
        
        // Switch back to French and get the value
        I18NUtil.setContentLocale(Locale.FRENCH);
        assertEquals(
                "Expected French value property",
                "Bonjour",
                nodeService.getProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
        
        // Switch back to English and get the value
        I18NUtil.setContentLocale(Locale.ENGLISH);
        assertEquals(
                "Expected English value property",
                "Hello",
                nodeService.getProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
    }

    /**
     * {@inheritDoc}
     * 
     * This instance modifies the ML text value to be just the default locale string.
     */
    protected void getExpectedPropertyValues(Map<QName, Serializable> checkProperties)
    {
        MLText mlTextValue = (MLText) checkProperties.get(PROP_QNAME_ML_TEXT_VALUE);
        String strValue = mlTextValue.getDefaultValue();
        checkProperties.put(PROP_QNAME_ML_TEXT_VALUE, strValue);
    }
    
    @SuppressWarnings("unchecked")
    public void testMultiProp() throws Exception
    {
        QName undeclaredPropQName = QName.createQName(NAMESPACE, getName());
        // create node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MULTIPLE_TESTER).getChildRef();
        ArrayList<Serializable> values = new ArrayList<Serializable>(1);
        values.add("ABC");
        values.add("DEF");
        // test allowable conditions
        nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, "ABC");
        // nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, values); -- should fail
        nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_MULTIPLE, "ABC");
        nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_MULTIPLE, values);
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_SINGLE, "ABC");
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_SINGLE, values);
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, "ABC");
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, values);
        nodeService.setProperty(nodeRef, undeclaredPropQName, "ABC");
        nodeService.setProperty(nodeRef, undeclaredPropQName, values);

        // commit as we will be breaking the transaction in the next test
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            // this should fail as we are passing multiple values into a non-any that is multiple=false
            nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, values);
        }
        catch (DictionaryException e)
        {
            // expected
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable e) {}
        }
        
        txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            // Check that multi-valued d:mltext can be collections of MLText
            values.clear();
            values.add(new MLText("ABC"));
            values.add(new MLText("DEF"));
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE, values);
            List<Serializable> checkValues = (List<Serializable>) nodeService.getProperty(
                    nodeRef, PROP_QNAME_MULTI_ML_VALUE);
            assertEquals("Expected 2 MLText values back", 2, checkValues.size());
            assertTrue("Incorrect type in collection", checkValues.get(0) instanceof String);
            assertTrue("Incorrect type in collection", checkValues.get(1) instanceof String);
            
            // Check that multi-valued d:any properties can be collections of collections (empty)
            // We put ArrayLists and HashSets into the Collection of d:any, so that is exactly what should come out
            values.clear();
            ArrayList<Serializable> arrayListVal = new ArrayList<Serializable>(2);
            HashSet<Serializable> hashSetVal = new HashSet<Serializable>(2);
            values.add(arrayListVal);
            values.add(hashSetVal);
            nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, values);
            checkValues = (List<Serializable>) nodeService.getProperty(
                    nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE);
            assertEquals("Expected 2 Collection values back", 2, checkValues.size());
            assertTrue("Incorrect type in collection", checkValues.get(0) instanceof ArrayList);  // ArrayList in - ArrayList out
            assertTrue("Incorrect type in collection", checkValues.get(1) instanceof HashSet);  // HashSet in - HashSet out
            
            // Check that multi-valued d:any properties can be collections of collections (with values)
            // We put ArrayLists and HashSets into the Collection of d:any, so that is exactly what should come out
            arrayListVal.add("ONE");
            arrayListVal.add("TWO");
            hashSetVal.add("ONE");
            hashSetVal.add("TWO");
            values.clear();
            values.add(arrayListVal);
            values.add(hashSetVal);
            nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, values);
            checkValues = (List<Serializable>) nodeService.getProperty(
                    nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE);
            assertEquals("Expected 2 Collection values back", 2, checkValues.size());
            assertTrue("Incorrect type in collection", checkValues.get(0) instanceof ArrayList);  // ArrayList in - ArrayList out
            assertTrue("Incorrect type in collection", checkValues.get(1) instanceof HashSet);  // HashSet in - HashSet out
            assertEquals("First collection incorrect", 2, ((Collection)checkValues.get(0)).size());
            assertEquals("Second collection incorrect", 2, ((Collection)checkValues.get(1)).size());
        }
        catch (DictionaryException e)
        {
            // expected
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable e) {}
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testMultiValueMLTextProperties() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_ML_PROPERTIES).getChildRef();
        
        // Create MLText properties and add to a collection
        List<MLText> mlTextCollection = new ArrayList<MLText>(2);
        MLText mlText0 = new MLText();
        mlText0.addValue(Locale.ENGLISH, "Hello");
        mlText0.addValue(Locale.FRENCH, "Bonjour");
        mlTextCollection.add(mlText0);
        MLText mlText1 = new MLText();
        mlText1.addValue(Locale.ENGLISH, "Bye bye");
        mlText1.addValue(Locale.FRENCH, "Au revoir");
        mlTextCollection.add(mlText1);
        
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE, (Serializable) mlTextCollection);
        
        I18NUtil.setContentLocale(Locale.ENGLISH);
        Collection<String> mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"Hello", "Bye bye"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.FRENCH);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"Bonjour", "Au revoir"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.GERMAN);
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE, (Serializable)Arrays.asList(new String[]{"eins", "zwei", "drie", "vier"}));
        
        I18NUtil.setContentLocale(Locale.ENGLISH);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"Hello", "Bye bye"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.FRENCH);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"Bonjour", "Au revoir"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.GERMAN);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"eins", "zwei", "drie", "vier"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.GERMAN);
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE, (Serializable)Arrays.asList(new String[]{"eins"}));
        
        I18NUtil.setContentLocale(Locale.ENGLISH);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"Hello", "Bye bye"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.FRENCH);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"Bonjour", "Au revoir"}), mlTextCollectionCheck);
        
        I18NUtil.setContentLocale(Locale.GERMAN);
        mlTextCollectionCheck = (Collection<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", Arrays.asList(new String[]{"eins"}), mlTextCollectionCheck);
        
    }
}
