/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
