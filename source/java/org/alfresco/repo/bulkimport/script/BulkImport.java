
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
