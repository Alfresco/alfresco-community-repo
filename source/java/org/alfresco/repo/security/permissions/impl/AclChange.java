package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.ACLType;

/**
 * 
 * @author andyh
 *
 */
public interface AclChange
{
    public Long getBefore();
    public Long getAfter();
    public ACLType getTypeAfter();
    public ACLType getTypeBefore();
}
