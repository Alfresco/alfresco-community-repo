package org.alfresco.repo.domain.permissions;

import org.alfresco.repo.security.permissions.ACEType;


/**
 * Entity for <b>alf_access_control_entry</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface Ace
{
    public Long getId();
    public Long getPermissionId();
    public Long getAuthorityId();
    public boolean isAllowed();
    public Integer getApplies();
    public Long getContextId();
    public ACEType getAceType();
}
