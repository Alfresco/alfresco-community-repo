package org.alfresco.repo.domain.propval;

import java.io.Serializable;

/**
 * Entity bean for <b>alf_prop_unique_ctx</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyUniqueContextEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private short version;
    private Long value1PropId;
    private Long value2PropId;
    private Long value3PropId;
    private Long propertyId;
    
    public PropertyUniqueContextEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyRootEntity")
          .append("[ ID=").append(id)
          .append(", version=").append(version)
          .append(", value1PropId=").append(value1PropId)
          .append(", value2PropId=").append(value2PropId)
          .append(", value3PropId=").append(value3PropId)
          .append(", propertyId=").append(propertyId)
          .append("]");
        return sb.toString();
    }

    public void incrementVersion()
    {
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0;
        }
        else
        {
            this.version++;
        }
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public short getVersion()
    {
        return version;
    }

    public void setVersion(short version)
    {
        this.version = version;
    }

    public Long getValue1PropId()
    {
        return value1PropId;
    }

    public void setValue1PropId(Long value1PropId)
    {
        this.value1PropId = value1PropId;
    }

    public Long getValue2PropId()
    {
        return value2PropId;
    }

    public void setValue2PropId(Long value2PropId)
    {
        this.value2PropId = value2PropId;
    }

    public Long getValue3PropId()
    {
        return value3PropId;
    }

    public void setValue3PropId(Long value3PropId)
    {
        this.value3PropId = value3PropId;
    }

    public Long getPropertyId()
    {
        return propertyId;
    }

    public void setPropertyId(Long propId)
    {
        this.propertyId = propId;
    }
}
