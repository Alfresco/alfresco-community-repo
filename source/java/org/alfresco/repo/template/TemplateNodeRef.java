package org.alfresco.repo.template;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Contract supported by Template API objects that represent a repository object via a NodeRef
 * and associated minimum properties such as as Type and Name.
 * 
 * @author Kevin Roast
 */
public interface TemplateNodeRef extends Serializable
{
    /**
     * @return The GUID for the node
     */
    public String getId();
    
    /**
     * @return Returns the NodeRef this Node object represents
     */
    public NodeRef getNodeRef();
    
    /**
     * @return Returns the type.
     */
    public QName getType();
    
    /**
     * @return The display name for the node
     */
    public String getName();
}
