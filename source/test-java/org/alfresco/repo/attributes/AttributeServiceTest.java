/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.domain.propval.DefaultPropertyTypeConverter;
import org.alfresco.repo.domain.propval.PropValGenerator;
import org.alfresco.repo.domain.propval.PropertyTypeConverter;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.domain.propval.PropertyValueEntity;
import org.alfresco.repo.domain.propval.PropertyValueEntity.PersistedType;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.AttributeService.AttributeQueryCallback;
import org.alfresco.service.cmr.attributes.DuplicateAttributeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.apache.commons.lang.mutable.MutableInt;
import org.springframework.context.ApplicationContext;

/**
 * {@link AttributeService}
 * 
 * @author Derek Hulley
 */
public class AttributeServiceTest extends TestCase
{
    private static final Serializable KEY_A = "a";
    private static final Serializable[] KEY_AAA = new Serializable[] {"a", "a", "a"};
    private static final Serializable[] KEY_AAB = new Serializable[] {"a", "a", "b"};
    private static final Serializable[] KEY_AAC = new Serializable[] {"a", "a", "c"};
    private static final Serializable VALUE_AAA_STRING = "aaa";
    private static final Serializable VALUE_AAB_STRING = "aab";
    private static final Serializable VALUE_AAC_STRING = "aac";
    
    private ApplicationContext ctx;
    private AttributeService attributeService;
    private PropertyValueDAO propertyValueDAO;
    
    @Override
    protected void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        attributeService = (AttributeService) ctx.getBean("AttributeService");
        propertyValueDAO = (PropertyValueDAO) ctx.getBean("propertyValueDAO");
    }

    @Override
    protected void tearDown() throws Exception
    {
    }
    
    public void testBasic() throws Exception
    {
        attributeService.removeAttribute(KEY_AAA);
        attributeService.removeAttribute(KEY_AAB);
        attributeService.removeAttribute(KEY_AAC);
        
        assertFalse(attributeService.exists(KEY_AAA));
        assertFalse(attributeService.exists(KEY_AAB));
        assertFalse(attributeService.exists(KEY_AAC));
        
        attributeService.setAttribute(VALUE_AAA_STRING, KEY_AAA);
        attributeService.setAttribute(VALUE_AAB_STRING, KEY_AAB);
        attributeService.setAttribute(VALUE_AAC_STRING, KEY_AAC);

        assertTrue(attributeService.exists(KEY_AAA));
        assertTrue(attributeService.exists(KEY_AAB));
        assertTrue(attributeService.exists(KEY_AAC));
        
        assertEquals(VALUE_AAA_STRING, attributeService.getAttribute(KEY_AAA));
        assertEquals(VALUE_AAB_STRING, attributeService.getAttribute(KEY_AAB));
        assertEquals(VALUE_AAC_STRING, attributeService.getAttribute(KEY_AAC));
//        
//        attributeService.removeAttribute(KEY_AAA);
//        attributeService.removeAttribute(KEY_AAB);
//        attributeService.removeAttribute(KEY_AAC);
    }
    
    /**
     * Checks that {@link AttributeService#getAttributes(AttributeQueryCallback, Serializable...) AttributeService.getAttributes}
     * works.  This includes coverage of <a href=https://issues.alfresco.com/jira/browse/MNT-9112>MNT-9112</a>.
     */
    public void testGetAttributes() throws Exception
    {
        attributeService.setAttribute(VALUE_AAA_STRING, KEY_AAA);
        attributeService.setAttribute(VALUE_AAB_STRING, KEY_AAB);
        attributeService.setAttribute(VALUE_AAC_STRING, KEY_AAC);

        final List<Serializable> results = new ArrayList<Serializable>();
        final MutableInt counter = new MutableInt();
        final MutableInt max = new MutableInt(3);
        AttributeQueryCallback callback = new AttributeQueryCallback()
        {
            @Override
            public boolean handleAttribute(Long id, Serializable value, Serializable[] keys)
            {
                counter.increment();
                results.add(value);
                if (counter.intValue() == max.intValue())
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        };
        
        counter.setValue(0);
        max.setValue(3);
        results.clear();
        attributeService.getAttributes(callback, KEY_A);
        assertEquals(3, results.size());
        assertEquals(3, counter.getValue());
        
        counter.setValue(0);
        max.setValue(2);
        results.clear();
        attributeService.getAttributes(callback, KEY_A);
        assertEquals(2, results.size());
        assertEquals(2, counter.getValue());
    }
    
    public void testRemoveOrphanedProps()
    {
        final Serializable[] stringKey = new String[] { "z", "q", "string" };
        final Serializable[] doubleKey = new String[] { "z", "q", "double" };
        final Serializable[] dateKey = new String[] { "z", "q", "date" };
        
        // Make sure there's nothing left from previous failed test runs etc.
        attributeService.removeAttributes(stringKey);
        attributeService.removeAttributes(doubleKey);
        attributeService.removeAttributes(dateKey);
        
        final PropValGenerator valueGen = new PropValGenerator(propertyValueDAO);

        // Create some values
        final String stringValue = valueGen.createUniqueString();
        attributeService.createAttribute(stringValue, stringKey);
        
        final Double doubleValue = valueGen.createUniqueDouble();
        attributeService.createAttribute(doubleValue, doubleKey);
        
        final Date dateValue = valueGen.createUniqueDate();
        attributeService.createAttribute(dateValue, dateKey);
        
        // Remove the properties, potentially leaving oprhaned prop values.
        attributeService.removeAttributes(stringKey);
        attributeService.removeAttributes(doubleKey);
        attributeService.removeAttributes(dateKey);
        
        // Check there are some persisted values to delete, otherwise there is no
        // need to run the cleanup script in the first place.
        assertEquals(stringValue, propertyValueDAO.getPropertyValue(stringValue).getSecond());
        assertEquals(doubleValue, propertyValueDAO.getPropertyValue(doubleValue).getSecond());
        assertEquals(dateValue, propertyValueDAO.getPropertyValue(dateValue).getSecond());
        
        // Run the cleanup script - should remove the orphaned values.
        propertyValueDAO.cleanupUnusedValues();
     
        // Check that the cleanup script removed the orphaned values.
        assertPropDeleted(propertyValueDAO.getPropertyValue(stringValue));
        assertPropDeleted(propertyValueDAO.getPropertyValue(doubleValue));
        assertPropDeleted(propertyValueDAO.getPropertyValue(dateValue));
    }
    
    private void assertPropDeleted(Pair<Long, ?> value)
    {
        if (value != null)
        {
            String msg = String.format("Property value [%s=%s] should have been deleted by cleanup script.",
                        value.getSecond().getClass().getSimpleName(), value.getSecond());
            fail(msg);
        }
    }
    
    public void testKeySegmentsGuaranteeUniqueness()
    {
        final PropertyTypeConverter converter = new DefaultPropertyTypeConverter();

        final int VAL1 = 0;
        final int VAL2 = 1;
        final int KEY_INT_1 = 1;
        final int KEY_INT_2 = 2;
        final String KEY_STR_1 = "string";
        final String KEY_STR_2 = "string2";
        final TestIdentifier.TestEnum KEY_ENUM = TestIdentifier.TestEnum.ONE;
        final HashMap<String, String> KEY_MAP = new HashMap<>();
        final NodeRef KEY_NODEREF = new NodeRef("workspace://SpacesStore/5980bbdb-8a31-437e-95c8-d5092c3c58fc");
        final TestIdentifier KEY_SERIALIZABLE = new TestIdentifier(KEY_STR_2);

        try
        {
            // check integer keys
            assertEquals(PropertyValueEntity.getPersistedTypeEnum(KEY_INT_2, converter), PersistedType.LONG);
            attributeService.createAttribute(VAL1, KEY_INT_1, KEY_INT_2);
            try
            {
                attributeService.createAttribute(VAL2, KEY_INT_1, KEY_INT_2);
                fail("Duplicate attribute creation should not be allowed");
            }
            catch (DuplicateAttributeException expected)
            {
            }

            // check noderef keys
            assertEquals(PropertyValueEntity.getPersistedTypeEnum(KEY_NODEREF, converter), PersistedType.STRING);
            attributeService.createAttribute(VAL1, KEY_INT_1, KEY_NODEREF);
            try
            {
                attributeService.createAttribute(VAL2, KEY_INT_1, KEY_NODEREF);
                fail("Duplicate attribute creation should not be allowed");
            }
            catch (DuplicateAttributeException expected)
            {
            }

            // check enum keys
            assertEquals(PropertyValueEntity.getPersistedTypeEnum(KEY_ENUM, converter), PersistedType.ENUM);
            attributeService.createAttribute(VAL1, KEY_INT_1, KEY_ENUM);
            try
            {
                attributeService.createAttribute(VAL2, KEY_INT_1, KEY_ENUM);
                fail("Duplicate attribute creation should not be allowed");
            }
            catch (DuplicateAttributeException expected)
            {
            }

            // check constructable keys
            assertEquals(PropertyValueEntity.getPersistedTypeEnum(KEY_MAP, converter), PersistedType.CONSTRUCTABLE);
            attributeService.createAttribute(VAL1, KEY_INT_1, KEY_MAP);
            try
            {
                attributeService.createAttribute(VAL2, KEY_INT_1, KEY_MAP);
                fail("Duplicate attribute creation should not be allowed");
            }
            catch (DuplicateAttributeException expected)
            {
            }

            // check string keys
            assertEquals(PropertyValueEntity.getPersistedTypeEnum(KEY_STR_2, converter), PersistedType.STRING);
            attributeService.createAttribute(VAL1, KEY_STR_1, KEY_STR_2);
            try
            {
                attributeService.createAttribute(VAL2, KEY_STR_1, KEY_STR_2);
                fail("Duplicate attribute creation should not be allowed");
            }
            catch (DuplicateAttributeException expected)
            {
            }

            // check custom type serializable key
            assertEquals(PropertyValueEntity.getPersistedTypeEnum(KEY_SERIALIZABLE, converter), PersistedType.SERIALIZABLE);
            try
            {
                attributeService.createAttribute(VAL1, KEY_STR_1, KEY_SERIALIZABLE);
                fail("Keys of SERIALIZABLE persisted type are not allowed because it cannot guarantee uniqueness");
            }
            catch (IllegalArgumentException expected)
            {
            }
        }
        finally
        {
            attributeService.removeAttribute(KEY_INT_1, KEY_INT_2);
            attributeService.removeAttribute(KEY_INT_1, KEY_NODEREF);
            attributeService.removeAttribute(KEY_INT_1, KEY_ENUM);
            attributeService.removeAttribute(KEY_INT_1, KEY_MAP);
            attributeService.removeAttribute(KEY_STR_1, KEY_STR_2);
            attributeService.removeAttribute(KEY_STR_1, KEY_SERIALIZABLE);
        }
    }
}
