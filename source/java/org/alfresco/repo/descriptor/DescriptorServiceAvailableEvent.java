package org.alfresco.repo.descriptor;

import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the availability of the {@link DescriptorService}. Useful for
 * Monitoring purposes.
 * 
 * @author dward
 */
public class DescriptorServiceAvailableEvent extends ApplicationEvent
{

    private static final long serialVersionUID = 8217523101300405165L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source descriptor service
     */
    public DescriptorServiceAvailableEvent(DescriptorService source)
    {
        super(source);
    }

    /**
     * Gets the descriptor service that raised the event.
     * 
     * @return the descriptor service
     */
    public DescriptorService getDescriptorService()
    {
        return (DescriptorService) getSource();
    }
}
