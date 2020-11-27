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
package org.alfresco.util;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link ValueProtectingMap}
 *
 * @author Derek Hulley
 * @since 3.4.9
 * @since 4.0.1
 */
public class ValueProtectingMapTest
{

    private static final int WARMUP_ITERATIONS = 5000;

    private static final int BENCHMARK_ITERATIONS = 1000;

    private static final Set<Class<?>> moreImmutableClasses = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(TestImmutable.class)));

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
            this.i++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + i;
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestMutable other = (TestMutable) obj;
            if (i != other.i)
                return false;
            return true;
        }
    }

    /**
     * No matter how many times we wrap instances in instances, the backing map/data must remain
     * shared as long as there are no direct modifications against wrapping maps.
     */
    @Test
    public void multiLayeredWrapping()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> firstLayer = new ValueProtectingMap<>(map, moreImmutableClasses);
        ValueProtectingMap<String, Serializable> secondLayer = new ValueProtectingMap<>(firstLayer, moreImmutableClasses);
        ValueProtectingMap<String, Serializable> thirdLayer = new ValueProtectingMap<>(secondLayer, moreImmutableClasses);

        Assert.assertEquals(mutable, firstLayer.get("mutable"));
        Assert.assertEquals(mutable, secondLayer.get("mutable"));
        Assert.assertEquals(mutable, thirdLayer.get("mutable"));
        Assert.assertSame(immutable, firstLayer.get("immutable"));
        Assert.assertSame(immutable, secondLayer.get("immutable"));
        Assert.assertSame(immutable, thirdLayer.get("immutable"));

        mutable.increment();

        Assert.assertEquals(mutable, firstLayer.get("mutable"));
        Assert.assertEquals(mutable, secondLayer.get("mutable"));
        Assert.assertEquals(mutable, thirdLayer.get("mutable"));
        Assert.assertSame(immutable, firstLayer.get("immutable"));
        Assert.assertSame(immutable, secondLayer.get("immutable"));
        Assert.assertSame(immutable, thirdLayer.get("immutable"));

        // when first layer is modified, other layers should still have the shared state
        // test removal change
        firstLayer.remove("mutable");

        mutable.increment();
        map.put("immutable", altImmutable);

        Assert.assertNull(firstLayer.get("mutable"));
        Assert.assertEquals(mutable, secondLayer.get("mutable"));
        Assert.assertEquals(mutable, thirdLayer.get("mutable"));
        Assert.assertSame(immutable, firstLayer.get("immutable"));
        Assert.assertSame(altImmutable, secondLayer.get("immutable"));
        Assert.assertSame(altImmutable, thirdLayer.get("immutable"));

        // when second layer is modified, third layer should still have the shared state
        // test update change
        TestMutable secondLayerMutable = (TestMutable) secondLayer.get("mutable");
        secondLayerMutable.increment();
        secondLayer.put("mutable", secondLayerMutable);

        map.remove("immutable");

        Assert.assertNull(firstLayer.get("mutable"));
        Assert.assertEquals(secondLayerMutable, secondLayer.get("mutable"));
        Assert.assertEquals(mutable, thirdLayer.get("mutable"));
        Assert.assertSame(immutable, firstLayer.get("immutable"));
        Assert.assertSame(altImmutable, secondLayer.get("immutable"));
        Assert.assertNull(thirdLayer.get("immutable"));
    }

    @Test
    public void clear()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertFalse(protectingMap.isEmpty());
        Assert.assertEquals(2, protectingMap.size());

        map.clear();

        Assert.assertTrue(protectingMap.isEmpty());
        Assert.assertEquals(0, protectingMap.size());

        map.put("mutable", mutable);
        map.put("immutable", immutable);

        Assert.assertFalse(protectingMap.isEmpty());
        Assert.assertEquals(2, protectingMap.size());

        protectingMap.clear();

        // expect state change to be limited to wrapping map
        Assert.assertTrue(protectingMap.isEmpty());
        Assert.assertEquals(0, protectingMap.size());
        Assert.assertNull(protectingMap.get("mutable"));
        Assert.assertNull(protectingMap.get("immutable"));
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2, map.size());

        // expect state to be decoupled
        map.put("mutable2", mutable);

        Assert.assertTrue(protectingMap.isEmpty());
        Assert.assertEquals(0, protectingMap.size());
        Assert.assertNull(protectingMap.get("mutable2"));
    }

    @Test
    public void remove()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertEquals(2, protectingMap.size());
        Assert.assertEquals(mutable, protectingMap.get("mutable"));

        // expect state to be coupled
        mutable.increment();

        Assert.assertEquals(mutable, protectingMap.get("mutable"));

        protectingMap.remove("immutable");

        // expect state change to be limited to wrapping map
        Assert.assertEquals(1, protectingMap.size());
        Assert.assertNull(protectingMap.get("immutable"));
        Assert.assertEquals(2, map.size());

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
        map.put("mutable2", mutable);
        Assert.assertEquals(1, protectingMap.size());
        Assert.assertNull(protectingMap.get("mutable2"));
    }

    @Test
    public void put()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertEquals(2, protectingMap.size());
        Assert.assertEquals(mutable, protectingMap.get("mutable"));

        // expect state to be coupled
        mutable.increment();

        Assert.assertEquals(mutable, protectingMap.get("mutable"));

        protectingMap.put("immutable2", altImmutable);

        // expect state change to be limited to wrapping map
        Assert.assertEquals(3, protectingMap.size());
        Assert.assertEquals(2, map.size());
        Assert.assertNull(map.get("immutable2"));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
        map.put("mutable2", mutable);
        Assert.assertEquals(3, protectingMap.size());
        Assert.assertNull(protectingMap.get("mutable2"));
    }

    @Test
    public void putAll()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        Map<String, Serializable> updates = new HashMap<>();
        updates.put("immutable2", altImmutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertEquals(2, protectingMap.size());
        Assert.assertEquals(mutable, protectingMap.get("mutable"));

        // expect state to be coupled
        mutable.increment();

        Assert.assertEquals(mutable, protectingMap.get("mutable"));

        protectingMap.putAll(updates);

        // expect state change to be limited to wrapping map
        Assert.assertEquals(3, protectingMap.size());
        Assert.assertEquals(2, map.size());
        Assert.assertNull(map.get("immutable2"));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
        map.put("mutable2", mutable);
        Assert.assertEquals(3, protectingMap.size());
        Assert.assertNull(protectingMap.get("mutable2"));
    }

    @Test
    public void get()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertNotSame(mutable, protectingMap.get("mutable"));
        Assert.assertEquals(mutable, protectingMap.get("mutable"));
        Assert.assertSame(immutable, protectingMap.get("immutable"));

        mutable.increment();
        Assert.assertNotSame(mutable, protectingMap.get("mutable"));
        Assert.assertEquals(mutable, protectingMap.get("mutable"));
        Assert.assertSame(immutable, protectingMap.get("immutable"));

        map.put("immutable2", altImmutable);
        Assert.assertNotSame(mutable, protectingMap.get("mutable"));
        Assert.assertSame(immutable, protectingMap.get("immutable"));
        Assert.assertSame(altImmutable, protectingMap.get("immutable2"));

        // test that first remove decouples state and remove on original is not reflected
        protectingMap.remove("immutable");
        map.remove("immutable2");
        Assert.assertNotSame(mutable, protectingMap.get("mutable"));
        Assert.assertNull(protectingMap.get("immutable"));
        Assert.assertSame(altImmutable, protectingMap.get("immutable2"));
    }

    @Test
    public void containsKey()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertTrue(protectingMap.containsKey("mutable"));
        Assert.assertTrue(protectingMap.containsKey("immutable"));
        Assert.assertFalse(protectingMap.containsKey("immutable2"));

        map.remove("mutable");
        map.put("immutable2", altImmutable);
        Assert.assertFalse(protectingMap.containsKey("mutable"));
        Assert.assertTrue(protectingMap.containsKey("immutable"));
        Assert.assertTrue(protectingMap.containsKey("immutable2"));

        // test that first remove decouples state and remove on original is not reflected
        protectingMap.remove("immutable");
        map.remove("immutable2");
        Assert.assertFalse(protectingMap.containsKey("mutable"));
        Assert.assertFalse(protectingMap.containsKey("immutable"));
        Assert.assertTrue(protectingMap.containsKey("immutable2"));
    }

    @Test
    public void containsValue()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Assert.assertTrue(protectingMap.containsValue(mutable));
        Assert.assertTrue(protectingMap.containsValue(immutable));
        Assert.assertFalse(protectingMap.containsValue(altImmutable));

        map.remove("mutable");
        map.put("immutable2", altImmutable);
        Assert.assertFalse(protectingMap.containsValue(mutable));
        Assert.assertTrue(protectingMap.containsValue(immutable));
        Assert.assertTrue(protectingMap.containsValue(altImmutable));

        // test that first remove decouples state and remove on original is not reflected
        protectingMap.remove("immutable");
        map.remove("immutable2");
        Assert.assertFalse(protectingMap.containsValue(mutable));
        Assert.assertFalse(protectingMap.containsValue(immutable));
        Assert.assertTrue(protectingMap.containsValue(altImmutable));
    }

    @Test
    public void keySet()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Set<String> keySet = protectingMap.keySet();

        Assert.assertTrue(keySet.contains("mutable"));
        Assert.assertTrue(keySet.contains("immutable"));

        map.put("immutable2", altImmutable);

        Assert.assertTrue(keySet.contains("immutable2"));

        protectingMap.remove("immutable");

        Assert.assertFalse(keySet.contains("immutable"));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));

        map.remove("immutable2");

        Assert.assertTrue(keySet.contains("immutable2"));
    }

    @Test
    public void keySetIterator()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Set<String> keySet = protectingMap.keySet();

        Iterator<String> iterator = keySet.iterator();
        Set<String> iteratedKeys = new HashSet<>();
        iterator.forEachRemaining(iteratedKeys::add);
        Assert.assertTrue(keySet.containsAll(iteratedKeys));
        Assert.assertTrue(iteratedKeys.containsAll(keySet));

        map.put("immutable2", altImmutable);

        Assert.assertTrue(keySet.contains("immutable2"));
        iterator = keySet.iterator();
        iteratedKeys = new HashSet<>();
        iterator.forEachRemaining(iteratedKeys::add);
        Assert.assertTrue(keySet.containsAll(iteratedKeys));
        Assert.assertTrue(iteratedKeys.containsAll(keySet));

        iterator = keySet.iterator();
        for (String k = iterator.next(); true; k = iterator.next())
        {
            if ("immutable2".equals(k))
            {
                iterator.remove();
                break;
            }
        }

        Assert.assertFalse(keySet.contains("immutable2"));
        Assert.assertFalse(protectingMap.containsKey("immutable2"));
        Assert.assertTrue(map.containsKey("immutable2"));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
    }

    @Test
    public void values()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Collection<Serializable> values = protectingMap.values();

        Assert.assertTrue(values.contains(mutable));
        Assert.assertTrue(values.contains(immutable));

        map.put("immutable2", altImmutable);

        Assert.assertTrue(values.contains(altImmutable));

        protectingMap.remove("immutable");

        Assert.assertFalse(values.contains(immutable));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));

        map.remove("immutable2");

        Assert.assertTrue(values.contains(altImmutable));
    }

    @Test
    public void valuesIterator()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Collection<Serializable> values = protectingMap.values();

        Iterator<Serializable> iterator = values.iterator();
        Set<Serializable> iteratedValues = new HashSet<>();
        iterator.forEachRemaining(iteratedValues::add);
        Assert.assertTrue(values.containsAll(iteratedValues));
        Assert.assertTrue(iteratedValues.containsAll(values));

        map.put("immutable2", altImmutable);

        Assert.assertTrue(values.contains(altImmutable));
        iterator = values.iterator();
        iteratedValues = new HashSet<>();
        iterator.forEachRemaining(iteratedValues::add);
        Assert.assertTrue(values.containsAll(iteratedValues));
        Assert.assertTrue(iteratedValues.containsAll(values));

        iterator = values.iterator();
        for (Serializable v = iterator.next(); true; v = iterator.next())
        {
            if (altImmutable.equals(v))
            {
                iterator.remove();
                break;
            }
        }

        Assert.assertFalse(values.contains(altImmutable));
        Assert.assertFalse(protectingMap.containsValue(altImmutable));
        Assert.assertTrue(map.containsValue(altImmutable));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
    }

    @Test
    public void entrySet()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Collection<Entry<String, Serializable>> entrySet = protectingMap.entrySet();

        Assert.assertEquals(2, entrySet.size());

        entrySet.forEach(e -> {
            Assert.assertTrue(map.containsKey(e.getKey()));
            Assert.assertEquals(map.get(e.getKey()), e.getValue());
        });

        map.put("immutable2", altImmutable);

        Assert.assertEquals(3, entrySet.size());

        entrySet.forEach(e -> {
            Assert.assertTrue(map.containsKey(e.getKey()));
            Assert.assertEquals(map.get(e.getKey()), e.getValue());
        });

        protectingMap.remove("immutable");

        Assert.assertEquals(2, entrySet.size());

        entrySet.forEach(e -> {
            Assert.assertTrue(map.containsKey(e.getKey()));
            Assert.assertEquals(map.get(e.getKey()), e.getValue());
        });

        map.remove("immutable2");

        Assert.assertEquals(2, entrySet.size());

        entrySet.forEach(e -> {
            if (!"immutable2".equals(e.getKey()))
            {
                Assert.assertTrue(map.containsKey(e.getKey()));
                Assert.assertEquals(map.get(e.getKey()), e.getValue());
            }
        });
    }

    @Test
    public void entrySetIteratorRemove()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Collection<Entry<String, Serializable>> entrySet = protectingMap.entrySet();

        Iterator<Entry<String, Serializable>> iterator = entrySet.iterator();
        Set<Entry<String, Serializable>> iteratedEntries = new HashSet<>();
        iterator.forEachRemaining(iteratedEntries::add);
        Assert.assertTrue(entrySet.containsAll(iteratedEntries));
        Assert.assertTrue(iteratedEntries.containsAll(entrySet));

        map.put("immutable2", altImmutable);

        Assert.assertTrue(entrySet.contains(new SimpleEntry<>("immutable2", altImmutable)));
        iterator = entrySet.iterator();
        iteratedEntries = new HashSet<>();
        iterator.forEachRemaining(iteratedEntries::add);
        Assert.assertTrue(entrySet.containsAll(iteratedEntries));
        Assert.assertTrue(iteratedEntries.containsAll(entrySet));

        iterator = entrySet.iterator();
        for (Entry<String, Serializable> e = iterator.next(); true; e = iterator.next())
        {
            if ("immutable2".equals(e.getKey()))
            {
                iterator.remove();
                break;
            }
        }

        Assert.assertFalse(entrySet.contains(new SimpleEntry<>("immutable2", altImmutable)));
        Assert.assertFalse(protectingMap.containsValue(altImmutable));
        Assert.assertTrue(map.containsValue(altImmutable));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
    }

    @Test
    public void entrySetIteratorValueUpdate()
    {
        Map<String, Serializable> map = new HashMap<>();
        TestMutable mutable = new TestMutable();
        TestImmutable immutable = new TestImmutable();
        TestImmutable altImmutable = new TestImmutable();
        map.put("mutable", mutable);
        map.put("immutable", immutable);

        ValueProtectingMap<String, Serializable> protectingMap = new ValueProtectingMap<>(map, moreImmutableClasses);

        Collection<Entry<String, Serializable>> entrySet = protectingMap.entrySet();

        Iterator<Entry<String, Serializable>> iterator = entrySet.iterator();
        Set<Entry<String, Serializable>> iteratedEntries = new HashSet<>();
        iterator.forEachRemaining(iteratedEntries::add);
        Assert.assertTrue(entrySet.containsAll(iteratedEntries));
        Assert.assertTrue(iteratedEntries.containsAll(entrySet));

        map.put("immutable2", altImmutable);

        Assert.assertTrue(entrySet.contains(new SimpleEntry<>("immutable2", altImmutable)));
        iterator = entrySet.iterator();
        iteratedEntries = new HashSet<>();
        iterator.forEachRemaining(iteratedEntries::add);
        Assert.assertTrue(entrySet.containsAll(iteratedEntries));
        Assert.assertTrue(iteratedEntries.containsAll(entrySet));

        iterator = entrySet.iterator();
        for (Entry<String, Serializable> e = iterator.next(); true; e = iterator.next())
        {
            if ("immutable2".equals(e.getKey()))
            {
                e.setValue(immutable);
                break;
            }
        }

        Assert.assertFalse(entrySet.contains(new SimpleEntry<>("immutable2", altImmutable)));
        Assert.assertTrue(entrySet.contains(new SimpleEntry<>("immutable2", immutable)));
        Assert.assertSame(immutable, protectingMap.get("immutable2"));
        Assert.assertSame(altImmutable, map.get("immutable2"));

        // expect state to be decoupled
        mutable.increment();
        Assert.assertNotEquals(mutable, protectingMap.get("mutable"));
    }

    @Test
    public void benchmark_immutableGet()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<Serializable> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            dummyList.set(0, copy.get("immutable"));
        };
        runBenchmark(run, map, "benchmark_immutableGet");
    }

    @Test
    public void benchmark_mutableGet()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            ((TestMutable) copy.get("mutable")).increment();
        };
        runBenchmark(run, map, "benchmark_mutableGet");
    }

    @Test
    public void benchmark_put()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            copy.put("immutableNew", new TestImmutable());
        };
        runBenchmark(run, map, "benchmark_put");
    }

    @Test
    public void benchmark_keySetIteration()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<String> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            copy.keySet().forEach(e -> dummyList.set(0, e));
        };
        runBenchmark(run, map, "benchmark_keySetIteration");
    }

    @Test
    public void benchmark_valuesIteration()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<Serializable> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            copy.values().forEach(e -> dummyList.set(0, e));
        };
        runBenchmark(run, map, "benchmark_valuesIteration");
    }

    @Test
    public void benchmark_entrySetImmutableReadyOnlyIteration()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<Serializable> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            copy.entrySet().forEach(e -> {
                if (e.getKey().startsWith("immutable"))
                {
                    dummyList.set(0, e.getValue());
                }
            });
        };
        runBenchmark(run, map, "benchmark_entrySetImmutableReadyOnlyIteration");
    }

    @Test
    public void benchmark_entrySetMutableReadyOnlyIteration()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<Serializable> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            copy.entrySet().forEach(e -> {
                if (e.getKey().startsWith("mutable"))
                {
                    dummyList.set(0, e.getValue());
                }
            });
        };
        runBenchmark(run, map, "benchmark_entrySetMutableReadyOnlyIteration");
    }
    
    @Test
    public void benchmark_entrySetMixedReadyOnlyIteration()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<Serializable> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            copy.entrySet().forEach(e -> {
                dummyList.set(0, e.getValue());
            });
        };
        runBenchmark(run, map, "benchmark_entrySetMixedReadyOnlyIteration");
    }

    @Test
    public void benchmark_entrySetIterationWithSingleUpdate()
    {
        Map<String, Serializable> map = buildBenchmarkMap();
        // list only used to limit/avoid compiler optimising away iterations as nothing of consequence
        // would be done if we only do get without acting on the return value
        final LinkedList<Serializable> dummyList = new LinkedList<>();
        dummyList.add(0, null);
        Consumer<Map<String, Serializable>> run = m -> {
            Map<String, Serializable> copy = new ValueProtectingMap<>(m, moreImmutableClasses);
            AtomicBoolean first = new AtomicBoolean(true);
            copy.entrySet().forEach(e -> {
                dummyList.set(0, e.getValue());
                if (first.compareAndSet(true, false))
                {
                    e.setValue(new TestImmutable());
                }
            });
        };
        runBenchmark(run, map, "benchmark_entrySetIterationWithSingleUpdate");
    }

    protected Map<String, Serializable> buildBenchmarkMap()
    {
        Map<String, Serializable> map = new HashMap<>();

        map.put("mutable", new TestMutable());
        map.put("mutable1", new TestMutable());
        map.put("mutable2", new TestMutable());
        map.put("mutable3", new TestMutable());
        map.put("mutable4", new TestMutable());
        map.put("mutable5", new TestMutable());
        map.put("immutable", new TestImmutable());
        map.put("immutable1", new TestImmutable());
        map.put("immutable2", new TestImmutable());
        map.put("immutable3", new TestImmutable());
        map.put("immutable4", new TestImmutable());
        map.put("immutable5", new TestImmutable());

        map = new ValueProtectingMap<>(map, moreImmutableClasses);
        return map;
    }

    protected void runBenchmark(Consumer<Map<String, Serializable>> benchmarkOp, Map<String, Serializable> map, String benchmarkName)
    {
        for (int iter = 0; iter < WARMUP_ITERATIONS; iter++)
        {
            benchmarkOp.accept(map);
        }

        long start = System.nanoTime();
        for (int iter = 0; iter < BENCHMARK_ITERATIONS; iter++)
        {
            benchmarkOp.accept(map);
        }
        long end = System.nanoTime();

        System.out.println(benchmarkName + " - Total: " + ((end - start) * 1.0d / 1000000) + "ms");
        System.out.println(benchmarkName + " - Per iteration: " + ((end - start) * 1.0d / BENCHMARK_ITERATIONS) + "ns");
    }
}