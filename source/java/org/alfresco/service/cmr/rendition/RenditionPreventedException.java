package org.alfresco.service.cmr.rendition;

import org.alfresco.repo.rendition.RenditionPreventionRegistry;

/**
 * This exception is thrown if an attempt is made to render a node which has a {@link RenditionPreventionRegistry content class}
 * registered to prevent rendition.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.1
 */
public class RenditionPreventedException extends RenditionServiceException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Rendition Service Exception with the specified message.
     * 
     * @param message   the message string
     */
    public RenditionPreventedException(String message) 
    {
        super(message);
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source    the source exception
     */
    public RenditionPreventedException(String message, Throwable source) 
    {
        super(message, source);
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and {@link RenditionDefinition}.
     * 
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     */
    public RenditionPreventedException(String message, RenditionDefinition renditionDefinition) 
    {
        super(message);
    }
    
    /**
     * Constructs a Rendition Service Exception with the specified message, {@link RenditionDefinition} and
     * source exception
     * .
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @param source the source exception.
     */
    public RenditionPreventedException(String message, RenditionDefinition renditionDefinition, Throwable source) 
    {
        super(message, source);
    }
}
