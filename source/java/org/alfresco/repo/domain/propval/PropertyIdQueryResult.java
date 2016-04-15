package org.alfresco.repo.domain.propval;

import java.util.List;

/**
 * Entity bean for rolled up <b>alf_prop_link</b> results.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyIdQueryResult
{
    private Long propId;
    private List<PropertyIdSearchRow> propValues;
    
    public PropertyIdQueryResult()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyLinkQueryResult")
          .append("[ propId=").append(propId)
          .append(", propValues=").append(propValues.size())
          .append("]");
        return sb.toString();
    }

    public Long getPropId()
    {
        return propId;
    }

    public void setPropId(Long propId)
    {
        this.propId = propId;
    }

    public List<PropertyIdSearchRow> getPropValues()
    {
        return propValues;
    }

    public void setPropValues(List<PropertyIdSearchRow> propValues)
    {
        this.propValues = propValues;
    }
}
