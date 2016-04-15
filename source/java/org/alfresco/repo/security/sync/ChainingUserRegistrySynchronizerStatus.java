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

import java.util.Date;

/**
 * Reports upon the status of the 
 * ChainingUserRegistrySynchronizer
 * @author mrogers
 */
public interface ChainingUserRegistrySynchronizerStatus
{
    /**
     * Get the start date/time of the last synchronization
     * @return the date/time or null
     */
    Date getSyncStartTime();
    
    /**
     * Get the end date/time of the last synchronization
     * @return the date/time or null
     */
    Date getSyncEndTime();
    
    /**
     * The last error message or null if last sync completed without error 
     * @return the last error message or null
     */
    String getLastErrorMessage();
    
    /**
     * Get the serverid
     * @return the server id of the sever that last ran sync
     */
    String getLastRunOnServer();
      
    
    /**
     * Get the synchronization status
     * @param zoneId - zone id
     * @return the status
     */
    public String getSynchronizationStatus(String zoneId);
  
    /**
     * Get the date/time that the last user/person update completed
     * @param zoneId String
     * @return date or null if sync has never completed
     */
    public Date getSynchronizationLastUserUpdateTime(String zoneId);
    
    /**
     * Get the date/time that the last group update completed
     * @param zoneId String
     * @return date or null if sync has never completed
     */
    public Date getSynchronizationLastGroupUpdateTime(String zoneId);
    
    /**
     * Get the last error message from synchronizing this zone
     * @param zoneId the zone
     * @return the last error message or null if the last sync did not have an error
     */
    public String getSynchronizationLastError(String zoneId);
    
    /**
     * Get the synchronization summary message for the specified zone
     * @param zoneId the zone
     * @return the summary or null
     */
    public String getSynchronizationSummary(String zoneId);

    /**
     * 
     * @return String
     */
    public String getSynchronizationStatus();

}
