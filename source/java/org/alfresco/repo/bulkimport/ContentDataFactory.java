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