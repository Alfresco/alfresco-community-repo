package org.alfresco.repo.content.caching;

import org.springframework.context.ApplicationEvent;

/**
 * Abstract base class for CachingContentStore related application events.
 * 
 * @author Matt Ward
 */
public abstract class CachingContentStoreEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor that captures the source of the event.
     * 
     * @param source
     */
    public CachingContentStoreEvent(Object source)
    {
        super(source);
    }
    
    /**
     * Is the event an instance of the specified type (or subclass)?
     * 
     * @param type {@code Class<?>}
     * @return boolean
     */
    public boolean isType(Class<?> type)
    {
        return type.isAssignableFrom(getClass());
    }
}
