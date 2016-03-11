package org.alfresco.module.org_alfresco_module_rm;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @deprecated as of 2.1 see {@link org.alfresco.module.org_alfresco_module_rm.admin.InvalidCustomAspectMetadataException}
 */
public class InvalidCustomAspectMetadataException extends CustomMetadataException
{
    private static final long serialVersionUID = -6194867814140009959L;
    public static final String MSG_INVALID_CUSTOM_ASPECT = "rm.admin.invalid-custom-aspect";
    
    public InvalidCustomAspectMetadataException(QName customAspect, String aspectName)
    {
        super(I18NUtil.getMessage(MSG_INVALID_CUSTOM_ASPECT, customAspect, aspectName));
    }
}
