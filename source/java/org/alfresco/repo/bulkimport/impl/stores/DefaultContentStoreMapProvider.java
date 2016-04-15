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

package org.alfresco.repo.bulkimport.impl.stores;

import java.util.HashMap;

import org.alfresco.repo.content.ContentStore;

/**
 * Provides a default {@link java.util.Map} of registered content stores.
 * Use when the Content Store Selector is not available (e.g on community releases).
 * 
 * @since 4.0
 *
 */
public class DefaultContentStoreMapProvider extends AbstractContentStoreMapProvider
{
	/**
	 *  the default store name, should match the default store defined by the content store selector aspect.
	 */
    private String defaultStoreName;
	
	/**
	 * Default implementation, relies on the default {@link ContentStore}.
	 */
	protected void setUpStoreMap()
	{
		storeMap = new HashMap<String, ContentStore>();
		storeMap.put(defaultStoreName, contentStore);
	}

	// boilerplate setters
	
	public void setDefaultStoreName(String defaultStoreName)
	{
		this.defaultStoreName = defaultStoreName;
	}
}
