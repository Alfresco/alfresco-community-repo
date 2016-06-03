package org.alfresco.repo.security.sync;

/**
 * End of Synchronize Directory
 * 
 * @author mrogers
 * @since 4.2
 *
 */
public class SynchronizeDirectoryEndEvent extends SynchronizeDirectoryEvent
{
   
    public SynchronizeDirectoryEndEvent(Object o, String zone)
    {
        super(o, zone);
       
    }
    
    public SynchronizeDirectoryEndEvent(Object o, String zone, Exception e)
    {
        super(o, zone);
       
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 5374340649898136746L;

}
