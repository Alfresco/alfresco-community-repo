package org.alfresco.repo.download.cannedquery;

import java.util.Date;

/**
 * Query parameters for GetDownloadsCannedQuery
 * 
 * @author Alex Miller
 */
public class GetDownloadsCannedQueryParams extends DownloadEntity
{
    private Date before;

    public GetDownloadsCannedQueryParams(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId, Date before)
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
        this.before = before;
    }
    
    public Date getBefore()
    {
        return before;
    }
}
