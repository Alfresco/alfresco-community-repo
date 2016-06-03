package org.alfresco.repo.utils;

import javax.faces.context.FacesContext;

import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Default implementation of {@link TemplateImageResolver} interface, based on {@link FileTypeImageUtils} utility. It relies on availability of {@link FacesContext} instance
 * 
 * @author Dmitry Velichkevich
 */
public class FacesImageResolver implements TemplateImageResolver
{
    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.repository.TemplateImageResolver#resolveImagePathForName(java.lang.String, org.alfresco.service.cmr.repository.FileTypeImageSize)
     */
    @Override
    public String resolveImagePathForName(String filename, FileTypeImageSize size)
    {
        if (FacesContext.getCurrentInstance() != null)
        {
            return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
        }
        else
        {
            return null;
        }
    }
}
