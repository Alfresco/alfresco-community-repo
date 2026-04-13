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
package org.alfresco.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Utility class providing helper methods for various types of <code>equals</code> functionality
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class EqualsHelper
{
    /**
     * Performs an equality check <code>left.equals(right)</code> after checking for null values
     * 
     * @param left the Object appearing in the left side of an <code>equals</code> statement
     * @param right the Object appearing in the right side of an <code>equals</code> statement
     * @return Return true or false even if one or both of the objects are null
     */
    public static boolean nullSafeEquals(Object left, Object right)
    {
        return (left == right) || (left != null && right != null && left.equals(right));
    }

    /**
     * Performs an case-sensitive or case-insensitive equality check after checking for null values
     * @param ignoreCase           <tt>true</tt> to ignore case
     */
    public static boolean nullSafeEquals(String left, String right, boolean ignoreCase)
    {
        if (ignoreCase)
        {
            return (left == right) || (left != null && right != null && left.equalsIgnoreCase(right));
        }
        else
        {
            return (left == right) || (left != null && right != null && left.equals(right));
        }
    }
    
    private static final int BUFFER_SIZE = 1024;
    /**
     * Performs a byte-level comparison between two streams.
     * 
     * @param left         the left stream.  This is closed at the end of the operation.
     * @param right        an right stream.  This is closed at the end of the operation.
     * @return             Returns <tt>true</tt> if the streams are identical to the last byte
     */
    public static boolean binaryStreamEquals(InputStream left, InputStream right) throws IOException
    {
        try
        {
            if (left == right)
            {
                // The same stream!  This is pretty pointless, but they are equal, nevertheless.
                return true;
            }
            
            byte[] leftBuffer = new byte[BUFFER_SIZE];
            byte[] rightBuffer = new byte[BUFFER_SIZE];
            while (true)
            {
                int leftReadCount = left.read(leftBuffer);
                int rightReadCount = right.read(rightBuffer);
                if (leftReadCount != rightReadCount)
                {
                    // One stream ended before the other
                    return false;
                }
                else if (leftReadCount == -1)
                {
                    // Both streams ended without any differences found
                    return true;
                }
                for (int i = 0; i < leftReadCount; i++)
                {
                    if (leftBuffer[i] != rightBuffer[i])
                    {
                        // We found a byte difference
                        return false;
                    }
                }
            }
            // The only exits with 'return' statements, so there is no need for any code here
        }
        finally
        {
            try { left.close(); } catch (Throwable e) {}
            try { right.close(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Compare two maps and generate a difference report between the actual and expected values.
     * This method is particularly useful during unit tests as the result (if not <tt>null</tt>)
     * can be appended to a failure message.
     * 
     * @param actual                the map in hand
     * @param expected              the map expected
     * @return                      Returns a difference report or <tt>null</tt> if there were no
     *                              differences.  The message starts with a new line and is neatly
     *                              formatted.
     */
    public static String getMapDifferenceReport(Map<?, ?> actual, Map<?, ?> expected)
    {
        Map<?, ?> copyResult = new HashMap<Object, Object>(actual);
        
        boolean failure = false;

        StringBuilder sb = new StringBuilder(1024);
        sb.append("\nValues that don't match the expected values: ");
        for (Map.Entry<?, ?> entry : expected.entrySet())
        {
            Object key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object resultValue = actual.get(key);
            if (!EqualsHelper.nullSafeEquals(resultValue, expectedValue))
            {
                sb.append("\n")
                  .append("   Key: ").append(key).append("\n")
                  .append("      Result:   ").append(resultValue).append("\n")
                  .append("      Expected: ").append(expectedValue);
                failure = true;
            }
            copyResult.remove(key);
        }
        sb.append("\nValues that are present but should not be: ");
        for (Map.Entry<?, ?> entry : copyResult.entrySet())
        {
            Object key = entry.getKey();
            Object resultValue = entry.getValue();
            sb.append("\n")
              .append("   Key: ").append(key).append("\n")
              .append("      Result:   ").append(resultValue);
          failure = true;
        }
        if (failure)
        {
            return sb.toString();
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Enumeration for results returned by {@link EqualsHelper#getMapComparison(Map, Map) map comparisons}.
     * 
     * @author Derek Hulley
     * @since 3.3
     */
    public static enum MapValueComparison
    {
        /** The key was only present in the left map */
        LEFT_ONLY,
        /** The key was only present in the right map */
        RIGHT_ONLY,
        /** The key was present in both maps and the values were equal */ 
        EQUAL,
        /** The key was present in both maps but not equal */
        NOT_EQUAL
    }
    
    /**
     * Compare two maps.
     * <p/>
     * The return codes that accompany the keys are:
     * <ul>
     *    <li>{@link MapValueComparison#LEFT_ONLY}</li>
     *    <li>{@link MapValueComparison#RIGHT_ONLY}</li>
     *    <li>{@link MapValueComparison#EQUAL}</li>
     *    <li>{@link MapValueComparison#NOT_EQUAL}</li>
     * </ul>
     *  
     * @param <K>           the map key type
     * @param <V>           the map value type
     * @param left          the left side of the comparison 
     * @param right         the right side of the comparison
     * @return              Returns a map whose keys are a union of the two maps' keys, along with
     *                      the value comparison result
     */
    public static <K, V> Map<K, MapValueComparison> getMapComparison(Map<K, V> left, Map<K, V> right)
    {
        Set<K> keys = new HashSet<K>(left.size() + right.size());
        keys.addAll(left.keySet());
        keys.addAll(right.keySet());
        
        Map<K, MapValueComparison> diff = new HashMap<K, MapValueComparison>(left.size() + right.size());

        // Iterate over the keys and do the comparisons
        for (K key : keys)
        {
            boolean leftHasKey = left.containsKey(key);
            boolean rightHasKey = right.containsKey(key);
            V leftValue = left.get(key);
            V rightValue = right.get(key);
            if (leftHasKey)
            {
                if (!rightHasKey)
                {
                    diff.put(key, MapValueComparison.LEFT_ONLY);
                }
                else if (EqualsHelper.nullSafeEquals(leftValue, rightValue))
                {
                    diff.put(key, MapValueComparison.EQUAL);
                }
                else
                {
                    diff.put(key, MapValueComparison.NOT_EQUAL);
                }
            }
            else if (rightHasKey)
            {
                if (!leftHasKey)
                {
                    diff.put(key, MapValueComparison.RIGHT_ONLY);
                }
                else if (EqualsHelper.nullSafeEquals(leftValue, rightValue))
                {
                    diff.put(key, MapValueComparison.EQUAL);
                }
                else
                {
                    diff.put(key, MapValueComparison.NOT_EQUAL);
                }
            }
            else
            {
                // How is it here?
            }
        }
        
        return diff;
    }
}
