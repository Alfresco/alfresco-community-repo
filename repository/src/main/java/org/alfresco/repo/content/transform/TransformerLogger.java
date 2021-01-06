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
package org.alfresco.repo.content.transform;

import java.text.SimpleDateFormat;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import org.alfresco.api.AlfrescoPublicApi;  
import org.alfresco.util.LogAdapter;
import org.apache.commons.logging.Log;

/**
 * Implementation of {@link Log} that logs messages to an internal structure which
 * only keeps a predefined number of entries.
 * 
 * Only supports debug level logging.
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
abstract class TransformerLogger<T> extends LogAdapter implements LogEntries
{
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

    private TransformerDebug transformerDebug;
    private Properties properties;
    
    private int maxEntries = -1;
    private Deque<T> entries = new LinkedList<T>();
    
    TransformerLogger()
    {
        super(null);
    }

    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Returns an int from a property.
     * @param propertyName
     */
    private int getProperty(String propertyName, int min, int max)
    {
        int i = 0;
        String value = properties.getProperty(propertyName);
        if (value != null)
        {
            try
            {
                i = Integer.parseInt(value);
                if (i < 0)
                {
                    i = 0;
                }
            }
            catch (NumberFormatException e)
            {
                i = 0;
            }
        }
        i = Math.min(Math.max(i, min), max);
        return i;
    }
    
    private int getMaxEntries()
    {
        if (maxEntries < 0)
        {
            maxEntries = getProperty(getPropertyName(), 0, getUpperMaxEntries());
        }
        return maxEntries;
    }

    /**
     * Enabled if number of transformer debug entries is greater than 0
     * AND there is not a test function generating output.
     */
    @Override
    public boolean isDebugEnabled()
    {
        return getMaxEntries() > 0 && transformerDebug.getStringBuilder() == null;
    }

    /**
     * @param message to be appended to the log
     * @param throwable ignored
     */
    @Override
    public void debug(Object message, Throwable throwable)
    {
        if (isDebugEnabled())
        {
            synchronized(entries)
            {
                addOrModify(entries, message);
                for (int size = entries.size(); size > maxEntries; size--)
                {
                    remove(entries);
                }
            }
        }
    }
    
    /**
     * Overridden in sub classes to add a new entry or modify and existing one
     * @param message
     */
    protected abstract void addOrModify(Deque<T> entries, Object message);
    
    /**
     * Removes an entry. By default removes the first (oldest) entry by may be overridden.
     */
    protected void remove(Deque<T> entries)
    {
        entries.removeFirst();
    }
    
    /**
     * Returns the log entries.
     * @param n the maximum number of entries to return. All if n is smaller or equal to zero.
     */
    @Override
    public String[] getEntries(int n)
    {
        if (getMaxEntries() > 0)
        {
            n = n <= 0 ? Integer.MAX_VALUE : n;
            synchronized(entries)
            {
                n = Math.min(Math.max(Math.min(maxEntries, n), 0), entries.size());
                String[] array = new String[n];
                Iterator<T> iterator = entries.descendingIterator();
                for (int i = 0; i < n; i++)
                {
                    array[i] = iterator.next().toString();
                }
                return array;
            }
        }
        else
        {
            return new String[] { "No entries are available. "+
                    getPropertyName()+" must be set to a number between 1 and "+
                    getUpperMaxEntries()};
        }
    }
    
    private static void appendProperty(StringBuilder sb, String propertyName, String value, String defaultValue)
    {
        boolean isDefaultValue = value.equals(defaultValue);
        if (isDefaultValue)
        {
            sb.append("# ");
        }
        sb.append(propertyName);
        sb.append('=');
        sb.append(value);
        if (!isDefaultValue)
        {
            sb.append("  # default=");
            sb.append(defaultValue);
        }
    }
    /**
     * Overridden to specify the maximum value the maxEntries property may set.
     * Generally quite a small number as values are stored in memory.
     */
    protected abstract int getUpperMaxEntries();

    /**
     * Overridden to specify the property name that specifies the maximum number of entries.
     */
    protected abstract String getPropertyName();
}
