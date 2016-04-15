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
