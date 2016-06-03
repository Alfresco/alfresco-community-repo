
package org.alfresco.repo.workflow;

import java.io.Serializable;

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class DefaultWorkflowPropertyHandler extends AbstractWorkflowPropertyHandler
{
    /**
    * {@inheritDoc}
    */
    public Object handleProperty(QName key, Serializable value, TypeDefinition type, Object object, Class<?> objectType)
    {
        return handleDefaultProperty(object, type, key, value);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected QName getKey()
    {
        // Does not have a key!
        return null;
    }
}
