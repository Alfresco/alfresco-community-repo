
package org.alfresco.repo.forms.processor.workflow;

import org.alfresco.service.namespace.QName;

/**
 * Data transfer object that represents a data key.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class DataKeyInfo
{
    private final String fieldName;
    private final QName qName;
    private final FieldType fieldType;
    private final boolean isAdd;
    
    private DataKeyInfo(String dataKey, QName qName, FieldType fieldType, boolean isAdd)
    {
        this.fieldName = dataKey;
        this.qName = qName;
        this.fieldType = fieldType;
        this.isAdd = isAdd;
    }
    
    public static DataKeyInfo makeAssociationDataKeyInfo(String dataKey, QName qName, boolean isAdd)
    {
        return new DataKeyInfo(dataKey, qName, FieldType.ASSOCIATION, isAdd);
    }
    
    public static DataKeyInfo makePropertyDataKeyInfo(String dataKey, QName qName)
    {
        return new DataKeyInfo(dataKey, qName, FieldType.PROPERTY, true);
    }
    
    public static DataKeyInfo makeTransientPropertyDataKeyInfo(String dataKey)
    {
        return new DataKeyInfo(dataKey, null, FieldType.TRANSIENT_PROPERTY, true);
    }
    
    public static DataKeyInfo makeTransientAssociationDataKeyInfo(String dataKey, boolean isAdd)
    {
        return new DataKeyInfo(dataKey, null, FieldType.TRANSIENT_ASSOCIATION, isAdd);
    }
    
    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @return the qName
     */
    public QName getQName()
    {
        return qName;
    }
    

    /**
     * @return the fieldType
     */
    public FieldType getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @return the isAdd
     */
    public boolean isAdd()
    {
        return isAdd;
    }

}
