package org.alfresco.service.cmr.quickshare;

import java.io.Serializable;

/**
 * Data transfer object for holding quick share information.
 *
 * @author Alex Miller
 * @since Cloud/4.2
 */
public class QuickShareDTO implements Serializable
{
    private static final long serialVersionUID = -2163618127531335360L;

    private String sharedId;

    /**
     * Default constructor
     * 
     * @param sharedId The quick share id
     */
    public QuickShareDTO(String sharedId)
    {
        this.sharedId = sharedId;
    }

    /**
     * Copy constructor
     */
    public QuickShareDTO(QuickShareDTO from) 
    {
        this(from.getId());
    }
    
    /**
     * @return The share id
     */
    public String getId()
    {
        return this.sharedId;
    }
}
