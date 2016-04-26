package org.alfresco.repo.domain.propval;

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_prop_string_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyStringValueEntity
{
    public static final String EMPTY_STRING = "";
    public static final String EMPTY_STRING_REPLACEMENT = ".empty";
    
    private Long id;
    private String stringValue;
    private String stringEndLower;
    private Long stringCrc;
    
    public PropertyStringValueEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (stringValue == null ? 0 : stringValue.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj != null && obj instanceof PropertyStringValueEntity)
        {
            PropertyStringValueEntity that = (PropertyStringValueEntity) obj;
            return EqualsHelper.nullSafeEquals(this.stringValue, that.stringValue);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyStringValueEntity")
          .append("[ ID=").append(id)
          .append(", stringValue=").append(stringValue)
          .append("]");
        return sb.toString();
    }
    
    public Pair<Long, String> getEntityPair()
    {
        if (stringValue != null && stringValue.equals(PropertyStringValueEntity.EMPTY_STRING_REPLACEMENT))
        {
            return new Pair<Long, String>(id, PropertyStringValueEntity.EMPTY_STRING);
        }
        else
        {
            return new Pair<Long, String>(id, stringValue);
        }
    }
    
    /**
     * Set the string and string-end values
     */
    public void setValue(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Null strings cannot be persisted");
        }
        if (value != null && value.equals(PropertyStringValueEntity.EMPTY_STRING))
        {
            // Oracle: We can't insert empty strings into the column.
            value = PropertyStringValueEntity.EMPTY_STRING_REPLACEMENT;
        }
        stringValue = value;
        // Calculate the crc value from the original value
        Pair<String, Long> crcPair = CrcHelper.getStringCrcPair(value, 16, false, true);
        stringEndLower = crcPair.getFirst();
        stringCrc = crcPair.getSecond();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }
    
    public String getStringEndLower()
    {
        return stringEndLower;
    }

    public void setStringEndLower(String stringEndLower)
    {
        this.stringEndLower = stringEndLower;
    }

    public Long getStringCrc()
    {
        return stringCrc;
    }

    public void setStringCrc(Long stringCrc)
    {
        this.stringCrc = stringCrc;
    }
}
