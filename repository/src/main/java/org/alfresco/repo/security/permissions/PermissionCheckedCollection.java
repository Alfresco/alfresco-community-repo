/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions;

import java.util.Collection;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Interface for collection-based results that carry extra information about the state of permission cut-offs.
 *
 * @author Derek Hulley
 * @since 4.0
 */
public interface PermissionCheckedCollection<T>
{
    /**
     * Check if the results have been truncated by permission check limits.
     *
     * @return <tt>true</tt> - if the results (usually a collection) have been cut off by permission check limits
     */
    boolean isCutOff();

    /**
     * Get the number of objects in the original (unfiltered) collection that did <b>not</b> have any permission checks.
     *
     * @return number of entries from the original collection that were not checked
     */
    int sizeUnchecked();

    /**
     * Get the number of objects in the original (unfiltered) collection.
     *
     * @return number of entries in the original, pre-checked collection
     */
    int sizeOriginal();

    /**
     * Helper 'introduction' to allow simple addition of the {@link PermissionCheckedCollection} interface to existing collections.
     *
     * @param <T>
     *            the type of the <code>Collection</code> in use
     *
     * @author Derek Hulley
     * @since 4.0
     */
    @SuppressWarnings("serial")
    class PermissionCheckedCollectionMixin<T> extends DelegatingIntroductionInterceptor implements PermissionCheckedCollection<T>
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
         * Helper method to create a {@link PermissionCheckedCollection} from an existing <code>Collection</code> by applying the same values as present on a potentially permission-checked source. If the existing checked source is <b>NOT</b> permission-checked, then the collection will not be decorated.
         *
         * @param <TT>
         *            the type of the <code>Collection</code>
         * @param collection
         *            the <code>Collection</code> to proxy
         * @param checkedSource
         *            a collection that might implement {@link PermissionCheckedCollection}
         * @return a <code>Collection</code> of the same type but including the {@link PermissionCheckedCollection} interface
         */
        public static <TT> Collection<TT> create(
                Collection<TT> collection, Collection<?> checkedSource)
        {
            if (checkedSource instanceof PermissionCheckedCollection)
            {
                PermissionCheckedCollection<?> source = (PermissionCheckedCollection<?>) checkedSource;
                return create(collection, source.isCutOff(), source.sizeUnchecked(), source.sizeOriginal());
            }
            else
            {
                return collection;
            }
        }

        /**
         * Helper method to create a {@link PermissionCheckedCollection} from an existing <code>Collection</code>
         *
         * @param <TT>
         *            the type of the <code>Collection</code>
         * @param collection
         *            the <code>Collection</code> to proxy
         * @param isCutOff
         *            <tt>true</tt> if permission checking was cut off before completion
         * @param sizeUnchecked
         *            number of entries from the original collection that were not checked
         * @param sizeOriginal
         *            number of entries in the original, pre-checked collection
         * @return a <code>Collection</code> of the same type but including the {@link PermissionCheckedCollection} interface
         */
        @SuppressWarnings("unchecked")
        public static <TT> Collection<TT> create(
                Collection<TT> collection,
                boolean isCutOff, int sizeUnchecked, int sizeOriginal)
        {
            // Create the mixin
            DelegatingIntroductionInterceptor mixin = new PermissionCheckedCollectionMixin<>(
                    isCutOff,
                    sizeUnchecked,
                    sizeOriginal);
            // Create the advisor
            IntroductionAdvisor advisor = new DefaultIntroductionAdvisor(mixin, PermissionCheckedCollection.class);
            // Create Proxy
            return (Collection<TT>) ProxyFactoryUtils.createProxy(collection, advisor);
        }
    }
}
