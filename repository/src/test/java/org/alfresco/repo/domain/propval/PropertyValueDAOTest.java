/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.domain.propval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.naming.CompositeName;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.domain.propval.PropertyValueDAO.PropertyFinderCallback;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.DBTests;

/**
 * @see PropertyValueDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class PropertyValueDAOTest
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private PropertyValueDAO propertyValueDAO;

    @Before
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setMaxRetries(0);

        propertyValueDAO = (PropertyValueDAO) ctx.getBean("propertyValueDAO");

        // Remove the caches to test all functionality
        removeCaches();
    }

    private void removeCaches()
    {
        ((AbstractPropertyValueDAOImpl) propertyValueDAO).setPropertyClassCache(null);
        ((AbstractPropertyValueDAOImpl) propertyValueDAO).setPropertyDateValueCache(null);
        ((AbstractPropertyValueDAOImpl) propertyValueDAO).setPropertyDoubleValueCache(null);
        ((AbstractPropertyValueDAOImpl) propertyValueDAO).setPropertyStringValueCache(null);
        ((AbstractPropertyValueDAOImpl) propertyValueDAO).setPropertyValueCache(null);
    }

    @Test
    public void testPropertyClass() throws Exception
    {
        final Class<?> clazz = this.getClass();
        RetryingTransactionCallback<Pair<Long, Class<?>>> createClassCallback = new RetryingTransactionCallback<Pair<Long, Class<?>>>() {
            public Pair<Long, Class<?>> execute() throws Throwable
            {
                // Get the classes
                return propertyValueDAO.getOrCreatePropertyClass(clazz);
            }
        };
        final Pair<Long, Class<?>> clazzEntityPair = txnHelper.doInTransaction(createClassCallback, false);
        assertNotNull(clazzEntityPair);
        assertNotNull(clazzEntityPair.getFirst());
        assertEquals(clazz, clazzEntityPair.getSecond());
        // Now retrieve it
        RetryingTransactionCallback<Void> getClassCallback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                Pair<Long, Class<?>> checkPair1 = propertyValueDAO.getPropertyClassById(clazzEntityPair.getFirst());
                assertEquals(clazzEntityPair, checkPair1);
                Pair<Long, Class<?>> checkPair2 = propertyValueDAO.getPropertyClass(clazzEntityPair.getSecond());
                assertEquals(clazzEntityPair, checkPair2);
                return null;
            }
        };
        txnHelper.doInTransaction(getClassCallback, true);

        // Test failure when requesting invalid ID
        RetryingTransactionCallback<Void> badGetCallback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                propertyValueDAO.getPropertyClassById(Long.MIN_VALUE);
                return null;
            }
        };
        try
        {
            txnHelper.doInTransaction(badGetCallback, false);
            fail("Expected exception when using invalid ID.");
        }
        catch (RuntimeException e)
        {
            // Expected
        }

        // Test null caching
        RetryingTransactionCallback<Void> noHitCallback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                propertyValueDAO.getPropertyClass(this.getClass());
                propertyValueDAO.getPropertyClass(this.getClass());
                return null;
            }
        };
        txnHelper.doInTransaction(noHitCallback, false);
    }

    @Test
    public void testPropertyDateValue() throws Exception
    {
        final Date dateValue = ISO8601DateFormat.parse("1936-08-04T23:37:25.793Z");
        final Date dateValueBack = ISO8601DateFormat.parse("1936-08-04T00:00:00.000Z");
        RetryingTransactionCallback<Pair<Long, Date>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Date>>() {
            public Pair<Long, Date> execute() throws Throwable
            {
                return propertyValueDAO.getOrCreatePropertyDateValue(dateValue);
            }
        };
        final Pair<Long, Date> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(dateValueBack, entityPair.getSecond());

        RetryingTransactionCallback<Pair<Long, Date>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Date>>() {
            public Pair<Long, Date> execute() throws Throwable
            {
                return propertyValueDAO.getPropertyDateValue(dateValue);
            }
        };
        final Pair<Long, Date> entityPairCheck = txnHelper.doInTransaction(getValueCallback, false);
        assertNotNull(entityPairCheck);
        assertEquals(entityPair, entityPairCheck);
    }

    @Test
    public void testPropertyStringValue() throws Exception
    {
        final String stringValue = "One Two Three - àâæçéèêëîïôœùûüÿñ - " + System.currentTimeMillis();
        final String stringValueUpper = stringValue.toUpperCase();
        final String stringValueLower = stringValue.toLowerCase();
        RetryingTransactionCallback<Pair<Long, String>> createStringCallback = new RetryingTransactionCallback<Pair<Long, String>>() {
            public Pair<Long, String> execute() throws Throwable
            {
                return propertyValueDAO.getOrCreatePropertyStringValue(stringValue);
            }
        };
        final Pair<Long, String> stringEntityPair = txnHelper.doInTransaction(createStringCallback, false);
        assertNotNull(stringEntityPair);
        assertNotNull(stringEntityPair.getFirst());
        assertEquals(stringValue, stringEntityPair.getSecond());

        // Check that the uppercase and lowercase strings don't have entries
        RetryingTransactionCallback<Void> getStringCallback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                Pair<Long, String> checkPair1 = propertyValueDAO.getPropertyStringValue(stringValue);
                assertNotNull(checkPair1);
                assertEquals(stringValue, checkPair1.getSecond());
                Pair<Long, String> checkPair2 = propertyValueDAO.getPropertyStringValue(stringValueUpper);
                assertNull(checkPair2);
                Pair<Long, String> checkPair3 = propertyValueDAO.getPropertyStringValue(stringValueLower);
                assertNull(checkPair3);
                return null;
            }
        };
        txnHelper.doInTransaction(getStringCallback, true);

        RetryingTransactionCallback<Pair<Long, String>> createStringUpperCallback = new RetryingTransactionCallback<Pair<Long, String>>() {
            public Pair<Long, String> execute() throws Throwable
            {
                return propertyValueDAO.getOrCreatePropertyStringValue(stringValueUpper);
            }
        };
        final Pair<Long, String> stringUpperEntityPair = txnHelper.doInTransaction(createStringUpperCallback, false);
        assertNotNull(stringUpperEntityPair);
        assertNotNull(stringUpperEntityPair.getFirst());
        assertEquals(stringValueUpper, stringUpperEntityPair.getSecond());
        assertNotSame("String IDs were not different", stringEntityPair.getFirst(), stringUpperEntityPair.getFirst());
    }

    /**
     * Try to catch Oracle out
     */
    @Test
    public void testPropertyStringValue_EmptyAndNull() throws Exception
    {
        // Check empty string
        RetryingTransactionCallback<Void> emptyStringCallback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                Pair<Long, String> emptyStringPair1 = propertyValueDAO.getOrCreatePropertyStringValue("");
                assertNotNull(emptyStringPair1);
                assertEquals("", emptyStringPair1.getSecond());
                Pair<Long, String> emptyStringPair2 = propertyValueDAO.getOrCreatePropertyStringValue("");
                assertNotNull(emptyStringPair2);
                assertEquals(emptyStringPair1, emptyStringPair2);
                return null;
            }
        };
        txnHelper.doInTransaction(emptyStringCallback, false);
    }

    @Test
    public void testPropertyDoubleValue() throws Exception
    {
        final Double doubleValue = Double.valueOf(1.7976931348623E+125);
        RetryingTransactionCallback<Pair<Long, Double>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Double>>() {
            public Pair<Long, Double> execute() throws Throwable
            {
                return propertyValueDAO.getOrCreatePropertyDoubleValue(doubleValue);
            }
        };
        final Pair<Long, Double> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(doubleValue, entityPair.getSecond());

        RetryingTransactionCallback<Pair<Long, Double>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Double>>() {
            public Pair<Long, Double> execute() throws Throwable
            {
                return propertyValueDAO.getPropertyDoubleValue(doubleValue);
            }
        };
        final Pair<Long, Double> entityPairCheck = txnHelper.doInTransaction(getValueCallback, false);
        assertNotNull(entityPairCheck);
        assertEquals(entityPair, entityPairCheck);
    }

    @Test
    public void testPropertySerializableValue() throws Exception
    {
        final Serializable serializableValue = new CompositeName("123");
        RetryingTransactionCallback<Pair<Long, Serializable>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                return propertyValueDAO.createPropertySerializableValue(serializableValue);
            }
        };
        final Pair<Long, Serializable> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(serializableValue, entityPair.getSecond());

        RetryingTransactionCallback<Pair<Long, Serializable>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                return propertyValueDAO.getPropertySerializableValueById(entityPair.getFirst());
            }
        };
        final Pair<Long, Serializable> entityPairCheck = txnHelper.doInTransaction(getValueCallback, false);
        assertNotNull(entityPairCheck);
        assertEquals(entityPair.getFirst(), entityPairCheck.getFirst());
        assertEquals(entityPair, entityPairCheck);

        // Check that we can persist and retrieve byte[] as a Serializable
        final Serializable bytes = (Serializable) new byte[]{1, 2, 3};
        RetryingTransactionCallback<Pair<Long, Void>> testBytesCallback = new RetryingTransactionCallback<Pair<Long, Void>>() {
            public Pair<Long, Void> execute() throws Throwable
            {
                Long id = propertyValueDAO.createPropertySerializableValue(bytes).getFirst();
                Serializable check = propertyValueDAO.getPropertySerializableValueById(id).getSecond();
                assertNotNull(check);
                assertTrue(check instanceof byte[]);
                Arrays.equals((byte[]) bytes, (byte[]) check);
                return null;
            }
        };
        txnHelper.doInTransaction(testBytesCallback, false);
    }

    /**
     * Tests that the given value can be persisted and retrieved with the same resulting ID
     */
    private void runPropertyValueTest(final Serializable value) throws Exception
    {
        runPropertyValueTest(value, true);
    }

    /**
     * Tests that the given value can be persisted and retrieved with the same resulting ID
     */
    private void runPropertyValueTest(final Serializable value, final boolean runValueRetrieval) throws Exception
    {
        // Create it (if it doesn't exist)
        RetryingTransactionCallback<Pair<Long, Serializable>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                // Get the classes
                return propertyValueDAO.getOrCreatePropertyValue(value);
            }
        };
        final Pair<Long, Serializable> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(value, entityPair.getSecond());

        if (runValueRetrieval)
        {
            // Retrieve it by value
            RetryingTransactionCallback<Pair<Long, Serializable>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>() {
                public Pair<Long, Serializable> execute() throws Throwable
                {
                    // Get the classes
                    return propertyValueDAO.getPropertyValue(value);
                }
            };
            final Pair<Long, Serializable> entityPairCheck = txnHelper.doInTransaction(getValueCallback, false);
            assertNotNull(entityPairCheck);
            assertEquals(entityPair, entityPairCheck);
        }

        // Retrieve it by ID
        RetryingTransactionCallback<Pair<Long, Serializable>> getByIdCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                // Get the classes
                return propertyValueDAO.getPropertyValueById(entityPair.getFirst());
            }
        };
        final Pair<Long, Serializable> entityPairCheck2 = txnHelper.doInTransaction(getByIdCallback, false);
        assertNotNull(entityPairCheck2);
        assertEquals(entityPair, entityPairCheck2);
    }

    /**
     * See MNT-20992
     *
     * @throws Exception
     */
    @Test
    public void testPropertyValue_EmptyMaps() throws Exception
    {
        HashMap emptyMap = new HashMap();
        // Set a real cache, the problem happens in the cache
        ((AbstractPropertyValueDAOImpl) propertyValueDAO).setPropertyValueCache(new DefaultSimpleCache<Serializable, Object>());

        // Create an empty property of type MLText, this will also cache the new property
        Pair<Long, Serializable> emptyMLTextPair = txnHelper.doInTransaction(() -> propertyValueDAO.getOrCreatePropertyValue(new MLText()), false);

        // Create an empty HashMap property
        Pair<Long, Serializable> emptyHashMapPair = txnHelper.doInTransaction(() -> propertyValueDAO.getOrCreatePropertyValue(emptyMap), false);

        // Check that the returned pair representing the empty HashMap is actually a HashMap
        assertEquals("Incorrect type persisted for an empty HashMap.", HashMap.class, emptyHashMapPair.getSecond().getClass());

        // Check again by retrieving the value by value
        Pair<Long, Serializable> emptyHashMapPairRetrievedByValye = txnHelper.doInTransaction(() -> propertyValueDAO.getPropertyValue(emptyMap), false);
        assertEquals("Incorrect type persisted for an empty HashMap.", HashMap.class, emptyHashMapPairRetrievedByValye.getSecond().getClass());

        // Check again by retrieving the value by id
        Pair<Long, Serializable> emptyHashMapPairRetrievedByID = txnHelper.doInTransaction(() -> propertyValueDAO.getPropertyValueById(emptyHashMapPair.getFirst()), false);
        assertEquals("Incorrect type persisted for an empty HashMap.", HashMap.class, emptyHashMapPairRetrievedByID.getSecond().getClass());
    }

    @Test
    public void testPropertyValue_Null() throws Exception
    {
        runPropertyValueTest(null);
    }

    @Test
    public void testPropertyValue_Boolean() throws Exception
    {
        runPropertyValueTest(Boolean.TRUE);
        runPropertyValueTest(Boolean.FALSE);
    }

    @Test
    public void testPropertyValue_Short() throws Exception
    {
        for (short i = 0; i < 100; i++)
        {
            runPropertyValueTest(Short.valueOf(i));
        }
    }

    @Test
    public void testPropertyValue_Integer() throws Exception
    {
        for (int i = 0; i < 100; i++)
        {
            runPropertyValueTest(Integer.valueOf(i));
        }
    }

    @Test
    public void testPropertyValue_Long() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(Long.valueOf(i));
        }
    }

    @Test
    public void testPropertyValue_Float() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(Float.valueOf((float) i + 0.01F));
        }
    }

    @Test
    public void testPropertyValue_Double() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(Double.valueOf((double) i + 0.01D));
        }
    }

    @Test
    public void testPropertyValue_Date() throws Exception
    {
        Random rand = new Random();
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Date(rand.nextLong()));
        }
    }

    @Test
    public void testPropertyValue_String() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new String("Value-" + i + ".xyz"));
        }
    }

    @Test
    public void testPropertyValue_Serializable() throws Exception
    {
        for (int i = 0; i < 100; i++)
        {
            // Choose a type that implements equals and hashCode but will not be recognised
            runPropertyValueTest(new CompositeName("Name-" + i), false);
        }
    }

    private static enum TEST_NUMBERS
    {
        ONE, TWO, THREE;
    }

    @Test
    public void testPropertyValue_Enum() throws Exception
    {
        for (int i = 0; i < 3; i++)
        {
            TEST_NUMBERS n = TEST_NUMBERS.values()[i];
            runPropertyValueTest(n);
        }
    }

    @Test
    public void testPropertyValue_EmptyHashMap() throws Exception
    {
        final HashMap<String, String> map = new HashMap<String, String>(15);
        runPropertyValueTest(map, true);
    }

    @Test
    public void testPropertyValue_EmptyArrayList() throws Exception
    {
        final ArrayList<String> list = new ArrayList<String>(20);
        runPropertyValueTest(list, true);
    }

    @Test
    public void testPropertyValue_EmptyHashSet() throws Exception
    {
        final HashSet<String> set = new HashSet<String>(20);
        runPropertyValueTest(set, true);
    }

    @Test
    public void testPropertyValue_MapOfStrings() throws Exception
    {
        final HashMap<String, String> map = new HashMap<String, String>(15);
        for (int i = 0; i < 20; i++)
        {
            String key = "MAP-KEY-" + i;
            String value = "MAP-VALUE-" + i;
            map.put(key, value);
        }
        runPropertyValueTest(map, false);
    }

    /**
     * Tests that the given value can be persisted and retrieved with the same resulting ID
     */
    private Long runPropertyTest(final Serializable value) throws Exception
    {
        // Create it
        RetryingTransactionCallback<Long> createValueCallback = new RetryingTransactionCallback<Long>() {
            public Long execute() throws Throwable
            {
                // Get the classes
                return propertyValueDAO.createProperty(value);
            }
        };
        final Long entityId = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityId);

        // Retrieve it by ID
        RetryingTransactionCallback<Serializable> getByIdCallback = new RetryingTransactionCallback<Serializable>() {
            public Serializable execute() throws Throwable
            {
                // Get the classes
                return propertyValueDAO.getPropertyById(entityId);
            }
        };
        final Serializable entityValueCheck = txnHelper.doInTransaction(getByIdCallback, false);
        assertNotNull(entityValueCheck);
        assertEquals(value, entityValueCheck);

        // Test the callback for multiple IDs
        final Map<Long, Serializable> propsById = new HashMap<Long, Serializable>();
        PropertyFinderCallback finderCallback = new PropertyFinderCallback() {
            public void handleProperty(Long id, Serializable value)
            {
                propsById.put(id, value);
            }
        };
        List<Long> entityIds = Collections.singletonList(entityId);
        propertyValueDAO.getPropertiesByIds(entityIds, finderCallback);

        assertEquals("Should be exactly one value in map", 1, propsById.size());
        assertTrue("Expected ID to be in map", propsById.containsKey(entityId));
        assertEquals("Value was not retrieved correctly", value, propsById.get(entityId));

        // Done
        return entityId;
    }

    @Test
    public void testProperty_MapOfStrings() throws Exception
    {
        final HashMap<String, String> map = new HashMap<String, String>(15);
        for (int i = 0; i < 20; i++)
        {
            String key = "MAP-KEY-" + i;
            String value = "MAP-VALUE-" + i;
            map.put(key, value);
        }
        runPropertyTest(map);
    }

    @Test
    public void testProperty_MapOfMapOfSerializables() throws Exception
    {
        final HashMap<String, Serializable> mapInner = new HashMap<String, Serializable>(15);
        for (int i = 0; i < 20; i++)
        {
            String key = "INNERMAP-KEY-" + i;
            Serializable value = new CompositeName("INNERMAP-VALUE-" + i);
            mapInner.put(key, value);
        }
        final HashMap<String, Map<?, ?>> mapOuter = new HashMap<String, Map<?, ?>>(37);
        for (int i = 0; i < 2; i++)
        {
            String key = "OUTERMAP-KEY-" + i;
            mapOuter.put(key, mapInner);
        }
        runPropertyTest(mapOuter);
    }

    @Test
    public void testProperty_MapOfMapOfStrings() throws Exception
    {
        final HashMap<String, String> mapInner = new HashMap<String, String>(15);
        for (int i = 0; i < 20; i++)
        {
            String key = "INNERMAP-KEY-" + i;
            String value = "INNERMAP-VALUE-" + i;
            mapInner.put(key, value);
        }
        final HashMap<String, Map<?, ?>> mapOuter = new HashMap<String, Map<?, ?>>(37);
        for (int i = 0; i < 2; i++)
        {
            String key = "OUTERMAP-KEY-" + i;
            mapOuter.put(key, mapInner);
        }
        runPropertyTest(mapOuter);
    }

    @Test
    public void testProperty_CollectionOfStrings() throws Exception
    {
        final ArrayList<String> list = new ArrayList<String>(20);
        for (int i = 0; i < 20; i++)
        {
            String value = "COLL-VALUE-" + i;
            list.add(value);
        }
        runPropertyTest(list);
    }

    @Test
    public void testProperty_UpdateCollection() throws Exception
    {
        final ArrayList<String> list = new ArrayList<String>(20);
        for (int i = 0; i < 20; i++)
        {
            String value = "COLL-VALUE-" + i;
            list.add(value);
        }
        final Long propId = runPropertyTest(list);

        // Now update it
        list.add("Additional value");

        RetryingTransactionCallback<Serializable> updateAndGetCallback = new RetryingTransactionCallback<Serializable>() {
            public Serializable execute() throws Throwable
            {
                // Get the classes
                propertyValueDAO.updateProperty(propId, list);
                // Get it by the ID again
                return propertyValueDAO.getPropertyById(propId);
            }
        };
        final Serializable entityValueCheck = txnHelper.doInTransaction(updateAndGetCallback, false);
        assertNotNull(entityValueCheck);
        assertEquals(list, entityValueCheck);
    }

    // public void testProperty_UpdateToVersionRollover() throws Exception
    // {
    // final List<String> list = Collections.emptyList();
    // final Long propId = runPropertyTest((Serializable)list);
    //
    // // Do 1000 updates to a property
    // RetryingTransactionCallback<Void> updateThousandsCallback = new RetryingTransactionCallback<Void>()
    // {
    // public Void execute() throws Throwable
    // {
    // for (int i = 0; i < 1000; i++)
    // {
    // propertyValueDAO.updateProperty(propId, (Serializable)list);
    // }
    // return null;
    // }
    // };
    // for (int i = 0; i < (Short.MAX_VALUE / 1000 + 1); i++)
    // {
    // txnHelper.doInTransaction(updateThousandsCallback, false);
    // }
    // }
    //
    @Test
    public void testProperty_Delete() throws Exception
    {
        final ArrayList<String> list = new ArrayList<String>(20);
        final Long propId = runPropertyTest(list);

        // Now delete it
        RetryingTransactionCallback<Serializable> deleteCallback = new RetryingTransactionCallback<Serializable>() {
            public Serializable execute() throws Throwable
            {
                // Get the classes
                propertyValueDAO.deleteProperty(propId);
                return null;
            }
        };
        txnHelper.doInTransaction(deleteCallback, false);

        RetryingTransactionCallback<Serializable> failedGetCallback = new RetryingTransactionCallback<Serializable>() {
            public Serializable execute() throws Throwable
            {
                // Get it by the ID again
                return propertyValueDAO.getPropertyById(propId);
            }
        };
        try
        {
            final Serializable entityValueCheck = txnHelper.doInTransaction(failedGetCallback, false);
            fail("Deleted property should not be gettable.  Got: " + entityValueCheck);
        }
        catch (Throwable e)
        {
            // Expected
        }
    }

    /* Switch off caches and rerun some of the tests */
    @Test
    public void testPropertyClass_NoCache() throws Exception
    {
        removeCaches();
        testPropertyClass();
    }

    @Test
    public void testPropertyDateValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertyDateValue();
    }

    @Test
    public void testPropertyStringValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertyStringValue();
    }

    @Test
    public void testPropertyDoubleValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertyDoubleValue();
    }

    @Test
    public void testPropertySerializableValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertySerializableValue();
    }

    @Test
    public void testPropertyUniqueContext() throws Exception
    {
        final String aaa = GUID.generate();
        final String bbb = GUID.generate();

        // Check null-null-null context
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // Get the ID for nulls
                Pair<Long, Long> nullPair = propertyValueDAO.getPropertyUniqueContext(null, null, null);
                if (nullPair != null)
                {
                    propertyValueDAO.deletePropertyUniqueContext(nullPair.getFirst());
                }
                // Check nulls
                propertyValueDAO.createPropertyUniqueContext(null, null, null, "A VALUE");
                try
                {
                    propertyValueDAO.createPropertyUniqueContext(null, null, null, "A VALUE");
                    fail("Failed to throw exception creating duplicate property unique context");
                }
                catch (PropertyUniqueConstraintViolation e)
                {
                    // Expected
                }
                return null;
            }
        }, false);
        // Create a well-known context ID
        final Long id = txnHelper.doInTransaction(new RetryingTransactionCallback<Long>() {
            public Long execute() throws Throwable
            {
                return propertyValueDAO.createPropertyUniqueContext("A", "AA", aaa, null).getFirst();
            }
        }, false);
        // Check that duplicates are disallowed
        try
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                public Void execute() throws Throwable
                {
                    propertyValueDAO.createPropertyUniqueContext("A", "AA", aaa, null);
                    return null;
                }
            }, false);
            fail("Failed to throw exception creating duplicate property unique context");
        }
        catch (PropertyUniqueConstraintViolation e)
        {
            // Expected
        }
        // Check that updates work
        try
        {
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                public Void execute() throws Throwable
                {
                    // Now update it
                    propertyValueDAO.updatePropertyUniqueContextKeys(id, "A", "AA", bbb);
                    // Should be able to create the previous one ...
                    propertyValueDAO.createPropertyUniqueContext("A", "AA", aaa, null);
                    // ... and fail to create the second one
                    propertyValueDAO.createPropertyUniqueContext("A", "AA", bbb, null);
                    return null;
                }
            }, false);
            fail("Failed to throw exception creating duplicate property unique context");
        }
        catch (PropertyUniqueConstraintViolation e)
        {
            // Expected
        }
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // Delete
                propertyValueDAO.deletePropertyUniqueContext(id);
                propertyValueDAO.createPropertyUniqueContext("A", "AA", bbb, null);

                return null;
            }
        }, false);
    }

    @Test
    public void testPropertyUniqueContextValue() throws Exception
    {
        final String aaa = GUID.generate();
        final String bbb = GUID.generate();
        final String ccc = GUID.generate();

        final String v1 = GUID.generate();
        final String v2 = GUID.generate();

        // Create a well-known context ID
        final Long id = txnHelper.doInTransaction(new RetryingTransactionCallback<Long>() {
            public Long execute() throws Throwable
            {
                return propertyValueDAO.createPropertyUniqueContext(aaa, bbb, ccc, null).getFirst();
            }
        }, false);
        Pair<Long, Serializable> v0Pair = new Pair<Long, Serializable>(id, null);
        Pair<Long, Serializable> v1Pair = new Pair<Long, Serializable>(id, v1);
        Pair<Long, Serializable> v2Pair = new Pair<Long, Serializable>(id, v2);

        // Check, assign value and recheck
        Pair<Long, Serializable> pair = null;

        // Check that the property is correct
        pair = txnHelper.doInTransaction(new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(aaa, bbb, ccc);
                if (pair.getSecond() == null)
                {
                    return new Pair<Long, Serializable>(pair.getFirst(), null);
                }
                else
                {
                    Serializable value = propertyValueDAO.getPropertyById(pair.getSecond());
                    return new Pair<Long, Serializable>(pair.getFirst(), value);
                }
            }
        }, true);
        assertEquals("ID-value pair incorrect", v0Pair, pair);

        pair = txnHelper.doInTransaction(new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                propertyValueDAO.updatePropertyUniqueContext(aaa, bbb, ccc, v1);
                Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(aaa, bbb, ccc);
                Serializable value = propertyValueDAO.getPropertyById(pair.getSecond());
                return new Pair<Long, Serializable>(pair.getFirst(), value);
            }
        }, false);
        assertEquals("ID-value pair incorrect", v1Pair, pair);

        pair = txnHelper.doInTransaction(new RetryingTransactionCallback<Pair<Long, Serializable>>() {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                propertyValueDAO.updatePropertyUniqueContext(aaa, bbb, ccc, v2);
                Pair<Long, Long> pair = propertyValueDAO.getPropertyUniqueContext(aaa, bbb, ccc);
                Serializable value = propertyValueDAO.getPropertyById(pair.getSecond());
                return new Pair<Long, Serializable>(pair.getFirst(), value);
            }
        }, false);
        assertEquals("ID-value pair incorrect", v2Pair, pair);
    }

    /**
     * MNT-10067: use a script to delete the orphaned property values.
     */
    @Test
    @Ignore("The functionality is not ready, yet")
    public void testScriptCanDeleteUnusedProps()
    {
        PropValGenerator valueGen = new PropValGenerator(propertyValueDAO);

        // Find some values to use in the test that aren't already in the property tables.
        final String stringValue = valueGen.createUniqueString();
        final Double doubleValue = valueGen.createUniqueDouble();
        final Date dateValue = valueGen.createUniqueDate();
        final Serializable serValue = valueGen.createUniqueSerializable();
        // We'll keep a list of the DB IDs of the persisted values so we can later check they've been deleted.
        final Map<Object, Long> persistedIDs = new HashMap<Object, Long>();

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                persistedIDs.put(stringValue, propertyValueDAO.getOrCreatePropertyStringValue(stringValue).getFirst());
                persistedIDs.put(doubleValue, propertyValueDAO.getOrCreatePropertyDoubleValue(doubleValue).getFirst());
                persistedIDs.put(dateValue, propertyValueDAO.getOrCreatePropertyDateValue(dateValue).getFirst());
                persistedIDs.put(serValue, propertyValueDAO.getOrCreatePropertyValue(serValue).getFirst());
                return null;
            }
        });

        // Run the clean-up script.
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // Check there are some persisted values to delete.
                assertEquals(stringValue, propertyValueDAO.getPropertyStringValue(stringValue).getSecond());
                assertEquals(doubleValue, propertyValueDAO.getPropertyDoubleValue(doubleValue).getSecond());
                assertEquals(dateValue, propertyValueDAO.getPropertyDateValue(dateValue).getSecond());
                // Serializable values are the odd-one-out; we can't query for them by value
                // and no de-duplication is used during storage.
                assertEquals(serValue, propertyValueDAO.getPropertyValueById(persistedIDs.get(serValue)).getSecond());

                propertyValueDAO.cleanupUnusedValues();
                return null;
            }
        });

        // Check all the properties have been deleted.
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                assertPropDeleted(propertyValueDAO.getPropertyStringValue(stringValue));
                assertPropDeleted(propertyValueDAO.getPropertyDoubleValue(doubleValue));
                // TODO: fix date deletion, not currently handled by CleanAlfPropTables.sql
                // assertPropDeleted(propertyValueDAO.getPropertyDateValue(dateValue));
                // Serializable values cannot be queried by value
                try
                {
                    propertyValueDAO.getPropertyValueById(persistedIDs.get(serValue));
                    fail(String.format("Persisted %s was not deleted, but should have been.",
                            serValue.getClass().getSimpleName()));
                }
                catch (DataIntegrityViolationException e)
                {
                    // Good - it was deleted.
                }
                return null;
            }
        });
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
}
