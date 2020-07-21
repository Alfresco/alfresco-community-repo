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
package org.alfresco.util.collections;

import static java.util.Arrays.asList;

import static org.alfresco.util.collections.CollectionUtils.asSet;
import static org.alfresco.util.collections.CollectionUtils.nullSafeMerge;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CollectionUtils}.
 * 
 * @author Neil Mc Erlean
 */
public class CollectionUtilsTest
{
    private static Set<String>          stooges;
    
    private static Map<String, Integer> primes;
    private static Map<String, Integer> squares;
    private static Map<String, Integer> nullMap;
    private static Map<String, Integer> nerdsBirthdays;
    
    @Before public void initData()
    {
        stooges = new HashSet<>();
        stooges.add("Larry");
        stooges.add("Curly");
        stooges.add("Moe");
        
        primes = new HashMap<>();
        primes.put("two", 2);
        primes.put("three", 3);
        primes.put("five", 5);
        
        squares = new HashMap<>();
        squares.put("one", 1);
        squares.put("two", 4);
        squares.put("three", 9);
        
        nerdsBirthdays = new HashMap<>();
        nerdsBirthdays.put("Alan Turing",           1912);
        nerdsBirthdays.put("Charles Babbage",       1791);
        nerdsBirthdays.put("Matthew Smith",         1966);
        nerdsBirthdays.put("Paul Dirac",            1902);
        nerdsBirthdays.put("Robert Boyle",          1627);
        nerdsBirthdays.put("Robert Hooke",          1635);
        nerdsBirthdays.put("J. Robert Oppenheimer", 1904);
    }
    
    @Test public void varArgsAsSet() {
        assertEquals(stooges, asSet("Larry", "Curly", "Moe"));

        assertEquals(stooges, CollectionUtils.<String>asSet(String.class, "Larry", "Curly", "Moe"));
    }
    
    @Test public void nullSafeMergeMaps()
    {
        assertNull(nullSafeMerge(nullMap, nullMap, true));
        
        assertEquals(Collections.emptyMap(), nullSafeMerge(nullMap, nullMap));
        assertEquals(primes, nullSafeMerge(nullMap, primes));
        assertEquals(primes, nullSafeMerge(primes, nullMap));
        
        Map<String, Integer> primesAndSquares = new HashMap<>();
        primesAndSquares.putAll(primes);
        primesAndSquares.putAll(squares);
        
        assertEquals(primesAndSquares, nullSafeMerge(primes, squares));
    }
    
    @Test public void collectionFiltering() throws Exception
    {
        Function<String, Boolean> johnFilter = new KeySubstringFilter("John");
        assertEquals(0, CollectionUtils.filterKeys(nerdsBirthdays, johnFilter).size());
        
        Function<String, Boolean> robertFilter = new KeySubstringFilter("Robert");
        assertEquals(3, CollectionUtils.filterKeys(nerdsBirthdays, robertFilter).size());
    }
    
    private static final class KeySubstringFilter implements Function<String, Boolean>
    {
        private final String substring;
        public KeySubstringFilter(String substring) { this.substring = substring; }
        @Override public Boolean apply(String value)
        {
            return value.contains(substring);
        }
    }
    
    @Test public void sortMapsByEntry() throws Exception
    {
        final Map<String, Integer> expectedSorting = getNerdsSortedByBirthDate();
        
        Comparator<Entry<String, Integer>> entryComparator = new Comparator<Entry<String, Integer>>()
                {
                    @Override public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2)
                    {
                        return e1.getValue().intValue() - e2.getValue().intValue();
                    }
                };
        
        final Map<String, Integer> actualSorting = CollectionUtils.sortMapByValue(nerdsBirthdays, entryComparator);
        
        assertEquals(expectedSorting, actualSorting);
    }
    
    @Test public void sortMapsByValue() throws Exception
    {
        final Map<String, Integer> expectedSorting = getNerdsSortedByBirthDate();
        
        Comparator<Integer> valueComparator = new Comparator<Integer>()
                {
                    @Override public int compare(Integer i1, Integer i2)
                    {
                        return i1.intValue() - i2.intValue();
                    }
                };
        
        Comparator<Entry<String, Integer>> entryComparator = CollectionUtils.<String, Integer>toEntryComparator(valueComparator);
        
        final Map<String, Integer> actualSorting = CollectionUtils.sortMapByValue(nerdsBirthdays, entryComparator);
        
        assertEquals(expectedSorting, actualSorting);
    }
    
    private Map<String, Integer> getNerdsSortedByBirthDate()
    {
        final Map<String, Integer> result = new LinkedHashMap<>(); // maintains insertion order
        result.put("Robert Boyle",          1627);
        result.put("Robert Hooke",          1635);
        result.put("Charles Babbage",       1791);
        result.put("Paul Dirac",            1902);
        result.put("J. Robert Oppenheimer", 1904);
        result.put("Alan Turing",           1912);
        result.put("Matthew Smith",         1966);
        return result;
    }
    
    @Test public void moveItemInList() throws Exception
    {
        final List<String> input = asList("a", "b", "c");
        
        assertEquals(asList("a", "b", "c"), CollectionUtils.moveRight(0, "b", input));
        assertEquals(asList("a", "c", "b"), CollectionUtils.moveRight(1, "b", input));
        assertEquals(asList("a", "c", "b"), CollectionUtils.moveRight(5, "b", input));
        
        assertEquals(asList("c", "a", "b"), CollectionUtils.moveRight(-2, "c", input));
        assertEquals(asList("c", "a", "b"), CollectionUtils.moveRight(-5, "c", input));
        
        assertEquals(asList("a", "b", "c"), CollectionUtils.moveLeft(0, "b", input));
        assertEquals(asList("b", "a", "c"), CollectionUtils.moveLeft(1, "b", input));
        assertEquals(asList("b", "a", "c"), CollectionUtils.moveLeft(5, "b", input));
        
        assertEquals(asList("b", "c", "a"), CollectionUtils.moveLeft(-2, "a", input));
        assertEquals(asList("b", "c", "a"), CollectionUtils.moveLeft(-5, "a", input));
        
        try                                     { CollectionUtils.moveRight(1, "x", input); }
        catch (NoSuchElementException expected) { return; }
        
        fail("Expected exception was not thrown.");
    }
    
    @Test public void flattenCollections() throws Exception
    {
        final List<String> list1 = CollectionUtils.toListOfStrings(stooges);
        Collections.sort(list1);
        final List<String> list2 = asList("Hello", "World");
        
        assertEquals(asList("Curly", "Larry", "Moe", "Hello", "World"),
                     CollectionUtils.flatten(list1, list2));
    }
}
