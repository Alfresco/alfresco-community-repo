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
package org.alfresco.cmis.changelog;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.cmis.CMISChangeEvent;
import org.alfresco.cmis.CMISChangeLog;

/**
 * CMISChangeLog Implementation
 * 
 * @author Dmitry Velichkevich
 */
public class CMISChangeLogImpl implements CMISChangeLog
{
    private boolean hasMoreItems;
    private List<CMISChangeEvent> changeEvents = new LinkedList<CMISChangeEvent>();
    private String nextChangeToken;

    /**
     * @see org.alfresco.cmis.CMISChangeLog#getChangeEvents()
     */
    public List<CMISChangeEvent> getChangeEvents()
    {
        return changeEvents;
    }

    /**
     * Set the change events
     * 
     * @param changeEvents list of change events
     */
    protected void setChangeEvents(List<CMISChangeEvent> changeEvents)
    {
        this.changeEvents = changeEvents;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLog#getEventCount()
     */
    public Integer getEventCount()
    {
        return changeEvents.size();
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLog#getNextChangeToken()
     */
    public String getNextChangeToken()
    {
        return nextChangeToken;
    }
    
    /**
     * Set the next ChangeToken
     * 
     * @param nextChangeToken the next ChangeToken
     */
    public void setNextChangeToken(String nextChangeToken) 
    {
		this.nextChangeToken = nextChangeToken;
	}

    /**
     * @see org.alfresco.cmis.CMISChangeLog#hasMoreItems()
     */
    public boolean hasMoreItems()
    {
        return hasMoreItems;
    }

    /**
     * Set the hasMoreItems value
     * 
     * @param hasMoreItems hasMoreItems value
     */
    protected void setHasMoreItems(boolean hasMoreItems)
    {
        this.hasMoreItems = hasMoreItems;
    }
	
}
