package org.alfresco.repo.security.permissions;

import java.io.Serializable;

import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;

public interface AccessControlEntry extends Comparable<AccessControlEntry>, Serializable
{
    public Integer getPosition();

    public PermissionReference getPermission();

    public String getAuthority();
    
    public AuthorityType getAuthorityType();

    public AccessStatus getAccessStatus();

    public ACEType getAceType();

    public AccessControlEntryContext getContext();
}
