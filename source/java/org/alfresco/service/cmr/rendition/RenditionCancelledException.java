package org.alfresco.service.cmr.rendition;

/**
 * Rendition Service Exception Class
 * 
 * @author Neil McErlean
 * @author Ray Gauss II
 */
public class RenditionCancelledException extends RenditionServiceException
{
    private static final long serialVersionUID = 6369478812653824042L;

    /**
     * Constructs a Rendition Cancelled Exception with the specified message.
     * 
     * @param message   the message string
     */
    public RenditionCancelledException(String message) 
    {
        super(message);
    }

    /**
     * Constructs a Rendition Cancelled Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source    the source exception
     */
    public RenditionCancelledException(String message, Throwable source) 
    {
        super(message, source);
    }

    /**
     * Constructs a Rendition Cancelled Exception with the specified message and {@link RenditionDefinition}.
     * 
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @since 3.5.0
     */
    public RenditionCancelledException(String message, RenditionDefinition renditionDefinition) 
    {
        super(message);
    }
    
    /**
     * Constructs a Rendition Cancelled Exception with the specified message, {@link RenditionDefinition} and
     * source exception
     * .
     * @param message the message string.
     * @param renditionDefinition the rendition definition.
     * @param source the source exception.
     * @since 3.5.0
     */
    public RenditionCancelledException(String message, RenditionDefinition renditionDefinition, Throwable source) 
    {
        super(message, source);
    }

}
