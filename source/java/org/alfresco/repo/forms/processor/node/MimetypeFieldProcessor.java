
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
 * {@link FieldProcessor} implementation representing the <code>mimetype</code> transient field.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class MimetypeFieldProcessor extends TransientFieldProcessor
{
    private static final Log logger = LogFactory.getLog(MimetypeFieldProcessor.class);

    public static final String KEY = "mimetype";

    private static final String MSG_MIMETYPE_LABEL = "form_service.mimetype.label";
    private static final String MSG_MIMETYPE_DESC = "form_service.mimetype.description";

    @Override
    protected Log getLogger() 
    {
        return logger;
    }

    @Override
    protected FieldDefinition makeTransientFieldDefinition() 
    {
        String dataKeyName = PROP_DATA_PREFIX + KEY;
        PropertyFieldDefinition mimetypeField = new PropertyFieldDefinition(KEY, DataTypeDefinition.TEXT
                    .getLocalName());
        mimetypeField.setLabel(I18NUtil.getMessage(MSG_MIMETYPE_LABEL));
        mimetypeField.setDescription(I18NUtil.getMessage(MSG_MIMETYPE_DESC));
        mimetypeField.setDataKeyName(dataKeyName);
        return mimetypeField;
    }

    @Override
    protected String getRegistryKey() 
    {
        return KEY;
    }
}
