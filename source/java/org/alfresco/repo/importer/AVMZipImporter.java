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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipException;

import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.apache.poi.util.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/**
 * Importer which allows the loading of part of an AVM
 *  filesystem from a Zip file, typically called at 
 *  boostrap time.
 * 
 * @author Nick Burch
 */
public class AVMZipImporter implements AVMImporter
{
    private AVMService avmService;
    
    /**
     * Sets the AVM Service to be used for importing to
     * 
     * @param avmService The AVM Service
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    /**
     * Imports all the entries in the Zip File into
     *  the AVM store.
     * @param storePath The full store path, eg stores:/alfresco/foo/
     */
    public void importNodes(ZipFile zip, String storePath) throws IOException, ZipException
    {
        String store = storePath;
        String path = null;
        
        int splitAt = storePath.indexOf(':');
        if(splitAt != -1) {
            store = storePath.substring(0, splitAt);
            path = storePath.substring(splitAt+1);
        }
        
        AVMStoreDescriptor avmStore = avmService.getStore(store);
        if(avmStore == null)
        {
            throw new IllegalArgumentException("No AVM store found with name " + store);
        }
        
        importNodes(zip, avmStore, path);
    }
    
    /**
     * Imports all the entries in the Zip File into
     *  the AVM store.
     * @param avmStore The AVM Store to import into
     * @param storePath Where in the AVM store to unpack into
     */
    @SuppressWarnings("unchecked")
    public void importNodes(ZipFile zip, AVMStoreDescriptor avmStore, String storePath) throws IOException, ZipException
    {
        if(avmStore == null)
        {
            throw new IllegalArgumentException("An AVM Store must be supplied");
        }
        
        // Normalise the store path
        if(storePath == null) {
            storePath = "/";
        }
        if(! storePath.startsWith("/")) {
            storePath = "/" + storePath;
        }
        if(! storePath.endsWith("/")) {
            storePath = storePath + "/";
        }
        
        // Process the zip file
        Enumeration<ZipEntry> entries = zip.getEntries();
        while(entries.hasMoreElements())
        {
            // Grab the entry, and build the AVM path for it
            ZipEntry entry = entries.nextElement();
            String relativeName = entry.getName();
            if(relativeName.startsWith("/")) {
                relativeName = relativeName.substring(1);
            }
            String avmName = storePath + relativeName;
            
            // Import in the appropriate way for the file
            if(entry.isDirectory())
            {
                ensureDirectory(avmStore, avmName);
            }
            else
            {
                String dir = avmName.substring(0, avmName.lastIndexOf('/') + 1);
                ensureDirectory(avmStore, dir);
                
                String avmPath = avmStore.getName() + ":" + avmName;
                importNode(zip.getInputStream(entry), avmPath);
            }
        }
    }
    
    /**
     * Recursively creates directories in AVM as required
     */
    protected void ensureDirectory(AVMStoreDescriptor store, String name)
    {
        if(! name.endsWith("/")) {
            throw new IllegalArgumentException(name + " isn't a directory");
        } else {
            name = name.substring(0, name.length()-1);
        }
        if(! name.startsWith("/")) {
            // Make it absolute
            name = "/" + name;
        }
        if(name.equals("/")) {
            throw new IllegalArgumentException("Can't create a store");
        }
        
        // Build up the AVM path
        int splitAt = name.lastIndexOf('/');
        String avmPath = store.getName() + ":" + name.substring(0, splitAt);
        String avmName = name.substring(splitAt+1);
        
        try {
            avmService.createDirectory(avmPath, avmName);
        } catch(AVMNotFoundException nfe) {
            // Recurse
            String parent = name.substring(0, splitAt+1);
            ensureDirectory(store, parent);
            // And re-do
            avmService.createDirectory(avmPath, avmName);
        } catch(AVMExistsException exists) {
            // Already there
        } catch(AVMWrongTypeException wte) {
            // Currently there, but not a directory...
            throw wte;
        }
    }

    /**
     * Imports the given path and version from the source data.
     *  
     * @param input The stream to read from
     * @param path The AVM path to import
     */
    public void importNode(InputStream input, String path)
        throws IOException
    {
        try
        {
            OutputStream out = avmService.getFileOutputStream(path);
            IOUtils.copy(input, out);
        }
        catch(AVMNotFoundException e)
        {
            int splitAt = path.lastIndexOf('/');
            String avmPath = path.substring(0, splitAt);
            String avmName = path.substring(splitAt+1);
            avmService.createFile(avmPath, avmName, input);
        }
    }
    
    /**
     * Imports the given AVM node from the source data.
     *  
     * @param input The stream to read from
     * @param node The AVM node to import
     */
    public void importNode(InputStream input, AVMNodeDescriptor node)
        throws IOException
    {
        OutputStream out = avmService.getFileOutputStream(node.getPath());
        IOUtils.copy(input, out);
    }
}
