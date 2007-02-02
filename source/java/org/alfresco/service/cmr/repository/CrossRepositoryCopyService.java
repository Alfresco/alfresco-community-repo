/**
 * 
 */
package org.alfresco.service.cmr.repository;

/**
 * Simple interface for copying between the two repsitory implementations.
 * @author britt
 */
public interface CrossRepositoryCopyService 
{
    /**
     * This copies recursively src, which may be a container or a content type
     * to dst, which must be a container. Copied nodes will have the copied from aspect
     * applied to them.
     * @param src The node to copy.
     * @param dst The container to copy it into.
     * @param name The name to give the copy.
     */
    public void copy(NodeRef src, NodeRef dst, String name);
}
