package org.alfresco.repo.dictionary;

import org.springframework.context.ApplicationEvent;

public class DictionaryRepositoryBootstrappedEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 113L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public DictionaryRepositoryBootstrappedEvent(Object source)
    {
        super(source);
    }

}
