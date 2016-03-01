 
package org.alfresco.module.org_alfresco_module_rm;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @deprecated as of 2.1 see {@link org.alfresco.module.org_alfresco_module_rm.admin.CannotApplyConstraintMetadataException}
 */
public class CannotApplyConstraintMetadataException extends CustomMetadataException
{
    private static final long serialVersionUID = -6194867814140009959L;
    public static final String MSG_CANNOT_APPLY_CONSTRAINT = "rm.admin.cannot-apply-constraint";
    
    public CannotApplyConstraintMetadataException(QName lovConstraint, String propIdAsString, QName dataType)
    {
        super(I18NUtil.getMessage(CannotApplyConstraintMetadataException.MSG_CANNOT_APPLY_CONSTRAINT, lovConstraint, propIdAsString, dataType));
    }
}
