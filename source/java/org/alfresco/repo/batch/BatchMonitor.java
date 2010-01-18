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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.batch;

import java.util.Date;

/**
 * An interface that allows the monitoring of metrics relating to a potentially long-running batch process.
 * 
 * @author dward
 */
public interface BatchMonitor
{
    /**
     * Gets the process name.
     * 
     * @return the process name
     */
    public String getProcessName();

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime();

    /**
     * Gets the total number of results.
     * 
     * @return the total number of results
     */
    public int getTotalResults();

    /**
     * Gets the ID of the entry being processed
     * 
     * @return the current entry id
     */
    public String getCurrentEntryId();

    /**
     * Gets the number of successfully processed entries.
     * 
     * @return the successfully processed entries
     */
    public int getSuccessfullyProcessedEntries();

    /**
     * Gets the progress expressed as a percentage.
     * 
     * @return the progress expressed as a percentage
     */
    public String getPercentComplete();

    /**
     * Gets the total number of errors.
     * 
     * @return the total number of errors
     */
    public int getTotalErrors();

    /**
     * Gets the stack trace of the last error.
     * 
     * @return the stack trace of the last error
     */
    public String getLastError();

    /**
     * Gets the entry id that caused the last error.
     * 
     * @return the last error entry id
     */
    public String getLastErrorEntryId();

    /**
     * Gets the end time.
     * 
     * @return the end time
     */
    public Date getEndTime();
}
