
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