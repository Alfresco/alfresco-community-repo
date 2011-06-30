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
package org.alfresco.repo.security.permissions;

import java.util.Collection;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Interface for collection-based results that describe permission filtering
 * behaviour around cut-off limits.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface PermissionCheckCollection<T>
{
    /**
     * Get the desired number of results.  Permission checks can stop once the number of
     * return objects reaches this number.
     * 
     * @return                          the number of results desired
     */
    int getTargetResultCount();

    /**
     * Get the maximum time for permission checks to execute before cutting the results off.
     * <br/>Zero: Ignore this value.
     * 
     * @return                          the time allowed for permission checks before cutoff
     */
    long getCutOffAfterTimeMs();

    /**
     * Get the maximum number of permission checks to perform before cutting the results off
     * 
     * @return                          the maximum number of permission checks before cutoff
     */
    int getCutOffAfterCount();

    /**
     * Helper 'introduction' to allow simple addition of the {@link PermissionCheckCollection} interface to
     * existing collections.
     *
     * @param <T>       the type of the <code>Collection</code> in use
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    @SuppressWarnings("serial")
    public static class PermissionCheckCollectionMixin<T> extends DelegatingIntroductionInterceptor implements PermissionCheckCollection<T>
    {
        private final int targetResultCount;
        private final long cutOffAfterTimeMs;
        private final int cutOffAfterCount;
        
        private PermissionCheckCollectionMixin(int targetResultCount, long cutOffAfterTimeMs, int cutOffAfterCount)
        {
            super();
            this.targetResultCount = targetResultCount;
            this.cutOffAfterTimeMs = cutOffAfterTimeMs;
            this.cutOffAfterCount = cutOffAfterCount;
            if (cutOffAfterTimeMs <= 0)
            {
                cutOffAfterTimeMs = 0;
            }
            if (cutOffAfterCount <= 0)
            {
                cutOffAfterCount = 0;
            }
        }

        @Override
        public int getTargetResultCount()
        {
            return targetResultCount;
        }

        @Override
        public long getCutOffAfterTimeMs()
        {
            return cutOffAfterTimeMs;
        }

        @Override
        public int getCutOffAfterCount()
        {
            return cutOffAfterCount;
        }

        /**
         * Helper method to create a {@link PermissionCheckCollection} from an existing <code>Collection</code>
         * 
         * @param <TT>              the type of the <code>Collection</code>
         * @param collection        the <code>Collection</code> to proxy
         * @param targetResultCount the desired number of results or default to the collection size
         * @param cutOffAfterTimeMs the number of milliseconds to wait before cut-off or zero to use the system default
         *                          time-based cut-off.
         * @param cutOffAfterCount  the number of permission checks to process before cut-off or zero to use the system default
         *                          count-based cut-off.
         * @return                  a <code>Collection</code> of the same type but including the
         *                          {@link PermissionCheckCollection} interface
         */
        @SuppressWarnings("unchecked")
        public static final <TT> Collection<TT> create(
                Collection<TT> collection,
                int targetResultCount, long cutOffAfterTimeMs, int cutOffAfterCount)
        {
            if (targetResultCount <= 0)
            {
                targetResultCount = collection.size();
            }
            // Create the mixin
            DelegatingIntroductionInterceptor mixin = new PermissionCheckCollectionMixin<Integer>(
                    targetResultCount,
                    cutOffAfterTimeMs,
                    cutOffAfterCount);
            // Create the advisor
            IntroductionAdvisor advisor = new DefaultIntroductionAdvisor(mixin, PermissionCheckCollection.class);
            // Proxy
            ProxyFactory pf = new ProxyFactory(collection);
            pf.addAdvisor(advisor);
            Object proxiedObject = pf.getProxy();
            
            // Done
            return (Collection<TT>) proxiedObject;
        }
    }
}
