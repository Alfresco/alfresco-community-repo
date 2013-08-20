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
package org.alfresco.repo.cache.lookup;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * A cache for two-way lookups of database entities.  These are characterized by having a unique
 * key (perhaps a database ID) and a separate unique key that identifies the object.
 * <p>
 * The keys must have good <code>equals</code> and </code>hashCode</code> implementations and
 * must respect the case-sensitivity of the use-case.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class EntityLookupCacheTest extends TestCase implements EntityLookupCallbackDAO<Long, Object, String>
{
    SimpleCache<Long, Object> cache;
    private EntityLookupCache<Long, Object, String> entityLookupCacheA;
    private EntityLookupCache<Long, Object, String> entityLookupCacheB;
    private TreeMap<Long, String> database;

    @Override
    protected void setUp() throws Exception
    {
        cache = new MemoryCache<Long, Object>();
        entityLookupCacheA = new EntityLookupCache<Long, Object, String>(cache, "A", this);
        entityLookupCacheB = new EntityLookupCache<Long, Object, String>(cache, "B", this);
        database = new TreeMap<Long, String>();
    }
    
    public void testLookupsUsingIncorrectValue() throws Exception
    {
        try
        {
            // Keep the "database" empty
            entityLookupCacheA.getByValue(this);
        }
        catch (AssertionFailedError e)
        {
            // Expected
        }
    }
    
    public void testLookupAgainstEmpty() throws Exception
    {
        TestValue value = new TestValue("AAA");
        Pair<Long, Object> entityPair = entityLookupCacheA.getByValue(value);
        assertNull(entityPair);
        assertTrue(database.isEmpty());
        
        // Now do lookup or create
        entityPair = entityLookupCacheA.getOrCreateByValue(value);
        assertNotNull("Expected a value to be found", entityPair);
        Long entityId = entityPair.getFirst();
        assertTrue("Database ID should have been created", database.containsKey(entityId));
        assertEquals("Database value incorrect", value.val, database.get(entityId));
        
        // Do lookup or create again
        entityPair = entityLookupCacheA.getOrCreateByValue(value);
        assertNotNull("Expected a value to be found", entityPair);
        assertEquals("Expected same entity ID", entityId, entityPair.getFirst());
        
        // Look it up using the value
        entityPair = entityLookupCacheA.getByValue(value);
        assertNotNull("Lookup after create should work", entityPair);
        
        // Look it up using the ID
        entityPair = entityLookupCacheA.getByKey(entityId);
        assertNotNull("Lookup by key should work after create", entityPair);
        assertTrue("Looked-up type incorrect", entityPair.getSecond() instanceof TestValue);
        assertEquals("Looked-up type value incorrect", value, entityPair.getSecond());
    }
    
    public void testLookupAgainstExisting() throws Exception
    {
        // Put some values in the "database"
        createValue(new TestValue("AAA"));
        createValue(new TestValue("BBB"));
        createValue(new TestValue("CCC"));
        
        // Look up by value
        Pair<Long, Object> entityPair = entityLookupCacheA.getByValue(new TestValue("AAA"));
        assertNotNull("Expected value to be found", entityPair);
        assertEquals("ID is incorrect", new Long(1), entityPair.getFirst());
        
        // Look up by ID
        entityPair = entityLookupCacheA.getByKey(new Long(2));
        assertNotNull("Expected value to be found", entityPair);
        
        // Do lookup or create
        entityPair = entityLookupCacheA.getByValue(new TestValue("CCC"));
        assertNotNull("Expected value to be found", entityPair);
        assertEquals("ID is incorrect", new Long(3), entityPair.getFirst());
    }

    public void testRegions() throws Exception
    {
        TestValue valueAAA = new TestValue("AAA");
        Pair<Long, Object> entityPairAAA = entityLookupCacheA.getOrCreateByValue(valueAAA);
        assertNotNull(entityPairAAA);
        assertEquals("AAA", database.get(entityPairAAA.getFirst()));
        assertEquals(2, cache.getKeys().size());
        
        TestValue valueBBB = new TestValue("BBB");
        Pair<Long, Object> entityPairBBB = entityLookupCacheB.getOrCreateByValue(valueBBB);
        assertNotNull(entityPairBBB);
        assertEquals("BBB", database.get(entityPairBBB.getFirst()));
        assertEquals(4, cache.getKeys().size());
        
        // Now cross-check against the caches and make sure that the cache 
        entityPairBBB = entityLookupCacheA.getByValue(valueBBB);
        assertEquals(6, cache.getKeys().size());
        entityPairBBB = entityLookupCacheB.getByValue(valueAAA);
        assertEquals(8, cache.getKeys().size());
    }

    public void testNullLookups() throws Exception
    {
        TestValue valueNull = null;
        Pair<Long, Object> entityPairNull = entityLookupCacheA.getOrCreateByValue(valueNull);
        assertNotNull(entityPairNull);
        assertTrue(database.containsKey(entityPairNull.getFirst()));
        assertNull(database.get(entityPairNull.getFirst()));
        assertEquals(2, cache.getKeys().size());
        
        // Look it up again
        Pair<Long, Object> entityPairCheck = entityLookupCacheA.getOrCreateByValue(valueNull);
        assertNotNull(entityPairNull);
        assertTrue(database.containsKey(entityPairNull.getFirst()));
        assertNull(database.get(entityPairNull.getFirst()));
        assertEquals(entityPairNull, entityPairCheck);
    }
    
    public void testUpdate() throws Exception
    {
        TestValue valueOne = new TestValue(getName() + "-ONE");
        TestValue valueTwo = new TestValue(getName() + "-TWO");
        Pair<Long, Object> entityPairOne = entityLookupCacheA.getOrCreateByValue(valueOne);
        assertNotNull(entityPairOne);
        Long id = entityPairOne.getFirst();
        assertEquals(valueOne.val, database.get(id));
        assertEquals(2, cache.getKeys().size());
        
        // Update
        int updateCount = entityLookupCacheA.updateValue(id, valueTwo);
        assertEquals("Update count was incorrect.", 1, updateCount);
        assertEquals(valueTwo.val, database.get(id));
        assertEquals(2, cache.getKeys().size());
    }
    
    public void testDeleteByKey() throws Exception
    {
        TestValue valueOne = new TestValue(getName() + "-ONE");
        Pair<Long, Object> entityPairOne = entityLookupCacheA.getOrCreateByValue(valueOne);
        assertNotNull(entityPairOne);
        Long id = entityPairOne.getFirst();
        assertEquals(valueOne.val, database.get(id));
        assertEquals(2, cache.getKeys().size());
        
        // Delete
        int deleteCount = entityLookupCacheA.deleteByKey(id);
        assertEquals("Delete count was incorrect.", 1, deleteCount);
        assertNull(database.get(id));
        assertEquals(0, cache.getKeys().size());
    }
    
    public void testDeleteByValue() throws Exception
    {
        TestValue valueOne = new TestValue(getName() + "-ONE");
        Pair<Long, Object> entityPairOne = entityLookupCacheA.getOrCreateByValue(valueOne);
        assertNotNull(entityPairOne);
        Long id = entityPairOne.getFirst();
        assertEquals(valueOne.val, database.get(id));
        assertEquals(2, cache.getKeys().size());
        
        // Delete
        int deleteCount = entityLookupCacheA.deleteByValue(valueOne);
        assertEquals("Delete count was incorrect.", 1, deleteCount);
        assertNull(database.get(id));
        assertEquals(0, cache.getKeys().size());
    }
    
    public void testClear() throws Exception
    {
        TestValue valueOne = new TestValue(getName() + "-ONE");
        Pair<Long, Object> entityPairOne = entityLookupCacheA.getOrCreateByValue(valueOne);
        assertNotNull(entityPairOne);
        Long id = entityPairOne.getFirst();
        assertEquals(valueOne.val, database.get(id));
        assertEquals(2, cache.getKeys().size());
        
        // Clear it
        entityLookupCacheA.clear();
        assertEquals(valueOne.val, database.get(id));               // Must still be in database
        assertEquals(0, cache.getKeys().size());                    // ... but cache must be empty
    }

    /**
     * Helper class to represent business object
     */
    private static class TestValue
    {
        private final String val;
        private TestValue(String val)
        {
            this.val = val;
        }
        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || !(obj instanceof TestValue))
            {
                return false;
            }
            return val.equals( ((TestValue)obj).val );
        }
        @Override
        public int hashCode()
        {
            return val.hashCode();
        }
        
    }
    
    public String getValueKey(Object value)
    {
        assertNotNull(value);
        assertTrue(value instanceof TestValue);
        String dbValue = ((TestValue)value).val;
        return dbValue;
    }

    public Pair<Long, Object> findByKey(Long key)
    {
        assertNotNull(key);

        String dbValue = database.get(key);
        if (dbValue == null)
        {
            return null;
        }
        // Make a value object
        TestValue value = new TestValue(dbValue);
        return new Pair<Long, Object>(key, value);
    }

    public Pair<Long, Object> findByValue(Object value)
    {
        assertTrue(value == null || value instanceof TestValue);
        String dbValue = (value == null) ? null : ((TestValue)value).val;

        for (Map.Entry<Long, String> entry : database.entrySet())
        {
            if (EqualsHelper.nullSafeEquals(entry.getValue(), dbValue))
            {
                return new Pair<Long, Object>(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    /**
     * Simulate creation of a new database entry
     */
    public Pair<Long, Object> createValue(Object value)
    {
        assertTrue(value == null || value instanceof TestValue);
        String dbValue = (value == null) ? null : ((TestValue)value).val;
        
        // Get the last key
        Long lastKey = database.isEmpty() ? null : database.lastKey();
        Long newKey = null;
        if (lastKey == null)
        {
            newKey = new Long(1);
        }
        else
        {
            newKey = new Long(lastKey.longValue() + 1);
        }
        database.put(newKey, dbValue);
        return new Pair<Long, Object>(newKey, value);
    }

    public int updateValue(Long key, Object value)
    {
        assertNotNull(key);
        assertTrue(value == null || value instanceof TestValue);
        
        // Find it
        Pair<Long, Object> entityPair = findByKey(key);
        if (entityPair == null)
        {
            return 0;
        }
        else
        {
            database.put(key, ((TestValue)value).val);
            return 1;
        }
    }

    public int deleteByKey(Long key)
    {
        assertNotNull(key);
        
        if (database.containsKey(key))
        {
            database.remove(key);
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public int deleteByValue(Object value)
    {
        assertTrue(value == null || value instanceof TestValue);
        
        // Find it
        Pair<Long, Object> entityPair = findByValue(value);
        if (entityPair == null)
        {
            return 0;
        }
        else
        {
            database.remove(entityPair.getFirst());
            return 1;
        }
    }
}
