package org.alfresco.repo.domain.permissions;

import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlListProperties;


/**
 * Entity for <b>alf_access_control_list</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface Acl extends AccessControlListProperties
{
    /**
     * Get the long key
     * @return Long
     */
    public Long getId();
    
    /**
     * Get the ACL ID
     * @return String
     */
    public String getAclId();
    
    /**
     * Get the ACL version
     * @return Long
     */
    public Long getAclVersion();
    
    /**
     * Is this the latest version of the acl identified by the acl id string? 
     * @return Boolean
     */
    public Boolean isLatest();
    
    /**
     * Get inheritance behaviour
     * @return Returns the inheritance status of this list
     */
    public Boolean getInherits();
    
    /**
     * Get the ACL from which this one inherits
     * 
     * @return Long
     */
    public Long getInheritsFrom();
    
    /**
     * Get the type for this ACL
     * 
     * @return ACLType
     */
    public ACLType getAclType();
    
    /**
     * Get the ACL inherited from nodes which have this ACL
     * 
     * @return Long
     */
    public Long getInheritedAcl();
    
    /**
     * Is this ACL versioned - if not there will be no old versions of the ACL 
     * and the long id will remain unchanged.
     * 
     * If an acl is versioned it can not be updated - a new copy has to be created,
     *  
     * @return Boolean
     */
    public Boolean isVersioned();
    
    public Boolean getRequiresVersion();
    
    public Long getAclChangeSetId();
}
