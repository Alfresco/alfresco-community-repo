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