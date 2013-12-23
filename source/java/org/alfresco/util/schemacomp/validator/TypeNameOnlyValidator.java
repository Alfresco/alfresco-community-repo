package org.alfresco.util.schemacomp.validator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Column;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Validates columns types without column size.
 * 
 * @author sergei.shcherbovich
 */
public class TypeNameOnlyValidator extends NameValidator
{
    private static String TYPE_SIZE_SPLITTER = "(";

    @Override
    public void validate(DbObject reference, DbObject target, DiffContext ctx)
    {
        if (!(target instanceof Column))
        {
            throw new AlfrescoRuntimeException("TypeNameOnlyValidator could be used only in context of column object but was: " + target.toString());
        }
        
        String referenceTypeName = ((Column)reference).getType();
        String targetTypeName = ((Column)target).getType();
        
        if (referenceTypeName.contains(TYPE_SIZE_SPLITTER))
        {
            referenceTypeName = referenceTypeName.substring(0, referenceTypeName.indexOf(TYPE_SIZE_SPLITTER));
        }
        if (targetTypeName.contains(TYPE_SIZE_SPLITTER))
        {
            targetTypeName = targetTypeName.substring(0, targetTypeName.indexOf(TYPE_SIZE_SPLITTER));
        }
        
        if (!referenceTypeName.equals(targetTypeName))
        {
            String message = I18NUtil.getMessage("system.schema_comp.column_names_validator", targetTypeName, referenceTypeName);
            ValidationResult result = new ValidationResult(new DbProperty(target, "type"), message);
            ctx.getComparisonResults().add(result);
        }
    }
    
    @Override
    public boolean validatesFullObject()
    {
        return true;
    }
    
}
