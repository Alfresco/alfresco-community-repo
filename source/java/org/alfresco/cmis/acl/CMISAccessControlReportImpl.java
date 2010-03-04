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
import java.util.HashMap;
import java.util.List;

import org.alfresco.cmis.CMISAccessControlEntriesGroupedByPrincipalId;
import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISAccessControlReport;
import org.alfresco.cmis.CMISConstraintException;

/**
 * A simple CMIS access control report
 * 
 * @author andyh
 *
 */
public class CMISAccessControlReportImpl implements CMISAccessControlReport
{
    private ArrayList<CMISAccessControlEntry> entries = new ArrayList<CMISAccessControlEntry>();
    
    private boolean extract = false;
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlReport#getAccessControlEntries()
     */
    public List<CMISAccessControlEntry> getAccessControlEntries()
    {
        return entries;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlReport#isExtract()
     */
    public boolean isExact()
    {
       return extract;
    }
    
    /**
     * Set extract.
     * @param extract
     */
    public void setExtract(boolean extract)
    {
        this.extract = extract;
    }

    /*package*/ void addEntry(CMISAccessControlEntry entry)
    {
        removeEntry(entry);
        entries.add(entry);
    }
    
    /*package*/ void removeEntry(CMISAccessControlEntry entry)
    {
        for(int i = 0; i < entries.size(); i++)
        {
            CMISAccessControlEntry current = entries.get(i);
            if(current.getPrincipalId().equals(entry.getPrincipalId()) && current.getPermission().equals(entry.getPermission()))
            {
                entries.remove(i);
                i--;
            } 
        }
        
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlReport#getAccessControlEntriesGroupedByPrincipalId()
     */
    public List<? extends CMISAccessControlEntriesGroupedByPrincipalId> getAccessControlEntriesGroupedByPrincipalId() throws CMISConstraintException
    {
        HashMap<String, CMISAccessControlEntriesGroupedByPrincipalIdImpl> grouped = new HashMap<String, CMISAccessControlEntriesGroupedByPrincipalIdImpl>();
        for(CMISAccessControlEntry entry : getAccessControlEntries())
        {
            CMISAccessControlEntriesGroupedByPrincipalIdImpl value = grouped.get(entry.getPrincipalId());
            if(value == null)
            {
                value = new CMISAccessControlEntriesGroupedByPrincipalIdImpl(entry.getPrincipalId());
                grouped.put(entry.getPrincipalId(), value);
            }
            value.addEntry(entry);
        }
        ArrayList<CMISAccessControlEntriesGroupedByPrincipalIdImpl> answer = new ArrayList<CMISAccessControlEntriesGroupedByPrincipalIdImpl>(grouped.values());
        return answer;
    }
    
  
}
