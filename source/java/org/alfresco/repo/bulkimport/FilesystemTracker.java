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
