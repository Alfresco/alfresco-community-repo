package org.alfresco.repo.security.sync;

/**
 * End of synchronize
 * 
 * @author mrogers
 * @since 4.2
 */
public class SynchronizeEndEvent extends SynchronizeEvent
{
    Exception e;
    public SynchronizeEndEvent(Object source)
    {
        super(source);
    }
    
    public SynchronizeEndEvent(Object source, Exception e)
    {
        super(source);
        this.e = e;
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 5374340649898136746L;

}
