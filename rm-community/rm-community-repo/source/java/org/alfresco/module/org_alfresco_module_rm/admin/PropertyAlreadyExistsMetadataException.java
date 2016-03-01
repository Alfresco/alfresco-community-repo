 
package org.alfresco.module.org_alfresco_module_rm.admin;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Custom metadata exception.
 * 
 * @author Roy Wethearll
 * @since 2.1
 * @see org.alfresco.module.org_alfresco_module_rm.PropertyAlreadyExistsMetadataException
 */
public class PropertyAlreadyExistsMetadataException extends CustomMetadataException
{
    private static final long serialVersionUID = -6194867814140009959L;

    public static final String MSG_PROPERTY_ALREADY_EXISTS = "rm.admin.property-already-exists";
    
    public PropertyAlreadyExistsMetadataException(String propIdAsString)
    {
        super(I18NUtil.getMessage(MSG_PROPERTY_ALREADY_EXISTS, propIdAsString));
    }
}
