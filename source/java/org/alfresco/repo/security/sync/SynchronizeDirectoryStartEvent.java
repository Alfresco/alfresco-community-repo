package org.alfresco.repo.security.sync;

/**
 * Start of synchronize directory
 * 
 * @author mrogers
 * @since 4.2
 */
public class SynchronizeDirectoryStartEvent extends SynchronizeDirectoryEvent
{
    private String batchProcessNames[];
    public SynchronizeDirectoryStartEvent(Object o, String zone, String batchProcessNames[])
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
