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

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of a significant event relating to a bulk filesystem
 * import. Useful for Monitoring purposes.
 * 
 * @since 4.0
 */
public class BulkFSImportEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 6249867689460133967L;

	/**
     * The Constructor.
     * 
     * @param source
     *            the source index monitor
     */
    public BulkFSImportEvent(BulkFilesystemImporter source)
    {
        super(source);
    }
    
    public BulkFilesystemImporter getBulkFilesystemImporter()
    {
    	return (BulkFilesystemImporter)source;
    }
}
