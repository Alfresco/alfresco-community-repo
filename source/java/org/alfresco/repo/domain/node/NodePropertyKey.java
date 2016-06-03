package org.alfresco.repo.domain.node;

import java.io.Serializable;

import org.alfresco.util.EqualsHelper;

/**
 * Compound key for persistence of {@link org.alfresco.repo.domain.node.Node}
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodePropertyKey implements Serializable, Comparable<NodePropertyKey>
{
    private static final long serialVersionUID = 3258695403221300023L;
    
    private Long qnameId;
    private Long localeId;
    private Integer listIndex;
    
    public NodePropertyKey()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("NodePropertyKey ")
          .append(" [listIndex=").append(listIndex)
          .append(", localeId=").append(localeId)
          .append(", qnameId=").append(qnameId)
          .append("]");
        return sb.toString();
    }

    public int hashCode()
    {
        return
                (qnameId == null ? 0 : qnameId.hashCode()) +
                (listIndex == null ? 0 : listIndex.hashCode());
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof NodePropertyKey))
        {
            return false;
        }
        // Compare in order of selectivity
        NodePropertyKey that = (NodePropertyKey) obj;
        return (EqualsHelper.nullSafeEquals(this.qnameId, that.qnameId) &&
                EqualsHelper.nullSafeEquals(this.listIndex, that.listIndex) &&
                EqualsHelper.nullSafeEquals(this.localeId, that.localeId)
                );
    }

    /**
     * throws ClassCastException        if the object is not of the correct type
     */
    public int compareTo(NodePropertyKey that)
    {
        // Comparision by priority: qnameId, listIndex, localeId, nodeId
        int compare = this.qnameId.compareTo(that.qnameId);
        if (compare != 0)
        {
            return compare;
        }
        compare = this.listIndex.compareTo(that.listIndex);
        if (compare != 0)
        {
            return compare;
        }
        return this.localeId.compareTo(that.localeId);
    }

    public Long getQnameId()
    {
        return qnameId;
    }

    public void setQnameId(Long qnameId)
    {
        this.qnameId = qnameId;
    }

    public Long getLocaleId()
    {
        return localeId;
    }

    public void setLocaleId(Long localeId)
    {
        this.localeId = localeId;
    }

    public Integer getListIndex()
    {
        return listIndex;
    }

    public void setListIndex(Integer listIndex)
    {
        this.listIndex = listIndex;
    }
}
