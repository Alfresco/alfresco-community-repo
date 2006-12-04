/**
 * 
 */
package org.alfresco.service.cmr.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * A highly simplified remote interface for the repo.
 * @author britt
 */
public interface RepoRemote 
{
    /**
     * Get the root node of the SpacesStore repo.
     * @return The root node ref.
     */
    public NodeRef getRoot();
    
    /**
     * Get a listing of a directory.
     * @param dir The node ref of the directory.
     * @return A Map of names to node refs.
     */
    public Map<String, Pair<NodeRef, QName>> getListing(NodeRef dir);
    
    /**
     * Lookup a node by path relative to a node.
     * @param base The base node ref.
     * @param path The relative path.
     * @return The node ref or null.
     */
    public NodeRef lookup(NodeRef base, String path);
    
    /**
     * Create a file relative to a base node.
     * @param base The base node ref.
     * @param path The relative path.
     * @return An OutputStream.
     */
    public OutputStream createFile(NodeRef base, String path);
    
    /**
     * Write to an already existing file.
     * @param base The base node ref.
     * @param path The relative path.
     * @return An OutputStream
     */
    public OutputStream writeFile(NodeRef base, String path);
    
    /**
     * Create a new directory.
     * @param base The base node ref.
     * @param path The relative path.
     * @return The node ref to the newly created directory.
     */
    public NodeRef createDirectory(NodeRef base, String path);
    
    /**
     * Remove a node directly.
     * @param toRemove The node ref to remove.
     */
    public void removeNode(NodeRef toRemove);
    
    /**
     * Remove a node via a relative path.
     * @param base The base node ref.
     * @param path The relative path.
     */
    public void removeNode(NodeRef base, String path);
    
    /**
     * Rename a node
     * @param base The base node ref.
     * @param src The relative source path.
     * @param dst The relative target path.
     */
    public void rename(NodeRef base, String src, String dst);
    
    /**
     * Read a file directly.
     * @param fileRef The node ref of the file.
     * @return An InputStream.
     */
    public InputStream readFile(NodeRef fileRef);
    
    /**
     * Read a file from a relative path.
     * @param base The base node ref.
     * @param path The relative path to the file.
     * @return An InputStream.
     */
    public InputStream readFile(NodeRef base, String path);
}
