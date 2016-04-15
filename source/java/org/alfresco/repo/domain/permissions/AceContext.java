package org.alfresco.repo.domain.permissions;


/**
 * Entity for <b>alf_ace_context</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public interface AceContext
{
    public Long getId();
    public String getClassContext();
    public String getPropertyContext();
    public String getKvpContext();
}
