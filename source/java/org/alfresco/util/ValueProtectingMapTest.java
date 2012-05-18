/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests {@link ValueProtectingMap}
 * 
 * @author Derek Hulley
 * @since 3.4.9
 * @since 4.0.1
 */
public class ValueProtectingMapTest extends TestCase
{
    private static Set<Class<?>> moreImmutableClasses;
    static
    {
        moreImmutableClasses = new HashSet<Class<?>>(13);
        moreImmutableClasses.add(TestImmutable.class);
    }
    
    /**
     * A class that is immutable
     */
    @SuppressWarnings("serial")
    private static class TestImmutable implements Serializable
    {
    }
    
    /**
     * A class that is mutable
     */
    @SuppressWarnings("serial")
    private static class TestMutable extends TestImmutable
    {
        public int i = 0;
        public void increment()
        {
            i++;
        }
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TestMutable other = (TestMutable) obj;
            if (i != other.i) return false;
            return true;
        }
    }
    
    private List<String> valueList;
    private Map<String, String> valueMap;
    private Date valueDate;
    private TestImmutable valueImmutable;
    private TestMutable valueMutable;
    
    private ValueProtectingMap<String, Serializable> map;
    private Map<String, Serializable> holyMap;

    @Override
    protected void setUp() throws Exception
    {
        valueList = new ArrayList<String>(4);
        valueList.add("ONE");
        valueList.add("TWO");
        valueList.add("THREE");
        valueList.add("FOUR");
        valueList = Collections.unmodifiableList(valueList);
        
        valueMap = new HashMap<String, String>(5);
        valueMap.put("ONE", "ONE");
        valueMap.put("TWO", "TWO");
        valueMap.put("THREE", "THREE");
        valueMap.put("FOUR", "FOUR");
        valueMap = Collections.unmodifiableMap(valueMap);
        
        valueDate = new Date();
        
        valueImmutable = new TestImmutable();
        valueMutable = new TestMutable();
        
        holyMap = new HashMap<String, Serializable>();
        holyMap.put("DATE", valueDate);
        holyMap.put("LIST", (Serializable) valueList);
        holyMap.put("MAP", (Serializable) valueMap);
        holyMap.put("IMMUTABLE", valueImmutable);
        holyMap.put("MUTABLE", valueMutable);
        
        // Now wrap our 'holy' map so that it cannot be modified
        holyMap = Collections.unmodifiableMap(holyMap);
        
        map = new ValueProtectingMap<String, Serializable>(holyMap, moreImmutableClasses);
    }
    
    /**
     * Make sure that NOTHING has changed in our 'holy' map
     */
    private void checkMaps(boolean expectMapClone)
    {
        assertEquals("Holy map size is wrong: ", 5, holyMap.size());
        // Note that the immutability of the maps and lists means that we don't need
        // to check every value within the lists and maps
        if (expectMapClone)
        {
            // Make sure that the holy map has been released
            assertTrue("Expect holy map to have been released: ", map.getProtectedMap() != holyMap);
            // Do some updates to the backing map and ensure that they stick
            Map<String, Serializable> mapClone = map.getProtectedMap();
            mapClone.put("ONE", "ONE");
            assertEquals("Modified the backing directly but value is not visible: ", map.get("ONE"), "ONE");
            map.put("TWO", "TWO");
            assertTrue("Backing map was changed again!", mapClone == map.getProtectedMap());
            mapClone.containsKey("TWO");
        }
        else
        {
            // Make sure that the holy map is still acting as the backing map
            assertTrue("Expect holy map to still be in use: ", map.getProtectedMap() == holyMap);
        }
    }
    
    public void testSetup()
    {
        checkMaps(false);
    }
    
    /**
     * No matter how many times we wrap instances in instances, the backing map must remain
     * the same.
     */
    public void testMapWrapping()
    {
        ValueProtectingMap<String, Serializable> mapTwo = new ValueProtectingMap<String, Serializable>(map);
        assertTrue("Backing map must be shared: ", mapTwo.getProtectedMap() == map.getProtectedMap());
        ValueProtectingMap<String, Serializable> mapThree = new ValueProtectingMap<String, Serializable>(map);
        assertTrue("Backing map must be shared: ", mapThree.getProtectedMap() == map.getProtectedMap());
    }
    
    public void testMapClear()
    {
        map.clear();
        assertEquals("Map should be empty: ", 0, map.size());
        checkMaps(true);
    }
    
    public void testMapContainsKey()
    {
        assertTrue(map.containsKey("LIST"));
        assertFalse(map.containsKey("LISTXXX"));
        checkMaps(false);
    }
    
    public void testMapContainsValue()
    {
        assertTrue(map.containsValue(valueMutable));
        assertFalse(map.containsValue("Dassie"));
        checkMaps(false);
    }
    
    public void testMapEntrySet()
    {
        map.entrySet();
        checkMaps(true);
    }
    
    /**
     * Ensures that single, immutable values are given out as-is and
     * without affecting the backing storage
     */
    public void testMapGetImmutable()
    {
        assertTrue("Immutable value instance incorrect", map.get("IMMUTABLE") == valueImmutable);
        checkMaps(false);
    }
    
    /**
     * Ensures that single, immutable values are cloned before being given out
     * without affecting the backing storage
     */
    public void testMapGetMutable()
    {
        TestMutable mutable = (TestMutable) map.get("MUTABLE");
        assertFalse("Mutable value instance incorrect", mutable == valueMutable);
        checkMaps(false);
        // Modify the instance
        mutable.increment();
        assertEquals("Backing mutable should not have changed: ", 0, valueMutable.i);
    }
    
    public void testMapIsEmpty()
    {
        assertFalse(map.isEmpty());
        checkMaps(false);
    }
    
    public void testMapKeySet()
    {
        map.keySet();
        checkMaps(true);
    }
    
    public void testMapPut()
    {
        map.put("ANOTHER", "VALUE");
        checkMaps(true);
    }
    
    public void testMapPutAll()
    {
        map.putAll(holyMap);
        checkMaps(true);
    }
    
    @SuppressWarnings("unchecked")
    public void testSerializability() throws Exception
    {
        map.put("MORE", "STUFF");
        checkMaps(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(map);
        os.close();
        // Read it back in
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ValueProtectingMap<String, Serializable> reloadedMap = (ValueProtectingMap<String, Serializable>) ois.readObject();
        ois.close();
        // Make sure it has the value
        assertEquals("Reloaded object not same.", "STUFF", reloadedMap.get("MORE"));
    }
}
