/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.content.filestore;

import java.io.File;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the existence of a {@link FileContentStore}. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 */
public class FileContentStoreCreatedEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 7090069096441126707L;

    /** The root directory of the store. */
    private final File rootDirectory;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source content store
     * @param rootDirectory
     *            the root directory
     */
    public FileContentStoreCreatedEvent(FileContentStore source, File rootDirectory)
    {
        super(source);
        this.rootDirectory = rootDirectory;
    }

    /**
     * Gets the root directory.
     * 
     * @return the root directory
     */
    public File getRootDirectory()
    {
        return this.rootDirectory;
    }
}
