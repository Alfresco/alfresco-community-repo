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
	 * @return
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
	 * @param nodeRef
	 * @param importableItem
	 */
	void itemImported(NodeRef nodeRef, ImportableItem importableItem);

	public BatchProcessWorkProvider<ImportableItem> getWorkProvider();
}
