/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
