package org.alfresco.util.schemacomp.validator;

import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Schema;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Validate a schema's version number with respect to a reference schema.
 * <p/>
 * For the target schema to pass validation its version number must be
 * greater than or equal to the reference schema's version number.
 * 
 * @author Matt Ward
 */
public class SchemaVersionValidator extends AbstractDbValidator
{
    public SchemaVersionValidator()
    {
        addFieldToValidate("version");
    }
    
    @Override
    public void validate(DbObject referenceObj, DbObject targetObj, DiffContext ctx)
    {
        Schema reference = (Schema) referenceObj;
        Schema target = (Schema) targetObj;
        
        if (target.getVersion() < reference.getVersion())
        {
            DbProperty targetProperty = new DbProperty(target, "version");
            String message = I18NUtil.getMessage(
                        "system.schema_comp.schema_version_validator",
                        reference.getVersion());
            ctx.getComparisonResults().add(new ValidationResult(targetProperty, message));
        }
    }
}
