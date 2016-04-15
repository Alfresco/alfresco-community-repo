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

package org.alfresco.repo.bulkimport.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.bulkimport.ContentStoreMapProvider;
import org.alfresco.repo.processor.BaseProcessorExtension;

/**
 * Custom javascript root object to provide access to the {@link org.alfresco.repo.bulkimport.BulkFilesystemImporter} from scripts.
 * 
 * @since 4.0
 *
 */
public class BulkImport extends BaseProcessorExtension
{
	private ContentStoreMapProvider storeMapProvider;
	private volatile List<String> storeNamesList;

	public void setStoreMapProvider(ContentStoreMapProvider storeMapProvider)
	{
		this.storeMapProvider = storeMapProvider;
	}

	/**
	 * Get a list of the currently registered content stores, from the configured {@link ContentStoreMapProvider}.
	 * @return the {@link List} of store names
	 */
	public List<String> getStoreNames()
	{
		if(storeNamesList == null)
		{
			synchronized(this)
			{
				Set<String> storeNamesSet = storeMapProvider.getStoreMap().keySet();
				if(storeNamesList == null)
					storeNamesList = Collections.unmodifiableList(new ArrayList<String>(storeNamesSet));
			}
			
		}
		return storeNamesList;
	}
}
