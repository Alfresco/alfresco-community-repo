package org.alfresco.repo.bulkimport;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A filesystem walker walks a filesystem, returning directories and files as it goes.
 * 
 * @since 4.0
 */
public interface FilesystemTracker
{
	/**
	 * An estimate of the number of directories and files in the filesystem.
	 * 
	 * @return int
	 */
	int count();
	
	/**
	 * Returns a list of at most 'count' importable items
	 * 
	 * @param count
	 * @return
	 */
//	List<ImportableItem> getImportableItems(int count);
	
	/**
	 * A callback to indicate that the item has been imported into the repository.
	 *
	 * @param nodeRef NodeRef
	 * @param importableItem ImportableItem
	 */
	void itemImported(NodeRef nodeRef, ImportableItem importableItem);

	public BatchProcessWorkProvider<ImportableItem> getWorkProvider();
}
