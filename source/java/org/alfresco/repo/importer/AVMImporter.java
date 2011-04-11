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
package org.alfresco.repo.importer;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * Importer which allows the loading of part of an AVM
 *  filesystem from external resources.
 * 
 * @author Nick Burch
 */
public interface AVMImporter
{
    /**
     * Imports the given path and version from the source data.
     *  
     * @param input The stream to read from
     * @param path The AVM path to import
     * @param version The AVM version ID
     */
    public void importNode(InputStream input, String path)
        throws IOException;
    
    /**
     * Imports the given AVM node from the source data.
     *  
     * @param input The stream to read from
     * @param node The AVM node to import
     */
    public void importNode(InputStream input, AVMNodeDescriptor node)
        throws IOException;
}
