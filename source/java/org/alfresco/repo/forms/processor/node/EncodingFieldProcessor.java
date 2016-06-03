
package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * {@link FieldProcessor} implementation representing the <code>encoding</code> transient field.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class EncodingFieldProcessor extends TransientFieldProcessor
{
    private static final Log logger = LogFactory.getLog(EncodingFieldProcessor.class);

    public static final String KEY = "encoding";
    
    private static final String MSG_ENCODING_LABEL = "form_service.encoding.label";
    private static final String MSG_ENCODING_DESC = "form_service.encoding.description";

    @Override
    protected Log getLogger() 
    {
        return logger;
    }

    @Override
    protected FieldDefinition makeTransientFieldDefinition() 
    {
        String dataKeyName = PROP_DATA_PREFIX + KEY;
        PropertyFieldDefinition encodingField = new PropertyFieldDefinition(KEY, 
                    DataTypeDefinition.TEXT.getLocalName());
        encodingField.setLabel(I18NUtil.getMessage(MSG_ENCODING_LABEL));
        encodingField.setDescription(I18NUtil.getMessage(MSG_ENCODING_DESC));
        encodingField.setDataKeyName(dataKeyName);
        return encodingField;
    }

    @Override
    protected String getRegistryKey() 
    {
        return KEY;
    }
}