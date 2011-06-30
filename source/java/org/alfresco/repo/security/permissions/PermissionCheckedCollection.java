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
 * Interface for collection-based results that carry extra information
 * about the state of permission cut-offs.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface PermissionCheckedCollection<T>
{
    /**
     * Check if the results have been truncated by permission check limits.
     * This can only be called when {@link #isFiltered()} is <tt>true</tt>.
     * 
     * @return              <tt>true</tt> - if the results (usually a collection) have been
     *                      cut off by permission check limits
     */
    boolean isCutOff();
    
    /**
     * Get the number of objects in the original (unfiltered) collection that did
     * <b>not<b/> have any permission checks.
     * 
     * @return              number of entries from the original collection that were not checked
     */
    int sizeUnchecked();
    
    /**
     * Get the number of objects in the original (unfiltered) collection.
     * 
     * @return              number of entries in the original, pre-checked collection
     */
    int sizeOriginal();

    /**
     * Helper 'introduction' to allow simple addition of the {@link PermissionCheckedCollection} interface to
     * existing collections.
     *
     * @param <T>       the type of the <code>Collection</code> in use
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    @SuppressWarnings("serial")
    public static class PermissionCheckedCollectionMixin<T> extends DelegatingIntroductionInterceptor implements PermissionCheckedCollection<T>
    {
        private final boolean isCutOff;
        private final int sizeUnchecked;
        private final int sizeOriginal;
        private PermissionCheckedCollectionMixin(boolean isCutOff, int sizeUnchecked, int sizeOriginal)
        {
            super();
            this.isCutOff = isCutOff;
            this.sizeUnchecked = sizeUnchecked;
            this.sizeOriginal = sizeOriginal;
        }
        @Override
        public boolean isCutOff()
        {
            return isCutOff;
        }
        @Override
        public int sizeUnchecked()
        {
            return sizeUnchecked;
        }
        @Override
        public int sizeOriginal()
        {
            return sizeOriginal;
        }
        /**
         * Helper method to create a {@link PermissionCheckedCollection} from an existing <code>Collection</code>
         * 
         * @param <TT>              the type of the <code>Collection</code>
         * @param collection        the <code>Collection</code> to proxy
         * @param isCutOff          <tt>true</tt> if permission checking was cut off before completion
         * @param sizeUnchecked     number of entries from the original collection that were not checked
         * @param sizeOriginal      number of entries in the original, pre-checked collection
         * @return                  a <code>Collection</code> of the same type but including the
         *                          {@link PermissionCheckedCollection} interface
         */
        @SuppressWarnings("unchecked")
        public static final <TT> Collection<TT> create(
                Collection<TT> collection,
                boolean isCutOff, int sizeUnchecked, int sizeOriginal)
        {
            // Create the mixin
            DelegatingIntroductionInterceptor mixin = new PermissionCheckedCollectionMixin<Integer>(
                    isCutOff,
                    sizeUnchecked,
                    sizeOriginal
                    );
            // Create the advisor
            IntroductionAdvisor advisor = new DefaultIntroductionAdvisor(mixin, PermissionCheckedCollection.class);
            // Proxy
            ProxyFactory pf = new ProxyFactory(collection);
            pf.addAdvisor(advisor);
            Object proxiedObject = pf.getProxy();
            
            // Done
            return (Collection<TT>) proxiedObject;
        }
    }
}
