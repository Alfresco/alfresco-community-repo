package org.alfresco.filesys.repo;

import org.alfresco.filesys.alfresco.AlfrescoNetworkFile;
import org.alfresco.filesys.alfresco.NetworkFileLegacyReferenceCount;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * NodeRef Based Network File Class
 * 
 * @author gkspencer
 */
public abstract class NodeRefNetworkFile extends AlfrescoNetworkFile 
    implements  NetworkFileLegacyReferenceCount
{

	// Associated node ref
	
	protected NodeRef m_nodeRef;
	
	// Process id of the owner
	
	protected int m_pid;
	
	// Reference count of file opens
	//
	// The same file stream may be re-used if the same process/client opens it multiple times
	
	private int m_openCount = 1;
	
    /**
     * Create a network file object with the specified file/directory name.
     * 
     * @param name File name string.
     */
    public NodeRefNetworkFile(String name)
    {
        super( name);
    }

    /**
     * Create a network file object with the specified file/directory name.
     * 
     * @param name File name string.
     * @param node NodeRef
     */
    public NodeRefNetworkFile(String name, NodeRef node)
    {
        super( name);
        
        m_nodeRef = node;
    }

	/**
	 * Return the node ref
	 * 
	 * @return NodeRef
	 */
	public NodeRef getNodeRef()
	{
		return m_nodeRef;
	}
	
	/**
	 * set the node ref
	 * 
	 * @param nodeRef NodeRef
	 */
	public void setNodeRef( NodeRef nodeRef)
	{
		m_nodeRef = nodeRef;
	}
	
	/**
	 * Return the process id of the owner
	 * 
	 * @return int
	 */
	public final int getProcessId() {
		return m_pid;
	}
	
	/**
	 * Set the owner process id
	 * 
	 * @param pid int
	 */
	public final void setProcessId(int pid) {
		m_pid = pid;
	}
	
	/**
	 * Increment the file open count
	 * 
	 * @return int
	 */
	public synchronized final int incrementOpenCount() {
		return ++m_openCount;
	}
	
	/**
	 * Decrement the file open count
	 * 
	 * @return int
	 */
	public synchronized final int decrementOpenCount() {
		return --m_openCount;
	}
	
    /**
     * Return the open file count
     * 
     * @return int
     */
    public final int getOpenCount() {
        return m_openCount;
    }
    
    private int legacyOpenCount = 0;
    
    /**
     * Increment the legacy file open count
     * 
     * @return int
     */
    public synchronized final int incrementLegacyOpenCount() {
        legacyOpenCount++;
        return legacyOpenCount;
    }
    
    /**
     * Decrement the legacy file open count
     * 
     * @return int
     */
    public synchronized final int decrementLagacyOpenCount() {
        legacyOpenCount--;
        return legacyOpenCount;
    }
    
    /**
     * Return the legacy open file count
     * 
     * @return int
     */
    public final int getLegacyOpenCount() {
        return legacyOpenCount;
    }
}
