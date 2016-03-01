 
package org.alfresco.module.org_alfresco_module_rm;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @deprecated as of 2.1 see {@link org.alfresco.module.org_alfresco_module_rm.admin.NotCustomisableMetadataException}
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
