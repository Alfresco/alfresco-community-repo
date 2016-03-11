package org.alfresco.module.org_alfresco_module_rm.admin;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Custom metadata exception.
 * 
 * @author Roy Wethearll
 * @since 2.1
 * @see org.alfresco.module.org_alfresco_module_rm.NotCustomisableMetadataException
 */
public class NotCustomisableMetadataException extends CustomMetadataException
{
    private static final long serialVersionUID = -6194867814140009959L;
    public static final String MSG_NOT_CUSTOMISABLE = "rm.admin.not-customisable";
    
    public NotCustomisableMetadataException(String aspectName)
    {
        super(I18NUtil.getMessage(MSG_NOT_CUSTOMISABLE, aspectName));
    }
}
