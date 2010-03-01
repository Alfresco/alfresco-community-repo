/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.cmis.acl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.cmis.CMISAccessControlEntriesGroupedByPrincipalId;
import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISConstraintException;

/**
 * @author andyh
 *
 */
public class CMISAccessControlEntriesGroupedByPrincipalIdImpl implements CMISAccessControlEntriesGroupedByPrincipalId
{   
    private String principalId;
    
    private List<String> directPermissions = new ArrayList<String>();
    
    private List<String> indirectPermissions = new ArrayList<String>();

    /*package */ CMISAccessControlEntriesGroupedByPrincipalIdImpl(String principalId)
    {
        this.principalId = principalId;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlEntriesGroupedByPrincipalId#getDirectPermissions()
     */
    public List<String> getDirectPermissions()
    {
        return directPermissions;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlEntriesGroupedByPrincipalId#getInirectPermissions()
     */
    public List<String> getIndirectPermissions()
    {
       return indirectPermissions;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlEntriesGroupedByPrincipalId#getPrincipalId()
     */
    public String getPrincipalId()
    {
       return principalId;
    }
    
    /* package */ void addEntry(CMISAccessControlEntry entry) throws CMISConstraintException
    {
        if(!principalId.equals(entry.getPrincipalId()))
        {
            throw new CMISConstraintException("Grouping error in principal id");
        }
        if(entry.getDirect())
        {
            directPermissions.add(entry.getPermission());
        }
        else
        {
            indirectPermissions.add(entry.getPermission());
        }
    }
    

   
}
