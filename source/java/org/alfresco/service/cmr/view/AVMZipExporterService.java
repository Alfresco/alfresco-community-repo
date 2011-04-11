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
package org.alfresco.service.cmr.view;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.apache.tools.zip.ZipOutputStream;

/**
 * Exporter which allows the saving of part of an AVM
 *  filesystem to a Zip file.
 * 
 * @author Nick Burch
 */
public interface AVMZipExporterService
{
    /**
     * Exports the given path and version as a zip file, stored
     *  in the specified file.
     *  
     * @param output The File to store the Zip in
     * @param path The AVM path to export
     * @param version The AVM version IO 
     * @param recurse Should the export recurse into directories?
     */
    public void export(File output, int version, String path, boolean recurse)
        throws IOException, ZipException;
    
    /**
     * Exports the given path and version into an already open
     *  Zip file. This method can be used to output multiple different
     *  AVM resources into one file.
     *  
     * @param output The File to store the Zip in
     * @param path The AVM path to export
     * @param version The AVM version IO 
     * @param recurse Should the export recurse into directories?
     */
    public void export(ZipOutputStream out, int version, String path, boolean recurse)
        throws IOException, ZipException;
    
    /**
     * Exports the given AVM node into an already open
     *  Zip file. This method can be used to output multiple different
     *  AVM resources into one file.
     *  
     * @param output The File to store the Zip in
     * @param node The AVM node to export
     * @param recurse Should the export recurse into directories?
     */
    public void export(ZipOutputStream out, AVMNodeDescriptor node, boolean recurse)
        throws IOException, ZipException;
}
