package org.alfresco.repo.domain.propval;

import java.util.List;

/**
 * Query parameters for searching <b>alf_prop_link</b> by IDs.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyIdQueryParameter
{
    private List<Long> rootPropIds;
    
    public PropertyIdQueryParameter()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyIdQueryParameter")
          .append(", rootPropIds=").append(rootPropIds)
          .append("]");
        return sb.toString();
    }

    public List<Long> getRootPropIds()
    {
        return rootPropIds;
    }

    public void setRootPropIds(List<Long> rootPropIds)
    {
        this.rootPropIds = rootPropIds;
    }
}
