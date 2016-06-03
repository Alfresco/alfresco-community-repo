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
package org.alfresco.service.cmr.remote;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Over the wire, and authentication safe flavor of
 * RepoRemote interface.
 * @author britt
 */
public interface RepoRemoteTransport 
{
    /**
     * Get the root node of the SpacesStore repo.
     * @return The root node ref.
     */
    public NodeRef getRoot(String ticket);
    
    /**
     * Get a listing of a directory.
     * @param dir The node ref of the directory.
     * @return A Map of names to node refs.
     */
    public Map<String, Pair<NodeRef, Boolean>> getListing(String ticket, NodeRef dir);
    
    /**
     * Lookup a node by path relative to a node.
     * @param base The base node ref.
     * @param path The relative path.
     * @return The node ref or null.
     */
    public Pair<NodeRef, Boolean> lookup(String ticket, NodeRef base, String path);
    
    /**
     * Create a file relative to a base node.
     * @param base The base node ref.
     * @param path The relative path.
     * @return A handle.
     */
    public String createFile(String ticket, NodeRef base, String path);
    
    /**
     * Write to an already existing file.
     * @param base The base node ref.
     * @param path The relative path.
     * @return A handle.
     */
    public String writeFile(String ticket, NodeRef base, String path);
    
    /**
     * Create a new directory.
     * @param base The base node ref.
     * @param path The relative path.
     * @return The node ref to the newly created directory.
     */
    public NodeRef createDirectory(String ticket, NodeRef base, String path);
    
    /**
     * Remove a node directly.
     * @param toRemove The node ref to remove.
     */
    public void removeNode(String ticket, NodeRef toRemove);
    
    /**
     * Remove a node via a relative path.
     * @param base The base node ref.
     * @param path The relative path.
     */
    public void removeNode(String ticket, NodeRef base, String path);
    
    /**
     * Rename a node
     * @param base The base node ref.
     * @param src The relative source path.
     * @param dst The relative target path.
     */
    public void rename(String ticket, NodeRef base, String src, String dst);
    
    /**
     * Read a file directly.
     * @param fileRef The node ref of the file.
     * @return A handle.
     */
    public String readFile(String ticket, NodeRef fileRef);
    
    /**
     * Read a file from a relative path.
     * @param base The base node ref.
     * @param path The relative path to the file.
     * @return A handle.
     */
    public String readFile(String ticket, NodeRef base, String path);
    
    /**
     * Read a block of bytes over the wire.
     * @param ticket The authentication ticket.
     * @param handle The remote handle.
     * @param count The number of bytes to try to read.
     * @return A buffer of the bytes read. Length is 0 at EOF.
     */
    public byte[] readInput(String ticket, String handle, int count);
    
    /**
     * Write a portion of a block of bytes over the wire.
     * @param ticket The authentication ticket.
     * @param handle The remote handle.
     * @param buff The buffer with data.
     * @param count The number of bytes to write.
     */
    public void writeOutput(String ticket, String handle, byte[] buff, int count);
    
    /**
     * Close a remote InputStream.
     * @param ticket The authentication ticket.
     * @param handle The handle.
     */
    public void closeInputHandle(String ticket, String handle);
    
    /**
     * Close a remote OutputStream.
     * @param ticket The authentication ticket.
     * @param handle The handle.
     */
    public void closeOutputHandle(String ticket, String handle);
}
