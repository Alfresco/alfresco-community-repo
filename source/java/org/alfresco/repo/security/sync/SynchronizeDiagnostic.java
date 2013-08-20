package org.alfresco.repo.security.sync;

import java.util.Collection;
import java.util.Date;

/**
 * The result of a synch
 * @author mrogers
 *
 */
public interface SynchronizeDiagnostic
{
    /**
     * Is the user directory active
     * @return true if active
     */
    public boolean isActive();
    
    /**
     * get the list of users who would be synchronised
     * @return the list of users who would be synchronized
     */
    public Collection<String> getUsers();
    
    /**
     * get the list of groups who would be syncronised
     * @return the list of groups who would be synchronized
     */
    public Collection<String> getGroups();
    
    /**
     * 
     * @return
     */
    public Date getPersonLastSynced();
 
    /**
     * 
     * @return
     */
    public Date getGroupLastSynced();

}
