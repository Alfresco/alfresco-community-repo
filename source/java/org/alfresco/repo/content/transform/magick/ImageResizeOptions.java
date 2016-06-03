package org.alfresco.repo.content.transform.magick;

import org.alfresco.api.AlfrescoPublicApi;    

/**
 * Image resize options
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi 
public class ImageResizeOptions
{
    /** The width */
    private int width = -1;
    
    /** The height */
    private int height = -1;
    
    /** Indicates whether the aspect ratio of the image should be maintained */
    private boolean maintainAspectRatio = true;
    
    /** Indicates whether this is a percentage resize */
    private boolean percentResize = false;
    
    /** Indicates whether the resized image is a thumbnail */
    private boolean resizeToThumbnail = false;
    
    /**
     * Indicates that scaling operations should scale up or down to the specified dimensions, as requested.
     * If this argument is false, only resizings that scale the image down will be performed. Scaling up will result in
     * an unchanged image.
     * @since 4.0
     */
    private boolean allowEnlargement = true;
    
    /**
     * Default constructor
     */
    public ImageResizeOptions()
    {
    }
    
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public void setHeight(int height)
    {
        this.height = height;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public void setMaintainAspectRatio(boolean maintainAspectRatio)
    {
        this.maintainAspectRatio = maintainAspectRatio;
    }
    
    public boolean isMaintainAspectRatio()
    {
        return maintainAspectRatio;
    }
    
    public void setPercentResize(boolean percentResize)
    {
        this.percentResize = percentResize;
    }
    
    public boolean isPercentResize()
    {
        return percentResize;
    }
    
    public void setResizeToThumbnail(boolean resizeToThumbnail)
    {
        this.resizeToThumbnail = resizeToThumbnail;
    }
    
    public boolean isResizeToThumbnail()
    {
        return resizeToThumbnail;
    }    
    
    public void setAllowEnlargement(boolean allowEnlargement)
    {
        this.allowEnlargement = allowEnlargement;
    }
    
    public boolean getAllowEnlargement()
    {
        return allowEnlargement;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ImageResizeOptions [width=").append(this.width).append(", height=").append(this.height)
                    .append(", maintainAspectRatio=").append(this.maintainAspectRatio).append(", percentResize=")
                    .append(this.percentResize).append(", resizeToThumbnail=").append(this.resizeToThumbnail)
                    .append(", allowEnlargement=").append(this.allowEnlargement).append("]");
        return builder.toString();
    }
    
}
