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

package org.alfresco.repo.bulkimport;

import java.util.Map;

import org.alfresco.repo.content.ContentStore;

/**
 * 
 * @since 4.0
 *
 */
public interface ContentStoreMapProvider
{
	/**
	 * Get a map of the currently registered {@link ContentStore}, keyed by store name
	 * @return a {@link Map}
	 */
	public Map<String, ContentStore> getStoreMap();
	
	/**
	 * Check that the given store name is part of the map. It it is not, an exception will 
	 * be thrown. If it is, it will be returned.
	 *
	 * @param storeName the store 
	 * @return the corresponding {@link ContentStore}
	 */
	public ContentStore checkAndGetStore(String storeName);

}