package org.alfresco.service.cmr.repository;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;   

/**
 * Interface contract for the conversion of file name to a fully qualified icon image path for use by
 * templating and scripting engines executing within the repository context.
 * <p>
 * Generally this contract will be implemented by classes that have access to say the webserver
 * context which can be used to generate an icon image for a specific filename.
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
public interface TemplateImageResolver extends Serializable
{
    /**
     * Resolve the qualified icon image path for the specified filename 
     * 
     * @param filename      The file name to resolve image path for
     * @param size          Enum representing the size of the image to retrieve
     * 
     * @return image path for the specified filename and image size
     */
    public String resolveImagePathForName(String filename, FileTypeImageSize size);
}
