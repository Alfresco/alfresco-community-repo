package org.alfresco.repo.security.sync;

/**
 * Synchronize directory 
 * 
 * @author mrogers
 * @since 4.2
 */
public abstract class SynchronizeDirectoryEvent extends SynchronizeEvent
{
    private String zone;
    public SynchronizeDirectoryEvent(Object source, String zone)
    {
        super(source);
        this.zone = zone;
    }
    
    public String getZone()
    {
        return zone;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3486329726489553754L;

}
