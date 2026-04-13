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

package org.alfresco.util.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.util.Pair;

/**
 * @author Nick Smith
 * @author Neil Mc Erlean
 * @since 4.0
 */
public abstract class CollectionUtils
{
    public static boolean isEmpty(Map<?, ?> map)
    {
        if (map == null)
        {
            return true;
        }
        return map.isEmpty();
    }
    
    public static boolean isEmpty(Collection<?> items)
    {
        if(items == null)
        {
            return true;
        }
        return items.isEmpty();
    }
    
    /**
     * This method merges two sets returning the union of both sets.
     * 
     * @param first  first set. can be null.
     * @param second second set. can be null.
     * @return the union of both sets. will not be null
     */
    public static <T> Set<T> nullSafeMerge(Set<T> first, Set<T> second)
    {
        return nullSafeMerge(first, second, false);
    }
    
    /**
     * This method merges two sets returning the union of both sets.
     * 
     * @param first  first set. can be null.
     * @param second second set. can be null.
     * @param emptyResultIsNull if the result is empty, should we return null?
     * @return the union of both sets or null.
     */
    public static <T> Set<T> nullSafeMerge(Set<T> first, Set<T> second, boolean emptyResultIsNull)
    {
        Set<T> result = new HashSet<T>();
        
        if (first != null) result.addAll(first);
        if (second != null) result.addAll(second);
        
        if (result.isEmpty() && emptyResultIsNull)
        {
            result = null;
        }
        return result;
    }
    
    /**
     * This method merges two maps returning the union of both maps.
     * 
     * @param first  first map. can be null.
     * @param second second map. can be null.
     * @return the union of both maps. will not be null
     */
    public static <K, V> Map<K, V> nullSafeMerge(Map<K, V> first, Map<K, V> second)
    {
        return nullSafeMerge(first, second, false);
    }
    
    /**
     * This method merges two maps returning the union of both maps.
     * 
     * @param first  first map. can be null.
     * @param second second map. can be null.
     * @param emptyResultIsNull if the result is empty, should we return null?
     * @return the union of both maps, or null.
     */
    public static <K, V> Map<K, V> nullSafeMerge(Map<K, V> first, Map<K, V> second, boolean emptyResultIsNull)
    {
        Map<K, V> result = new HashMap<K, V>();
        
        if (first != null) result.putAll(first);
        if (second != null) result.putAll(second);
        
        if (result.isEmpty() && emptyResultIsNull)
        {
            result = null;
        }
        return result;
    }
    
    /**
     * This method joins two lists returning the a single list consisting of the first followed by the second.
     * 
     * @param first  first list. can be null.
     * @param second second list. can be null.
     * @return the concatenation of both lists. will not be null
     */
    public static <T> List<T> nullSafeAppend(List<T> first, List<T> second)
    {
        return nullSafeAppend(first, second, false);
    }
    
    /**
     * This method joins two lists returning the a single list consisting of the first followed by the second.
     * 
     * @param first  first list. can be null.
     * @param second second list. can be null.
     * @param emptyResultIsNull if the result is empty, should we return null?
     * @return the concatenation of both lists or null
     */
    public static <T> List<T> nullSafeAppend(List<T> first, List<T> second, boolean emptyResultIsNull)
    {
        List<T> result = new ArrayList<T>();
        
        if (first != null) result.addAll(first);
        if (second != null) result.addAll(second);
        
        if (result.isEmpty() && emptyResultIsNull)
        {
            result = null;
        }
        return result;
    }
    
    public static final Function<Object, String> TO_STRING_TRANSFORMER = new Function<Object, String>()
    {
        public String apply(Object value)
        {
            return value.toString();
        }
    };

    /**
     * Converts a {@link Collection} of values of type F to a {@link Serializable} {@link List} of values of type T.
     * Filters out all values converted to <code>null</code>.
     * @param <F> From type
     * @param <T> To type
     * @param values the values to convert.
     * @param transformer Used to convert values.
     * @return List
     */
    public static <F, T> List<T> transform(Collection<F> values, Function<? super F, ? extends T> transformer)
    {
        if(values == null || values.isEmpty())
        {
            return new ArrayList<T>();
        }
        List<T> results = new ArrayList<T>(values.size());
        for (F value : values)
        {
            T result = transformer.apply(value);
            if(result != null)
            {
                results.add(result);
            }
        }
        return results;
    }
    
    /**
     * Converts a {@link Map} having keys of type F to a new {@link Map} instance having keys of type T. The object references
     * in the value set are copied to the transformed map, thus reusing the same objects.
     * @param <F> From type
     * @param <T> To type
     * @param <V> The value type of the before and after maps.
     * @param map the map to convert.
     * @param transformer Used to convert keys.
     * @return a new Map instance with transformed keys and unchanged values. These values will be the same object references.
     */
    public static <F, T, V> Map<T, V> transformKeys(Map<F, V> map, Function<? super F, ? extends T> transformer)
    {
        if(map == null || map.isEmpty())
        {
            return new HashMap<T, V>();
        }
        Map<T, V> results = new HashMap<T, V>(map.size());
        for (Entry<F, V> entry : map.entrySet())
        {
            T transformedKey = transformer.apply(entry.getKey());
            results.put(transformedKey, entry.getValue());
        }
        return results;
    }
    
    /**
     * Converts a {@link Collection} of values of type F to a {@link Serializable} {@link List} of values of type T.
     * Filters out all values converted to <code>null</code>.
     * @param <F> From type
     * @param <T> To type
     * @param values the values to convert.
     * @param transformer Used to convert values.
     * @return List
     */
    public static <F, T> List<T> transform(Function<? super F, ? extends T> transformer, F... values)
    {
        if(values == null || values.length<1)
        {
            return new ArrayList<T>();
        }
        List<T> results = new ArrayList<T>(values.length);
        for (F value : values)
        {
            T result = transformer.apply(value);
            if(result != null)
            {
                results.add(result);
            }
        }
        return results;
    }
    
    public static List<String> toListOfStrings(Collection<?> values)
    {
        return transform(values, TO_STRING_TRANSFORMER);
    }
    
    /**
     * This utility method converts a vararg of Objects into a Set<T>.
     * 
     * @param objects the objects to be added to the set
     * @return a Set of objects (any equal objects will of course not be duplicated)
     * @throws ClassCastException if any of the supplied objects are not of type T.
     */
    public static <T> Set<T> asSet(T... objects)
    {
        Set<T> result = new HashSet<>();
        for (T obj : objects)
        {
            result.add(obj);
        }
        
        return result;
    }

    /**
     * This utility method converts a vararg of Objects into a Set<T>.
     *
     * @param clazz the Set type to return.
     * @param objects the objects to be added to the set
     * @return a Set of objects (any equal objects will of course not be duplicated)
     * @throws ClassCastException if any of the supplied objects are not of type T.
     */
    public static <T> Set<T> asSet(Class<T> clazz, Object... objects)
    {
        Set<T> result = new HashSet<T>();
        for (Object obj : objects)
        {
            @SuppressWarnings("unchecked")
            T cast = (T) obj;
            result.add(cast);
        }

        return result;
    }

    /**
     * Returns a filtered {@link List} of values. Only values for which <code>filter.apply(T) returns true</code> are included in the {@link List} or returned values. 
     * @param <T> The type of the {@link Collection}
     * @param values the {@link Collection} to be filtered.
     * @param filter the {@link Function} used to filter the {@link Collection}.
     * @return the filtered {@link List} of values.
     */
    public static <T> List<T> filter(Collection<T> values, final Function<? super T, Boolean > filter)
    {
        return transform(values, new Function<T, T>()
        {
            public T apply(T value)
            {
                if(filter.apply(value))
                {
                    return value;
                }
                return null;
            }
        });
    }
    
    /**
     * This method flattens the provided collection of collections of values into a single
     * {@code List} object containing each of the elements from the provided sub-collections.
     * <p/>
     * For example, {@code flatten( [1, 2], [3], [], [4, 5, 6] )} would produce a List like {@code [1, 2, 3, 4, 5, 6]}.
     * Here, "[]" represents any Java collection.
     * 
     * @param <T> the element type of the collections. Note that this must be the same for all collections.
     * @param values a collection of collections of elements to be flattened.
     * @return a List containing the flattened elements.
     */
    public static <T> List<T> flatten(Collection<? extends Collection<? extends T>> values)
    {
        List<T> results = new ArrayList<T>();
        for (Collection<? extends T> collection : values)
        {
            if (collection != null) { results.addAll(collection); }
        }
        return results;
    }
    
    /**
     * See {@link #flatten(Collection)}
     * @param collections a vararg of Collection objects to be flattened into a list.
     * @return A flat List containing the elements of the provided collections.
     * @since 5.0
     */
    @SafeVarargs
    public static <T> List<T> flatten(Collection<? extends T>... collections)
    {
        List<Collection<? extends T>> listOfCollections = Arrays.asList(collections);
        return CollectionUtils.flatten(listOfCollections);
    }
    
    public static <F, T> List<T> transformFlat(Collection<F> values, Function<? super F, ? extends Collection<? extends T>> transformer)
    {
        return flatten(transform(values, transformer));
    }
    
    /**
     * Finds the first value for which <code>acceptor</code> returns <code>true</code>.
     * @param <T> T
     * @param values Collection<T>
     * @param acceptor Function<? super T, Boolean>
     * @return returns accepted value or <code>null</code>.
     */
    public static <T> T findFirst(Collection<T> values, Function<? super T, Boolean> acceptor)
    {
        if (values != null )
        {
            for (T value : values)
            {
                if (acceptor.apply(value))
                {
                    return value;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns an immutable Serializable Set containing the values.
     * @param <T> T
     * @param values T...
     * @return Set<T>
     */
    public static <T> Set<T> unmodifiableSet(T... values)
    {
        return unmodifiableSet(Arrays.asList(values));
    }
    
    /**
     * Returns an immutable Serializable Set containing the values.
     * @param <T> T
     * @param values Collection<T>
     * @return Set<T>
     */
    public static <T> Set<T> unmodifiableSet(Collection<T> values)
    {
        TreeSet<T> set = new TreeSet<T>(values);
        return Collections.unmodifiableSet(set);
    }

    /**
     * @param values Collection<F>
     * @param transformer Function<F, T>
     * @return Map
     */
    public static <F, T> Map<F, T> transformToMap(Collection<F> values,
            Function<F, T> transformer)
    {
        if(isEmpty(values))
        {
            return Collections.emptyMap();
        }
        HashMap<F, T> results = new HashMap<F, T>(values.size());
        for (F value : values)
        {
            T result = transformer.apply(value);
            results.put(value, result);
        }
        return results;
    }
    
    /**
     * This method can be used to filter a Map. Any keys in the supplied map, for which the supplied {@link Function filter function}
     * returns <code>true</code>, will be included in the resultant Map, else they will not.
     * 
     * @param map the map whose entries are to be filtered.
     * @param filter the filter function which is applied to the key.
     * @return a filtered map.
     */
    public static <K, V> Map<K, V> filterKeys(Map<K, V> map, Function<? super K, ? extends Boolean> filter)
    {
        Map<K, V> results = new HashMap<K, V>();
        Set<Entry<K, V>> entries = map.entrySet();
        for (Entry<K, V> entry : entries)
        {
            K key = entry.getKey();
            if(filter.apply(key))
            {
                results.put(key, entry.getValue());
            }
        }
        return results;
    }
    
    public static <FK, FV, TK, TV> Map<TK, TV> transform(Map<FK, FV> map,
            Function<Entry<FK, FV>, Pair<TK, TV>> transformer )
    {
        Map<TK, TV> results = new HashMap<TK, TV>(map.size());
        for (Entry<FK, FV> entry : map.entrySet())
        {
            Pair<TK, TV> pair = transformer.apply(entry);
            if(pair!=null)
            {
                TK key = pair.getFirst();
                if (key != null)
                {
                    results.put(key, pair.getSecond());
                }
            }
        }
        return results;
    }
    
    public static <T> Filter<T> containsFilter(final Collection<T> values)
    {
        return new Filter<T>()
        {
            public Boolean apply(T value)
            {
                return values.contains(value);
            }
        };
    }
    
    /**
     * This method returns a new ArrayList which is the intersection of the two List parameters, based on {@link Object#equals(Object) equality}
     * of their elements.
     * The intersection list will contain elements in the order they have in list1 and any references in the resultant list will be
     * to elements within list1 also.
     * 
     * @return a new ArrayList whose values represent the intersection of the two Lists.
     */
    public static <T> List<T> intersect(List<? extends T> list1, List<? extends T> list2)
    {
        if (list1 == null || list1.isEmpty() || list2 == null || list2.isEmpty())
        {
            return Collections.emptyList();
        }
        
        List<T> result = new ArrayList<T>();
        result.addAll(list1);
        
        result.retainAll(list2);
        
        return result;
    }
    
    /**
     * This method returns a new HashMap which is the intersection of the two Map parameters, based on {@link Object#equals(Object) equality}
     * of their entries.
     * Any references in the resultant map will be to elements within map1.
     * 
     * @return a new HashMap whose values represent the intersection of the two Maps.
     */
    public static <K, V> Map<K, V> intersect(Map<K, V> map1, Map<K, V> map2)
    {
        if (map1 == null || map1.isEmpty() || map2 == null || map2.isEmpty())
        {
            return Collections.emptyMap();
        }
        
        // We now know neither map is null.
        Map<K, V> result = new HashMap<K, V>();
        for (Map.Entry<K, V> item : map1.entrySet())
        {
            V value = map2.get(item.getKey());
            if (value != null && value.equals(item.getValue()))
            {
                result.put(item.getKey(), item.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * This method returns a new HashSet which is the intersection of the two Set parameters, based on {@link Object#equals(Object) equality}
     * of their elements.
     * Any references in the resultant set will be to elements within set1.
     * 
     * @return a new HashSet whose values represent the intersection of the two Sets.
     */
    public static <T> Set<T> intersect(Set<? extends T> set1, Set<? extends T> set2)
    {
        if (set1 == null || set1.isEmpty() || set2 == null || set2.isEmpty())
        {
            return Collections.emptySet();
        }
        
        Set<T> result = new HashSet<T>();
        result.addAll(set1);
        
        result.retainAll(set2);
        
        return result;
    }

    /**
     * Creates a new sorted map, based on the values from the given map and Comparator.
     * 
     * @param map the map which needs to be sorted
     * @param valueComparator the Comparator
     * @return a new sorted map
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Comparator<Entry<K, V>> valueComparator)
    {
        if (map == null)
        {
            return Collections.emptyMap();
        }

        List<Entry<K, V>> entriesList = new LinkedList<>(map.entrySet());

        // Sort based on the map's values
        Collections.sort(entriesList, valueComparator);

        Map<K, V> orderedMap = new LinkedHashMap<>(entriesList.size());
        for (Entry<K, V> entry : entriesList)
        {
            orderedMap.put(entry.getKey(), entry.getValue());
        }
        return orderedMap;
    }
    
    /**
     * This method offers convenient conversion from value-based comparators to entry-based comparators
     * for use with {@link #sortMapByValue(Map, Comparator)} above.
     * <p/>
     * Call it like so: {@code CollectionUtils.<String, Integer>toEntryComparator(valueComparator);}
     * 
     * @param valueComparator a comparator which compares the value types from a Map.
     * @return a comparator which takes Map.Entry objects from that Map and compares their values.
     */
    public static <K, V> Comparator<Entry<K, V>> toEntryComparator(final Comparator<V> valueComparator)
    {
        return new Comparator<Entry<K, V>>()
        {
            @Override public int compare(Entry<K, V> e1, Entry<K, V> e2)
            {
                return valueComparator.compare(e1.getValue(), e2.getValue());
            }
        };
    }
    
    /**
     * This method returns a new List instance containing the same element objects as the provided
     * list, but with the specified element having been moved left by the specified offset.
     * <p/>
     * If the offset would mean that the element would move beyond the start or end of the list, it will
     * move only to the end.
     * 
     * @param offset the number of places over which to move the specified element.
     * @param element the element to be moved.
     * @param list the list to be reordered.
     * @return a new List instance containing the ordered elements.
     * @throws NoSuchElementException if the list does not contain an element equal to the one specified.
     */
    public static <T> List<T> moveLeft(int offset, T element, List<T> list)
    {
        return moveRight(-offset, element, list);
    }
    
    /**
     * This method does the same as {@link #moveLeft(int, Object, List)} but it moves the specified element
     * to the right instead of the left.
     */
    public static <T> List<T> moveRight(int offset, T element, List<T> list)
    {
        final int elementIndex = list.indexOf(element);
        
        if (elementIndex == -1) { throw new NoSuchElementException("Element not found in provided list."); }
        
        if (offset == 0)
        {
            return list;
        }
        else
        {
            int newElementIndex = elementIndex + offset;
            
            // Ensure that the element will not move off the end of the list.
            if      (newElementIndex >= list.size()) { newElementIndex = list.size() - 1; }
            else if (newElementIndex < 0)            { newElementIndex = 0; }
            
            List<T> result = new ArrayList<>(list);
            result.remove(element);
            
            result.add(newElementIndex, element);
            
            return result;
        }
    }
}
