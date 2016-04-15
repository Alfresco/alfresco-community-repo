package org.alfresco.repo.domain.propval;

import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_prop_numeric_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyDoubleValueEntity
{
    private Long id;
    private Double doubleValue;
    
    public PropertyDoubleValueEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (doubleValue == null ? 0 : doubleValue.hashCode()); 
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj != null && obj instanceof PropertyDoubleValueEntity)
        {
            PropertyDoubleValueEntity that = (PropertyDoubleValueEntity) obj;
            return EqualsHelper.nullSafeEquals(this.doubleValue, that.doubleValue);
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
        sb.append("PropertyNumericValueEntity")
          .append("[ ID=").append(id)
          .append(", value=").append(doubleValue)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-value pair
     */
    public Pair<Long, Double> getEntityPair()
    {
        return new Pair<Long, Double>(id, doubleValue);
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public double getDoubleValue()
    {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue)
    {
        this.doubleValue = doubleValue;
    }
}
