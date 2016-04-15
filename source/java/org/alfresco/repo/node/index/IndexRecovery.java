package org.alfresco.repo.node.index;

/**
 * Interface for components able to recover indexes.
 * 
 * @author Derek Hulley
 */
public interface IndexRecovery
{
    /**
     * Forces a reindex
     */
    public void reindex();
}
