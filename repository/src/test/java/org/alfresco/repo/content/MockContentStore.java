/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentReader;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MockContentStore extends AbstractContentStore
{
        @Override public boolean isWriteSupported()
        {
                return true;
        }

        @Override public ContentReader getReader(String contentUrl)
        {
                return null;
        }

        @Override public boolean isStorageClassesSupported(Set<String> storageClasses)
        {
                return storageClasses == null ||
                        storageClasses.isEmpty() ||
                        (1 == storageClasses.size() && storageClasses.contains(DEFAULT_SC));
        }

        /**
         * @return Returns the complete {@link Set} of supported storage classes by this {@link ContentStore}
         */
        public Set<String> getSupportedStorageClasses()
        {
                return Set.of(DEFAULT_SC);
        }

        /**
         * Updates the storage class for content
         *
         * @param contentUrl The URL of the content that will have its storage classes updated
         * @param storageClasses The new storage classes
         * @param parameters extra parameters
         */
        public void updateStorageClasses(String contentUrl, Set<String> storageClasses, Map<String, Object> parameters)
        {

        }

        /**
         * @param contentUrl the URL of the content for which the storage classes are to be requested
         * @return Returns the current storage classes for the content found at the contentUrl
         */
        public Set<String> findStorageClasses(String contentUrl)
        {
                return Collections.emptySet();
        }

        /**
         * @return Returns the complete collection of allowed storage classes transitions.
         * The key represents the source storage classes while the value (as a {@link Set}) represents all the possible target storage classes.
         */
        public Map<Set<String>, Set<Set<String>>> getStorageClassesTransitions()
        {
                return Collections.emptyMap();
        }

        /**
         * @param contentUrl the URL of the content for which the storage classes transitions are to be requested
         * @return Returns the complete collection of allowed storage classes transitions for the content found at content URL
         */
        public Map<Set<String>, Set<Set<String>>> findStorageClassesTransitions(String contentUrl)
        {
                return Collections.emptyMap();
        }
}

