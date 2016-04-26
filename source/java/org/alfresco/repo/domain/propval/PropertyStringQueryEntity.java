package org.alfresco.repo.domain.propval;

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.Pair;

/**
 * Entity bean for querying against the <b>alf_prop_string_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyStringQueryEntity
{
    private final Short persistedType;
    private final Long actualTypeId;
    private final String stringValue;
    private final String stringEndLower;
    private final Long stringCrc;
    
    public PropertyStringQueryEntity(Short persistedType, Long actualTypeId, String value)
    {
        this.persistedType = persistedType;
        this.actualTypeId = actualTypeId;
        
        stringValue = value;
        // Calculate the crc value from the original value
        Pair<String, Long> crcPair = CrcHelper.getStringCrcPair(value, 16, false, true);
        stringEndLower = crcPair.getFirst();
        stringCrc = crcPair.getSecond();
    }
    
    public Short getPersistedType()
    {
        return persistedType;
    }

    public Long getActualTypeId()
    {
        return actualTypeId;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public String getStringEndLower()
    {
        return stringEndLower;
    }

    public Long getStringCrc()
    {
        return stringCrc;
    }
}
