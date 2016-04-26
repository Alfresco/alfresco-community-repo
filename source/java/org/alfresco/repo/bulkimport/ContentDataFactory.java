
package org.alfresco.repo.bulkimport;

import java.io.File;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentData;

/**
 * Build a {@link ContentData} out of a given {@link ContentStore} and a given {@link File} within
 * that store
 * 
 * @since 4.0
 *
 */
public interface ContentDataFactory
{
	/**
	 * Create a {@link ContentData} by combining the given {@link ContentStore}'s root location and the {@link File}'s path within that store.
	 * The given file must therefore be accessible within the content store's configured root location.
	 * The encoding and mimetype will be guessed from the given file. 
	 * 
	 * @param store			The {@link ContentStore} in which the file should be
	 * @param contentFile	The {@link File} to check
	 * @return the constructed {@link ContentData}
	 */
	public ContentData createContentData(ContentStore store, File contentFile);

}