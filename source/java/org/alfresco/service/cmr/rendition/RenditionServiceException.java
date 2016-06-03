package org.alfresco.service.cmr.rendition;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Rendition Service Exception Class
 * 
 * @author Neil McErlean
 */
public class RenditionServiceException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -6947067735970465937L;
    private final RenditionDefinition renditionDefinition;

    /**
     * Constructs a Rendition Service Exception with the specified message.
     * 
     * @param message   the message string
     */
    public RenditionServiceException(String message) 
    {
        super(message);
        this.renditionDefinition = null;
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source    the source exception
     */
    public RenditionServiceException(String message, Throwable source) 
    {
        super(message, source);
        this.renditionDefinition = null;
    }

    /**
     * Constructs a Rendition Service Exception with the specified message and {@link RenditionDefinition}.
     * 
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @since 3.5.0
     */
    public RenditionServiceException(String message, RenditionDefinition renditionDefinition) 
    {
        super(message);
        this.renditionDefinition = renditionDefinition;
    }
    
    /**
     * Constructs a Rendition Service Exception with the specified message, {@link RenditionDefinition} and
     * source exception
     * .
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @param source the source exception.
     * @since 3.5.0
     */
    public RenditionServiceException(String message, RenditionDefinition renditionDefinition, Throwable source) 
    {
        super(message, source);
        this.renditionDefinition = renditionDefinition;
    }

    /**
     * Retrieves the {@link RenditionDefinition} associated with this exception.
     * @return the rendition definition, which may be <tt>null</tt>.
     * @since 3.5.0
     */
    public RenditionDefinition getRenditionDefinition()
    {
        return this.renditionDefinition;
    }
}
