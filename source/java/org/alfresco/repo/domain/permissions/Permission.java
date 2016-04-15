package org.alfresco.repo.domain.permissions;


/**
 * Entity for <b>alf_permission</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface Permission
{
    public Long getId();
    public Long getTypeQNameId();
    public String getName();
}
