package org.alfresco.repo.domain.schema;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the availability of the Alfresco Database Schema. Any class requiring
 * the database must wait until after this event.
 * 
 * @author dward
 */
public class SchemaAvailableEvent extends ApplicationEvent
{

    private static final long serialVersionUID = -1882393521985043422L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public SchemaAvailableEvent(Object source)
    {
        super(source);
    }

}
