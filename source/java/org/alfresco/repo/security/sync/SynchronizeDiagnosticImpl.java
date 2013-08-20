package org.alfresco.repo.security.sync;

import java.util.Collection;
import java.util.Date;

public class SynchronizeDiagnosticImpl implements SynchronizeDiagnostic
{
    private boolean isActive = true;
    private Collection<String> users;
    private Collection<String> groups;
    private Date personLastSynced;
    private Date groupLastSynced;

    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }
    
    public void setGroups(Collection<String> groups)
    {
        this.groups = groups;
    }
    
    public void setUsers(Collection<String> users)
    {
        this.users = users;
    }

    @Override
    public boolean isActive()
    {
        return isActive;
    }

    @Override
    public Collection<String> getUsers()
    {
        return users;
    }

    @Override
    public Collection<String> getGroups()
    {
        return groups;
    }
    

    
    public String toString()
    {
        return "SynchronizeDiagnosticImpl: isActive," + isActive ; 
    }

    public void setPersonLastSynced(Date personLastSynced)
    {
        this.personLastSynced = personLastSynced;
    }

    public Date getPersonLastSynced()
    {
        return personLastSynced;
    }

    public void setGroupLastSynced(Date groupLastSynced)
    {
        this.groupLastSynced = groupLastSynced;
    }

    public Date getGroupLastSynced()
    {
        return groupLastSynced;
    }

}
