
package org.alfresco.repo.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;

/**
 * A generic validator to validate string value against the target data type.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public interface ValueDataTypeValidator
{
    /**
     * Validates the given {@code value} against the given {@code dataType}.
     * <p>The supplied {@code dataType} must be a valid {@link QName} prefixed
     * string so it can be resolved into a fully qualified name data type
     * registered in the {@link DataTypeDefinition}.
     * 
     * @param dataType the target prefixed data type (e.g. {@code d:int}) which
     *            the string value should be converted to
     * @param value non-empty string value
     * @throws InvalidQNameException if the <tt>dataType</tt> is not a valid
     *             {@link QName} prefixed string (<b>{@code prefix:type} </b>)
     * @throws NamespaceException if the prefix of the given <tt>dataType</tt>
     *             is not mapped to a namespace URI
     * @throws AlfrescoRuntimeException if the given value cannot be converted
     *             into the given data type, or the data type is unknown
     */
    public void validateValue(String dataType, String value) throws InvalidQNameException, NamespaceException, AlfrescoRuntimeException;
}
