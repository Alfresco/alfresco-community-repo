/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.lucene.index;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of a significant event relating to a Lucene index. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 */
public class IndexEvent extends ApplicationEvent
{

    private static final long serialVersionUID = -4616231785087405506L;

    /** The event description. */
    private final String description;

    /** Its instance count. */
    private final int count;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source index monitor
     * @param description
     *            the event description
     * @param count
     *            its instance count
     */
    public IndexEvent(IndexMonitor source, String description, int count)
    {
        super(source);
        this.description = description;
        this.count = count;
    }

    /**
     * Gets the source index monitor.
     * 
     * @return the index monitor
     */
    public IndexMonitor getIndexMonitor()
    {
        return (IndexMonitor) getSource();
    }

    /**
     * Gets the event description.
     * 
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Gets the event instance count.
     * 
     * @return the count
     */
    public int getCount()
    {
        return this.count;
    }

}
