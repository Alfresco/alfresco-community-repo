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
package org.alfresco.repo.content;

/**
 * Implementations of this interface must provide a byte size limit for pieces of Alfresco content.
 * Simple implementations of this interface include:
 * <ul>
 *   <li>{@link NoLimitProvider} which provides a value indicating no limit.</li>
 *   <li>{@link SimpleFixedLimitProvider} which provides a fixed numerical limit value.</li>
 * </ul>
 * It is possible that smarter implementations may be added at a future date.
 * 
 * @author Neil Mc Erlean
 * @since Thor
 */
public interface ContentLimitProvider
{
    /**
     * A {@link ContentLimitProvider#getSizeLimit() limit} having this value is deemed not to be a limit.
     */
    public static final long NO_LIMIT = -1L;
    
    /**
     * This method returns a size limit in bytes.
     */
    long getSizeLimit();
    
    /**
     * A {@link ContentLimitProvider} which returns a fixed value.
     */
    public static class SimpleFixedLimitProvider implements ContentLimitProvider
    {
        private long limit;
        
        public SimpleFixedLimitProvider()
        {
            // Default constructor for use as bean.
        }
        public SimpleFixedLimitProvider(long limit)
        {
            this.limit = limit;
        }
        
        /**
         * This method sets a value for the limit. If the string does not {@link Long#parseLong(String) parse} to a
         * java long, the {@link ContentLimitProvider#NO_LIMIT default value} will be applied instead.
         * 
         * @param limit a String representing a valid Java long.
         */
        public void setSizeLimitString(String limit)
        {
            // A string parameter is used here in order to not to require end users to provide a value for the limit in a property
            // file. This results in the empty string being injected to this method.
            long longLimit = NO_LIMIT;
            try
            {
                longLimit = Long.parseLong(limit);
            } catch (NumberFormatException ignored)
            {
                // Intentionally empty
            }
            this.limit = longLimit;
        }
        
        @Override public long getSizeLimit()
        {
            return limit;
        }
    }
    
    /**
     * A {@link ContentLimitProvider} which returns a value indicating there is no limit.
     */
    public static class NoLimitProvider implements ContentLimitProvider
    {
        @Override public long getSizeLimit()
        {
            return NO_LIMIT;
        }
    }
}
