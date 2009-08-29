/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.propval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.naming.CompositeName;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.bouncycastle.util.Arrays;
import org.springframework.context.ApplicationContext;

/**
 * @see PropertyValueDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyValueDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private PropertyValueDAO propertyValueDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        propertyValueDAO = (PropertyValueDAO) ctx.getBean("propertyValueDAO");
        
        // Remove the caches to test all functionality
        removeCaches();
    }
    
    private void removeCaches()
    {
        ((AbstractPropertyValueDAOImpl)propertyValueDAO).setPropertyClassCache(null);
        ((AbstractPropertyValueDAOImpl)propertyValueDAO).setPropertyDateValueCache(null);
        ((AbstractPropertyValueDAOImpl)propertyValueDAO).setPropertyDoubleValueCache(null);
        ((AbstractPropertyValueDAOImpl)propertyValueDAO).setPropertyStringValueCache(null);
        ((AbstractPropertyValueDAOImpl)propertyValueDAO).setPropertyValueCache(null);
    }
    
    public void testPropertyClass() throws Exception
    {
        final Class<?> clazz = this.getClass();
        RetryingTransactionCallback<Pair<Long, Class<?>>> createClassCallback = new RetryingTransactionCallback<Pair<Long, Class<?>>>()
        {
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
        RetryingTransactionCallback<Void> getClassCallback = new RetryingTransactionCallback<Void>()
        {
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
        RetryingTransactionCallback<Void> badGetCallback = new RetryingTransactionCallback<Void>()
        {
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
        RetryingTransactionCallback<Void> noHitCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                propertyValueDAO.getPropertyClass(this.getClass());
                propertyValueDAO.getPropertyClass(this.getClass());
                return null;
            }
        };
        txnHelper.doInTransaction(noHitCallback, false);
    }
    
    public void testPropertyDateValue() throws Exception
    {
        final Date dateValue = ISO8601DateFormat.parse("1936-08-04T23:37:25.793Z");
        final Date dateValueBack = ISO8601DateFormat.parse("1936-08-04T00:00:00.000Z");
        RetryingTransactionCallback<Pair<Long, Date>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Date>>()
        {
            public Pair<Long, Date> execute() throws Throwable
            {
                return propertyValueDAO.getOrCreatePropertyDateValue(dateValue);
            }
        };
        final Pair<Long, Date> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(dateValueBack, entityPair.getSecond());
        
        RetryingTransactionCallback<Pair<Long, Date>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Date>>()
        {
            public Pair<Long, Date> execute() throws Throwable
            {
                return propertyValueDAO.getPropertyDateValue(dateValue);
            }
        };
        final Pair<Long, Date> entityPairCheck = txnHelper.doInTransaction(getValueCallback, false);
        assertNotNull(entityPairCheck);
        assertEquals(entityPair, entityPairCheck);
    }
    
    public void testPropertyStringValue() throws Exception
    {
        final String stringValue = "One Two Three - " + System.currentTimeMillis();
        final String stringValueUpper = stringValue.toUpperCase();
        final String stringValueLower = stringValue.toLowerCase();
        RetryingTransactionCallback<Pair<Long, String>> createStringCallback = new RetryingTransactionCallback<Pair<Long, String>>()
        {
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
        RetryingTransactionCallback<Void> getStringCallback = new RetryingTransactionCallback<Void>()
        {
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

        RetryingTransactionCallback<Pair<Long, String>> createStringUpperCallback = new RetryingTransactionCallback<Pair<Long, String>>()
        {
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
        
        // Check empty string
        RetryingTransactionCallback<Void> emptyStringCallback = new RetryingTransactionCallback<Void>()
        {
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
        txnHelper.doInTransaction(emptyStringCallback, true);
    }
    
    public void testPropertyDoubleValue() throws Exception
    {
        final Double doubleValue = Double.valueOf(1.7976931348623E+308);
        RetryingTransactionCallback<Pair<Long, Double>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Double>>()
        {
            public Pair<Long, Double> execute() throws Throwable
            {
                return propertyValueDAO.getOrCreatePropertyDoubleValue(doubleValue);
            }
        };
        final Pair<Long, Double> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(doubleValue, entityPair.getSecond());
        
        RetryingTransactionCallback<Pair<Long, Double>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Double>>()
        {
            public Pair<Long, Double> execute() throws Throwable
            {
                return propertyValueDAO.getPropertyDoubleValue(doubleValue);
            }
        };
        final Pair<Long, Double> entityPairCheck = txnHelper.doInTransaction(getValueCallback, false);
        assertNotNull(entityPairCheck);
        assertEquals(entityPair, entityPairCheck);
    }
    
    public void testPropertySerializableValue() throws Exception
    {
        final Serializable serializableValue = new CompositeName("123");
        RetryingTransactionCallback<Pair<Long, Serializable>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>()
        {
            public Pair<Long, Serializable> execute() throws Throwable
            {
                return propertyValueDAO.createPropertySerializableValue(serializableValue);
            }
        };
        final Pair<Long, Serializable> entityPair = txnHelper.doInTransaction(createValueCallback, false);
        assertNotNull(entityPair);
        assertEquals(serializableValue, entityPair.getSecond());
        
        RetryingTransactionCallback<Pair<Long, Serializable>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>()
        {
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
        final Serializable bytes = (Serializable) new byte[] {1, 2, 3};
        RetryingTransactionCallback<Pair<Long, Void>> testBytesCallback = new RetryingTransactionCallback<Pair<Long, Void>>()
        {
            public Pair<Long, Void> execute() throws Throwable
            {
                Long id = propertyValueDAO.createPropertySerializableValue(bytes).getFirst();
                Serializable check = propertyValueDAO.getPropertySerializableValueById(id).getSecond();
                assertNotNull(check);
                assertTrue(check instanceof byte[]);
                Arrays.areEqual((byte[])bytes, (byte[])check);
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
        RetryingTransactionCallback<Pair<Long, Serializable>> createValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>()
        {
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
            RetryingTransactionCallback<Pair<Long, Serializable>> getValueCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>()
            {
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
        RetryingTransactionCallback<Pair<Long, Serializable>> getByIdCallback = new RetryingTransactionCallback<Pair<Long, Serializable>>()
        {
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
    
    public void testPropertyValue_Null() throws Exception
    {
        runPropertyValueTest(null);
    }
    
    public void testPropertyValue_Boolean() throws Exception
    {
        runPropertyValueTest(Boolean.TRUE);
        runPropertyValueTest(Boolean.FALSE);
    }
    
    public void testPropertyValue_Short() throws Exception
    {
        for (short i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Short(i));
        }
    }
    
    public void testPropertyValue_Integer() throws Exception
    {
        for (int i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Integer(i));
        }
    }
    
    public void testPropertyValue_Long() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Long(i));
        }
    }
    
    public void testPropertyValue_Float() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Float((float)i + 0.01F));
        }
    }
    
    public void testPropertyValue_Double() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Double((double)i + 0.01D));
        }
    }
    
    public void testPropertyValue_Date() throws Exception
    {
        Random rand = new Random();
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new Date(rand.nextLong()));
        }
    }
    
    public void testPropertyValue_String() throws Exception
    {
        for (long i = 0; i < 100; i++)
        {
            runPropertyValueTest(new String("Value-" + i + ".xyz"));
        }
    }
    
    public void testPropertyValue_Serializable() throws Exception
    {
        for (int i = 0; i < 100; i++)
        {
            // Choose a type that implements equals and hashCode but will not be recognised
            runPropertyValueTest(new CompositeName("Name-"+i), false);
        }
    }
    
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
    
    public void testPropertyValue_MapOfMapOfSerializables() throws Exception
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
        runPropertyValueTest(mapOuter, false);
    }
    
    public void testPropertyValue_MapOfMapOfStrings() throws Exception
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
        runPropertyValueTest(mapOuter, false);
    }
    
    public void testPropertyValue_CollectionOfStrings() throws Exception
    {
        final ArrayList<String> list = new ArrayList<String>(20);
        for (int i = 0; i < 20; i++)
        {
            String value = "COLL-VALUE-" + i;
            list.add(value);
        }
        runPropertyValueTest(list, false);
    }
    
    public void testPropertyClass_NoCache() throws Exception
    {
        removeCaches();
        testPropertyClass();
    }

    public void testPropertyDateValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertyDateValue();
    }
    
    public void testPropertyStringValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertyStringValue();
    }

    public void testPropertyDoubleValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertyDoubleValue();
    }

    public void testPropertySerializableValue_NoCache() throws Exception
    {
        removeCaches();
        testPropertySerializableValue();
    }
}
