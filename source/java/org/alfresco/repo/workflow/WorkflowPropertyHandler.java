
package org.alfresco.repo.workflow;

import java.io.Serializable;

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public interface WorkflowPropertyHandler
{
    static final String DO_NOT_ADD = "*£$DO NOT ADD THIS PROPERTY $£*";
    
    Object handleProperty(QName key, Serializable value, TypeDefinition type, Object object, Class<?> objectType);
}
