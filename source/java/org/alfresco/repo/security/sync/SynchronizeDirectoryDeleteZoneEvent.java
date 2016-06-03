package org.alfresco.repo.security.sync;

/**
 * Delete od zone that has been synchronized
 * 
 * @author mrogers
 * @since 4.2
 */
public class SynchronizeDirectoryDeleteZoneEvent extends SynchronizeDirectoryEvent
{
    private String batchProcessNames[];
    public SynchronizeDirectoryDeleteZoneEvent(Object o, String zone, String batchProcessNames[])
    {
        super(o, zone);
        this.batchProcessNames = batchProcessNames;
    }
    
    public String[] getBatchProcessNames()
    {
        return batchProcessNames;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5374340649898136746L;

}
