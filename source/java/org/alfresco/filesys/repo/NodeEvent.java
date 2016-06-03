
package org.alfresco.filesys.repo;

import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Node Event Base Class
 *
 * <p>Contains the details of a file/folder node event to be processed by a node monitor thread.
 * 
 * @author gkspencer
 */
public class NodeEvent {

	// Target node
	
	private NodeRef m_nodeRef;

	// File/folder node type
	
	private FileFolderServiceType m_fileType;
	
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 */
	protected NodeEvent( FileFolderServiceType fType, NodeRef nodeRef) {
		m_fileType = fType;
		m_nodeRef = nodeRef;
	}
	
	/**
	 * Return the target node
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getNodeRef() {
		return m_nodeRef;
	}

	/**
	 * Return the node file/folder type
	 * 
	 * @return FileFolderServiceType
	 */
	public final FileFolderServiceType getFileType() {
		return m_fileType;
	}
	
}
