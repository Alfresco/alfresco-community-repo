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
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.ObjectInputStream.GetField;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Utility class for containing two things that aren't like each other
 */
@AlfrescoPublicApi
public final class Pair<F, S> implements Serializable
{
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Pair NULL_PAIR = new Pair(null, null);
    
    @SuppressWarnings("unchecked")
    public static final <X, Y> Pair<X, Y> nullPair()
    {
        return NULL_PAIR;
    }
    
    private static final long serialVersionUID = -7406248421185630612L;

    /**
     * The first member of the pair.
     */
    private F first;
    
    /**
     * The second member of the pair.
     */
    private S second;
    
    /**
     * Make a new one.
     * 
     * @param first The first member.
     * @param second The second member.
     */
    public Pair(F first, S second)
    {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Get the first member of the tuple.
     * @return The first member.
     */
    public final F getFirst()
    {
        return first;
    }
    
    /**
     * Get the second member of the tuple.
     * @return The second member.
     */
    public final S getSecond()
    {
        return second;
    }
    
    public final void setFirst(F first)
    {
        this.first = first;
    }
    
    public final void setSecond(S second)
    {
        this.second = second;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || !(other instanceof Pair<?, ?>))
        {
            return false;
        }
        Pair<?, ?> o = (Pair<?, ?>)other;
        return EqualsHelper.nullSafeEquals(this.first, o.first) &&
               EqualsHelper.nullSafeEquals(this.second, o.second);
    }
    
    @Override
    public int hashCode()
    {
        return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString()
    {
        return "(" + first + ", " + second + ")";
    }

    /**
     * Ensure that previously-serialized instances don't fail due to the member name change.
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException
    {
        GetField fields = is.readFields();
        if (fields.defaulted("first"))
        {
            // This is a pre-V3.3
            this.first = (F) fields.get("fFirst", null);
            this.second = (S) fields.get("fSecond", null);
        }
        else
        {
            this.first = (F) fields.get("first", null);
            this.second = (S) fields.get("second", null);
        }
    }
}
