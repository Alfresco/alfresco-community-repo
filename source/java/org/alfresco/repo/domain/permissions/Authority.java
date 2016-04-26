package org.alfresco.repo.domain.permissions;


/**
 * Entity for <b>alf_authority</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface Authority
{
    public Long getId();
    public String getAuthority();
    public Long getCrc();
}
