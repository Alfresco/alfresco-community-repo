
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
