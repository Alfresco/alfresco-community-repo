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
package org.alfresco.cmis;

import java.util.List;

/**
 * This class represents descriptor for some <b>Change Log Token</b>
 * 
 * @author Dmitry Velichkevich
 */
public interface CMISChangeLog
{
    /**
     * @return {@link List}&lt;{@link CMISChangeEvent}&gt; collection that contains all available for some <b>Change Log Token</b> <b>Change Events</b> descriptors
     */
    public List<CMISChangeEvent> getChangeEvents();

    /**
     * @return {@link Boolean} value that determines whether repository contains any more <b>Change Events</b> after some {@link CMISChangeLogService}.{@link #getChangeEvents()}
     *         invocation
     */
    public boolean hasMoreItems();

    /**
     * <b>Note:</b> this is optional operation and actual <b>Change Events</b> amount in collection may not be equal to result of this method invocation
     * 
     * @return {@link Integer} value that determines amount of <b>Change Events</b> those were returned
     */
    public Integer getEventCount();

    /**
     * @return {@link String} value that represents the next generated <b>Change Log Token</b>
     */
    public String getNextChangeToken();
}
