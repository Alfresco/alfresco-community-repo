package org.alfresco.repo.security.sync;

import org.springframework.context.ApplicationEvent;

public class SynchronizeEvent extends ApplicationEvent
{
 
    public SynchronizeEvent(Object source)
    {
        super(source);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = -3486329726489553754L;

}
