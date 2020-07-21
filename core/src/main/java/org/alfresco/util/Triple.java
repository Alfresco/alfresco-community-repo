/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.util;

/**
 * Utility class for containing three things that aren't like each other.
 * 
 * @since 4.0
 */
public final class Triple<T, U, V>
{
    /**
     * The first member of the triple.
     */
    private final T first;
    
    /**
     * The second member of the triple.
     */
    private final U second;
    
    /**
     * The third member of the triple.
     */
    private final V third;
    
    /**
     * Make a new one.
     * 
     * @param first  The first member.
     * @param second The second member.
     * @param third  The third member.
     */
    public Triple(final T first, final U second, final V third)
    {
        this.first  = first;
        this.second = second;
        this.third  = third;
    }
    
    /**
     * Get the first member of the tuple.
     * @return The first member.
     */
    public T getFirst()
    {
        return first;
    }
    
    /**
     * Get the second member of the tuple.
     * @return The second member.
     */
    public U getSecond()
    {
        return second;
    }
    
    /**
     * Get the third member of the tuple.
     * @return The third member.
     */
    public V getThird()
    {
        return third;
    }
    
    /**
     * Override of equals.
     * @param other The thing to compare to.
     * @return equality.
     */
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        
        if (!(other instanceof Triple))
        {
            return false;
        }
        
        Triple<?, ?, ?> o = (Triple<?, ?, ?>)other;
        return (first.equals(o.getFirst()) &&
                second.equals(o.getSecond()) &&
                third.equals(o.getThird()));
    }
    
    /**
     * Override of hashCode.
     */
    public int hashCode()
    {
        return ((first  == null ? 0 : first.hashCode()) +
                (second == null ? 0 : second.hashCode()) +
                (third  == null ? 0 : third.hashCode()));
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "(" + first + ", " + second + ", " + third + ")";
    }
}
