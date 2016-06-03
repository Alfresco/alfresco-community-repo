
package org.alfresco.filesys.repo;

import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Create Node Event Class
 * 
 * @author gkspencer
 */
public class CreateNodeEvent extends NodeEvent {

    private String relPath;
    private String name;
    
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 */
	public CreateNodeEvent( FileFolderServiceType fType, NodeRef nodeRef, String relPath, String name) {
		super( fType, nodeRef);
		this.setRelPath(relPath);
		this.setName(name);
	}
	
	/**
	 * Return the node event as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("[Create:fType=");
		str.append(getFileType());
		str.append(",nodeRef=");
		str.append(getNodeRef());
		str.append("]");
		
		return str.toString();
	}

    public void setRelPath(String relPath)
    {
        this.relPath = relPath;
    }

    public String getRelPath()
    {
        return relPath;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
