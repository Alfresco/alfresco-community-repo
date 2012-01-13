/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.content.transform.TransformerDebug;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A pair of transformation options that specify
 *   A) a max value over which the source is not read (throws an Exception) or
 *   B) a limit over which no more of the source is read (returns EOF) 
 *    
 * Each pair represents a values such as an elapse time, KBytes read or number of pages read.
 * It is only meaningful for a either the max or limit value to be set.
 * 
 * There is one pair of values for each transformer and another pair passed in via the
 * options parameter for each individual transformation. The later is for specific types of
 * transformation, such as thumbnail generation. When this occurs values are combined, by
 * using the lowest of the four values.
 * 
 * @author Alan Davis
 */
public class TransformationOptionPair
{
    /**
     * Action to take place for a given pair of values. 
     */
    public enum Action
    {
        THROW_EXCEPTION
        {
            public void throwIOExceptionIfRequired(String message, TransformerDebug transformerDebug) throws IOException
            {
                throw transformerDebug.setCause(new IOException(message));
            }
        },
        RETURN_EOF
        {
            public void throwIOExceptionIfRequired(String message, TransformerDebug transformerDebug) throws IOException
            {
                if (transformerDebug.isEnabled())
                {
                    transformerDebug.debug(message + " Returning EOF");
                }
            }
        };

        public abstract void throwIOExceptionIfRequired(String message, TransformerDebug transformerDebug) throws IOException;
    };

    private long max = -1;
    private long limit = -1;
    
    public long getMax()
    {
        return max;
    }
    
    public void setMax(long max, String exceptionMessage)
    {
        if (max >= 0 && limit >= 0)
        {
            throw new IllegalArgumentException(exceptionMessage);
        }
        this.max = max;
    }
    
    public long getLimit()
    {
        return limit;
    }
    
    public void setLimit(long limit, String exceptionMessage)
    {
        if (max >= 0 && limit >= 0)
        {
            throw new IllegalArgumentException(exceptionMessage);
        }
        this.limit = limit;
    }
    
    public long getValue()
    {
        return minSet(getMax(), getLimit());
    }

    public Action getAction()
    {
        return
            (getMax() >= 0) ? Action.THROW_EXCEPTION :
            (getLimit() >= 0) ? Action.RETURN_EOF
                        : null;
    }
    
    /**
     * Returns the lower of the two value supplied, ignoring values less than 
     * 0 unless both are less than zero.
     */
    private long minSet(long value1, long value2)
    {
        if (value1 < 0)
        {
            return value2;
        }
        else if (value2 < 0)
        {
            return value1;
        }
        return Math.min(value1, value2);
    }

    public Map<String, Object> toMap(Map<String, Object> optionsMap, String optMaxKey, String optLimitKey)
    {
        optionsMap.put(optMaxKey, getMax());
        optionsMap.put(optLimitKey, getLimit());
        return optionsMap;
    }

    public void set(Map<String, Object> optionsMap, String optMaxKey, String optLimitKey,
            String exceptionMessage)
    {
        long max = nvl((Long)optionsMap.get(optMaxKey));
        long limit = nvl((Long)optionsMap.get(optLimitKey));
        if (max >= 0 && limit >= 0)
        {
            throw new IllegalArgumentException(exceptionMessage);
        }
        if (max >= 0 || limit >= 0)
        {
            this.limit = limit;
            this.max = max;
        }
    }

    private long nvl(Long l)
    {
        return l == null ? -1 : l;
    }

    /**
     * Returns a TransformationOptionPair that has getter methods that combine the
     * the values from the getter methods of this and the supplied TransformationOptionPair.
     */
    public TransformationOptionPair combine(final TransformationOptionPair that)
    {
        return new TransformationOptionPair()
        {
            /**
             * Combines max values of this TransformationOptionPair and the supplied
             * one to return the max to be used in a transformation. The limit
             * value is discarded (-1 is returned) if the combined limit value is lower.
             */
            @Override
            public long getMax()
            {
                long max = minSet(TransformationOptionPair.this.getMax(), that.getMax());
                long limit = minSet(TransformationOptionPair.this.getLimit(), that.getLimit());
                
                return (max >= 0 && (limit < 0 || limit >= max))
                    ? max
                    : -1;
            }

            @Override
            public void setMax(long max, String exceptionMessage)
            {
                throw new UnsupportedOperationException();
            }
            
            /**
             * Combines limit values of this TransformationOptionPair and the supplied
             * one to return the limit to be used in a transformation. The limit
             * value is discarded (-1 is returned) if the combined max value is lower.
             */
            @Override
            public long getLimit()
            {
                long max = minSet(TransformationOptionPair.this.getMax(), that.getMax());
                long limit = minSet(TransformationOptionPair.this.getLimit(), that.getLimit());
                
                return (limit >= 0 && (max < 0 || max >= limit))
                    ? limit
                    : -1;
            }

            @Override
            public void setLimit(long limit, String exceptionMessage)
            {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public void set(Map<String, Object> optionsMap, String optMaxKey, String optLimitKey,
                    String exceptionMessage)
            {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public int hashCode()
    {
        return (int) ((max > 0) ? max : limit);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof TransformationOptionPair)
        {
            TransformationOptionPair that = (TransformationOptionPair) obj;
            return max == that.max && limit == that.limit;
        }
        else
        {
            return false;
        }
    }
}
