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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

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
}
