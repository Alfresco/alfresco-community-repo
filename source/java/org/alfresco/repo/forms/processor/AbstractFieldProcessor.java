
package org.alfresco.repo.forms.processor;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FormException;
import org.apache.commons.logging.Log;

/**
 * Abstract base class for all field processors.
 * 
 * @since 3.4
 * @author Nick Smith
 *
 * @param <Data>
 */
public abstract class AbstractFieldProcessor<Data> implements FieldProcessor
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.field.processor.FieldProcessor#generateField(java.lang.String, java.lang.Object)
     */
    public Field generateField(String fieldName, FormCreationData data)
    {
        Data typedData = checkDataType(data.getItemData());
        return generateTypedField(fieldName, data, typedData);
    }

    @SuppressWarnings("unchecked")
    private Data checkDataType(Object itemData)
    {
        Class<Data> expectedType = getExpectedDataType();
        if (expectedType.isAssignableFrom(itemData.getClass()))
        {
            return (Data)itemData;
        }
        else
        {
            String msg = "Data object: " + itemData + " is not of expected type: " + expectedType;
            throw new FormException(msg, new IllegalArgumentException());
        }
    }
    
    /**
     * Registers this {@link FieldProcessor} with the supplied registry.
     * 
     * @param registry The registry to which this {@link FieldProcessor} is added.
     */
    public void setFieldProcessorRegistry(FieldProcessorRegistry registry)
    {
        registry.register(getRegistryKey(), this);
    }

    /**
     * @return a {@link Log} used to log events that occur while processing fields.
     */
    protected abstract Log getLogger();
    
    /**
     * @return a {@link String} key used to identify this {@link FieldProcessor} in a {@link FieldProcessorRegistry}.
     */
    protected abstract String getRegistryKey();

    /**
     * @return the expected {@link Class} of the <code>data</code> parameter for the method <code>generateField</code>.
     */
    protected abstract Class<Data> getExpectedDataType();
    
    /**
     * The method which actually creates the {@link Field}.
     * @param fieldName the name of the {@link Field} to be genereated.
     * @param formData TODO
     * @param typedData the data used to create the field.
     * @return a {@link Field} or <code>null</code>.
     */
    protected abstract Field generateTypedField(String fieldName, FormCreationData formData, Data typedData);
}
