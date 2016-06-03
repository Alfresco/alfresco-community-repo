package org.alfresco.util;

import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Default implementation of {@link TemplateImageResolver} interface.
 */
public class DefaultImageResolver implements TemplateImageResolver
{
    private static final long serialVersionUID = 7531417785209341858L;

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.repository.TemplateImageResolver#resolveImagePathForName(java.lang.String, org.alfresco.service.cmr.repository.FileTypeImageSize)
     */
    @Override
    public String resolveImagePathForName(String filename, FileTypeImageSize size)
    {
        return null;
    }
}
