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
