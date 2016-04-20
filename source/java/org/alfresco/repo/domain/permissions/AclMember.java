package org.alfresco.repo.domain.permissions;


/**
 * Entity for <b>alf_access_control_member</b> persistence.
 * 
 * Relates an ACE to an ACL with a position
 * 
 * @author janv
 * @since 3.4
 */
public interface AclMember
{
    public Long getId();
    
    /**
     * Get the ACL to which the ACE belongs
     * 
     * @return - the acl id
     */
    public Long getAclId();
    
    /**
     * Get the ACE included in the ACL
     * 
     * @return - the ace id
     */
    public Long getAceId();
    
    /**
     * Get the position group for this member in the ACL
     * 
     * 0  - implies the ACE is on the object
     * >0 - that it is inherited in some way
     * 
     * The lower values are checked first so take precedence.
     * 
     * @return - the position of the ace in the acl
     */
    public Integer getPos();
}
