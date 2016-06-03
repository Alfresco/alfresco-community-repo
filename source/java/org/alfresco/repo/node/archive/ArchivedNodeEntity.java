
package org.alfresco.repo.node.archive;

import org.alfresco.repo.query.NodeBackedEntity;

/**
 * ArchivedNodes Entity - used by GetArchivedNodes CQ
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class ArchivedNodeEntity extends NodeBackedEntity
{

    private String filter;
    private Long assocTypeQNameId;
    private Boolean sortOrderAscending;
    private Boolean filterIgnoreCase;

    public ArchivedNodeEntity()
    {
        super();
    }

    public ArchivedNodeEntity(Long parentNodeId, Long nameQNameId, String filter,
                Long assocTypeQNameId, Boolean sortOrderAscending, Boolean filterIgnoreCase)
    {
        super(parentNodeId, nameQNameId, null);
        setFilter(filter);
        setSortOrderAscending(sortOrderAscending);
        setFilterIgnoreCase(filterIgnoreCase);
        this.assocTypeQNameId = assocTypeQNameId;
    }

    public String getFilter()
    {
        return this.filter;
    }

    public Boolean getSortOrderAscending()
    {
        return this.sortOrderAscending;
    }

    public void setSortOrderAscending(Boolean sortOrderAscending)
    {
        // set this.sortOrderAscending to false when sortOrderAscending is null.
        this.sortOrderAscending = Boolean.TRUE.equals(sortOrderAscending);
    }

    public void setFilter(String filter)
    {
        if (filter != null)
        {
            // escape the '%' character with '\' (standard SQL escape character). e.g. 'test%' will be 'test\%'
            // note: you have to write 4 backslashes each time you want one '\' in a regex.
            filter = filter.replaceAll("%", "\\\\%");

            // replace the wildcard character '*' with the one used in database queries i.e. '%'
            this.filter = filter.replace('*', '%');
        }
    }

    public Long getAssocTypeQNameId()
    {
        return this.assocTypeQNameId;
    }

    public void setAssocTypeQNameId(Long assocTypeQNameId)
    {
        this.assocTypeQNameId = assocTypeQNameId;
    }

    public Boolean getFilterIgnoreCase()
    {
        return this.filterIgnoreCase;
    }

    public void setFilterIgnoreCase(Boolean filterIgnoreCase)
    {
        // set this.filterIgnoreCase to false when filterIgnoreCase is null.
        this.filterIgnoreCase = Boolean.TRUE.equals(filterIgnoreCase);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(300);
        builder.append("ArchivedNodeEntity [filter=").append(this.filter)
                    .append(", assocTypeQNameId=").append(this.assocTypeQNameId)
                    .append(", sortOrderAscending=").append(this.sortOrderAscending)
                    .append(", filterIgnoreCase=").append(this.filterIgnoreCase).append("]");
        return builder.toString();
    }
}
