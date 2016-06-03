
package org.alfresco.repo.forms;

/**
 * Interface defining a field in a {@link Form}.
 *
 * @since 3.4
 * @author Nick Smith
 */
public interface Field
{
    /**
     * @return the field definition
     */
    FieldDefinition getFieldDefinition();

    /**
     * @return the fieldName
     */
    String getFieldName();

    /**
     * @return the value for this field or <code>null</code>.
     */
    Object getValue();
}