/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.model.filefolder.loader;

import org.alfresco.service.cmr.remote.FileFolderRemote;
import org.alfresco.service.cmr.remote.LoaderRemote;

/**
 * Helper class to store remote service connections.
 * 
 * @author Derek Hulley
 */
public class LoaderServerProxy
{
    public final String rmiUrl;
    public final String ticket;
    public final FileFolderRemote fileFolderRemote;
    public final LoaderRemote loaderRemote;
    
    public LoaderServerProxy(
            String rmiUrl,
            String ticket,
            FileFolderRemote fileFolderRemote,
            LoaderRemote loaderRemote)
    {
        this.rmiUrl = rmiUrl;
        this.ticket = ticket;
        this.fileFolderRemote = fileFolderRemote;
        this.loaderRemote = loaderRemote;
    }
}

