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

import java.util.Iterator;
import java.util.Map;

import org.alfresco.repo.bulkimport.ContentStoreMapProvider;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Common elements of the role of a {@link ContentStoreMapProvider}.
 * Extending classes should implement {@link #setUpStoreMap()} to initialize the {@link Map}.
 * 
 * @since 4.0
 *
 */
public abstract class AbstractContentStoreMapProvider extends AbstractLifecycleBean implements ContentStoreMapProvider
{
	private final static Log logger = LogFactory.getLog(AbstractContentStoreMapProvider.class);
    
	protected ContentStore contentStore;
	protected Map<String,ContentStore> storeMap;
	
	protected abstract void setUpStoreMap();
	
	/**
	 * set up the map on startup. see {@link #setUpStoreMap()}.
	 */
	protected void onBootstrap(ApplicationEvent event)
	{
		setUpStoreMap();
	}

	
	protected void onShutdown(ApplicationEvent event)
	{
		// nothing particular to do
	}
	
	/**
	 * Check that the given store name is in the list. 
	 * Also check it's an instance of {@link FileContentStore}. If it's not, output a warning
	 * as non-file-based implementations have not been tested and may be unsupported.
	 * 
	 * @param storeName	the store name to check
	 */
	public ContentStore checkAndGetStore(String storeName)
	{
		ContentStore store = storeMap.get(storeName);
    	if(store == null)
    	{
    		String validStores ="";
         	Iterator<String> it = storeMap.keySet().iterator();
         	while (it.hasNext()) 
         	{
         		validStores += "'" + it.next() + "'" + (it.hasNext() ? " , " : "");
         	}
         	throw new IllegalArgumentException("given store name : '" + storeName + "' is not part of the registered stores : " + validStores);
         }
         if(!(store instanceof FileContentStore))
         {
        	 // letting you off with a warning :)
        	 // some people may have a custom content store for which the import could work in this case too ...
        	 if(logger.isWarnEnabled())
        	 {
        		 logger.warn("selected store '" + storeName + "' is not a FileContentStore. Is the implementation based on local files ?");
        	 }
         }
         
         return store;
	}
	
	/**
	 * see {@link ContentStoreMapProvider#getStoreMap()}
	 */
	public Map<String,ContentStore> getStoreMap()
	{		
		return storeMap;
	}
	
	public ContentStore getContentStore()
	{
		return contentStore;
	}

	public void setContentStore(ContentStore contentStore)
	{
		this.contentStore = contentStore;
	}


}
