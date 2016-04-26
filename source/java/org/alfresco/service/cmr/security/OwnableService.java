package org.alfresco.service.cmr.security;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service support around managing ownership.
 * 
 * @author Andy Hind
 */
public interface OwnableService
{
    public static String NO_OWNER = "";

    /**
     * Get the username of the owner of the given object.
     *  
     * @param nodeRef NodeRef
     * @return the username or null if the object has no owner
     */
    @Auditable(parameters = {"nodeRef"})
    public String getOwner(NodeRef nodeRef);
    
    /**
     * Set the owner of the object.
     * 
     * @param nodeRef NodeRef
     * @param userName String
     */
    @Auditable(parameters = {"nodeRef", "userName"})
    public void setOwner(NodeRef nodeRef, String userName);
    
    /**
     * Set the owner of the object to be the current user.
     * 
     * @param nodeRef NodeRef
     */
    @Auditable(parameters = {"nodeRef"})
    public void takeOwnership(NodeRef nodeRef);
    
    /**
     * Does the given node have an owner?
     * 
     * @param nodeRef NodeRef
     * @return boolean
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean hasOwner(NodeRef nodeRef);
}
