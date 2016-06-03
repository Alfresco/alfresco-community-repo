
package org.alfresco.repo.node.archive;

/**
 * Parameter object for {@link GetArchivedNodesCannedQuery}.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class GetArchivedNodesCannedQueryParams extends ArchivedNodeEntity
{

    private int limit;

    /**
     *
     * @param parentNodeId Long
     * @param assocTypeQNameId Long
     * @param filter String
     * @param filterIgnoreCase Boolean
     * @param nameQNameId Long
     * @param sortOrderAscending Boolean
     */
    public GetArchivedNodesCannedQueryParams(Long parentNodeId, Long assocTypeQNameId,
                String filter, Boolean filterIgnoreCase, Long nameQNameId,
                Boolean sortOrderAscending)
    {
        super(parentNodeId, nameQNameId, filter, assocTypeQNameId, sortOrderAscending,
                    filterIgnoreCase);
    }

    /**
     * @param parentNodeId
     * @param assocTypeQNameId
     * @param filter
     * @param filterIgnoreCase
     * @param nameQNameId
     * @param sortOrderAscending
     * @param limit
     */
    public GetArchivedNodesCannedQueryParams(Long parentNodeId, Long assocTypeQNameId,
            String filter, Boolean filterIgnoreCase, Long nameQNameId, Boolean sortOrderAscending,
            int limit)
    {
        this(parentNodeId, assocTypeQNameId, filter, filterIgnoreCase, nameQNameId,
                sortOrderAscending);
        this.setLimit(limit);
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

}
