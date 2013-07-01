/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.transform.TransformerDebug;

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
public class TransformationOptionPair implements Serializable
{
    private static final long serialVersionUID = 1L;

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
        setMax(max);
    }

    private void setMax(long max)
    {
        this.max = max;
        this.limit = -1;
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
        setLimit(limit);
    }
    
    private void setLimit(long limit)
    {
        this.max = -1;
        this.limit = limit;
    }
    
    public long getValue()
    {
        return minSet(getMax(), getLimit());
    }

    /**
     * Indicates if the limit allows a transformation to take place at all.
     * If 0, it would not be possible.
     * @return true if a transformation is possible.
     */
    public boolean supported()
    {
        return getValue() != 0;
    }
    
    public Action getAction()
    {
        return
            (getMax() >= 0) ? Action.THROW_EXCEPTION :
            (getLimit() >= 0) ? Action.RETURN_EOF
                        : null;
    }

    /**
     * Defaults values that are set in this pair into the
     * supplied pair.
     * @param pair to be set
     */
    public void defaultTo(TransformationOptionPair pair)
    {
        long max = getMax();
        if (max >= 0)
        {
            pair.setMax(max);
        }
        else
        {
            long limit = getLimit();
            if (limit >= 0)
            {
                pair.setLimit(limit);
            }
        }
    }

    public String toString(String max, String limit)
    {
        if (getMax() >= 0)
        {
            return max+'='+getValue();
        }
        if (getLimit() >= 0)
        {
            return limit+'='+getValue();
        }
        return null;
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
    
    /**
     * Returns the higher (common denominator) of the two value supplied.
     * If either value is less than 0, -1 is returned.
     */
    private long maxSet(long value1, long value2)
    {
        if (value1 < 0 || value2 < 0)
        {
            return -1;
        }
        return Math.max(value1, value2);
    }

    public Map<String, Object> toMap(Map<String, Object> optionsMap, String optMaxKey, String optLimitKey)
    {
        optionsMap.put(optMaxKey, getMax());
        optionsMap.put(optLimitKey, getLimit());
        return optionsMap;
    }

    public void append(StringBuilder sb, String optMaxKey, String optLimitKey)
    {
        long max = getMax();
        if (max >= 0)
        {
            if (sb.length() > 1)
            {
                sb.append(", ");
            }
            
            sb.append(optMaxKey);
            sb.append('=');
            sb.append(max);
        }
        else
        {
            long limit = getLimit();
            if (limit >= 0)
            {
                if (sb.length() > 1)
                {
                    sb.append(", ");
                }
                
                sb.append(optLimitKey);
                sb.append('=');
                sb.append(limit);
            }
        }
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
    public TransformationOptionPair combine(TransformationOptionPair that)
    {
        return combine(that, true);
    }
    
    /**
     * Returns a TransformationOptionPair that has getter methods that combine the
     * the values from the getter methods of this and the supplied TransformationOptionPair
     * so that they return the lowest common denominator of the two limits .
     */
    public TransformationOptionPair combineUpper(final TransformationOptionPair that)
    {
        return combine(that, false);
    }
    
    private TransformationOptionPair combine(final TransformationOptionPair that, final boolean lower)
    {
        return new TransformationOptionPair()
        {
            /**
             * Combines max values of this TransformationOptionPair and the supplied
             * one to return the max to be used in a transformation. When 'lower' the max
             * value is discarded (-1 is returned) if the combined limit value is lower.
             * When 'not lower' (lowest common denominator) the max is only returned if the
             * limit value is -1.
             */
            @Override
            public long getMax()
            {
                long max = getMaxValue();
                long limit = getLimitValue();
                
                return lower
                    ? (max >= 0 && (limit < 0 || limit >= max))
                      ? max
                      : -1
                    : (limit < 0)
                      ? max
                      : -1;
            }
            
            /**
             * Combines limit values of this TransformationOptionPair and the supplied
             * one to return the limit to be used in a transformation. When 'lower' the limit
             * value is discarded (-1 is returned) if the combined max value is lower.
             * When 'not lower' (lowest common denominator) the limit is only returned if the
             * max value is -1.
             */
            @Override
            public long getLimit()
            {
                long max = getMaxValue();
                long limit = getLimitValue();
                
                return lower
                        ? (limit >= 0 && (max < 0 || max > limit))
                          ? limit
                          : -1
                        : (max < 0)
                          ? limit
                          : -1;
            }

            private long getLimitValue()
            {
                return lower
                        ? minSet(TransformationOptionPair.this.getLimit(), that.getLimit())
                        : maxSet(TransformationOptionPair.this.getLimit(), that.getLimit());
            }

            private long getMaxValue()
            {
                return lower
                        ? minSet(TransformationOptionPair.this.getMax(), that.getMax())
                        : maxSet(TransformationOptionPair.this.getMax(), that.getMax());
            }

            @Override
            public void setMax(long max, String exceptionMessage)
            {
                throw new UnsupportedOperationException();
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
