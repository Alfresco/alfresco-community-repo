
package org.alfresco.repo.forms.processor.node;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.processor.AbstractFieldProcessor;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.FormCreationData;

/**
 * Abstract base class for all transient {@link FieldProcessor}s.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public abstract class TransientFieldProcessor extends AbstractFieldProcessor<TransientValueGetter>
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.AbstractFieldProcessor#generateTypedField(java.lang.String, org.alfresco.repo.forms.processor.node.FormCreationData, java.lang.Object)
     */
    @Override
    protected Field generateTypedField(String fieldName, FormCreationData formData, TransientValueGetter typedData)
    {
        FieldDefinition transientPropDef = makeTransientFieldDefinition();
        Field fieldInfo = null;
        Object value = getValue(fieldName, typedData);
        if (transientPropDef != null)
        {
            fieldInfo = new ContentModelField(transientPropDef, value);
        }
        return fieldInfo;
    }

    protected Object getValue(String fieldName, TransientValueGetter data)
    {
        return data.getTransientValue(fieldName);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.forms.field.processor.AbstractFieldProcessor#
     * getExpectedDataType()
     */
    @Override
    protected Class<TransientValueGetter> getExpectedDataType()
    {
        return TransientValueGetter.class;
    }

    protected abstract FieldDefinition makeTransientFieldDefinition();
}