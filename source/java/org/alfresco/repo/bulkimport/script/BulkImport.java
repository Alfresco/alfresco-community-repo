/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.bulkimport.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.bulkimport.ContentStoreMapProvider;
import org.alfresco.repo.processor.BaseProcessorExtension;

/**
 * Custom javascript root object to provide access to the {@link BulkFilesystemImporter} from scripts.
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
	 * @return the {@link List<String>} of store names
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
