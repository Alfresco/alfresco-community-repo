package org.alfresco.repo.action.parameter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Message parameter processor.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class MessageParameterProcessor extends ParameterProcessor
{
    /**
     * @see org.alfresco.repo.action.parameter.ParameterProcessor#process(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String process(String value, NodeRef actionedUponNodeRef)
    {
        // the default position is to return the value un-changed
        String result = value;

        // strip the processor name from the value
        value = stripName(value);
        if (!value.isEmpty())
        {
            result = I18NUtil.getMessage(value);
            if (result == null)
            {
                throw new AlfrescoRuntimeException("The message parameter processor could not resolve the message for the id " + value);
            }
        }

        return result;
    }
}
