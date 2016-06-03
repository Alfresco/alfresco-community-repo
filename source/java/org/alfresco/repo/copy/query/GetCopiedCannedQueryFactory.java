package org.alfresco.repo.copy.query;

import java.util.List;

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CopyService.CopyInfo;

/**
 * Factory producing queries for the {@link CopyService}
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class GetCopiedCannedQueryFactory extends AbstractCopyCannedQueryFactory<CopyInfo>
{
    @Override
    public CannedQuery<CopyInfo> getCannedQuery(CannedQueryParameters parameters)
    {
        throw new UnsupportedOperationException();
    }
    
    private class GetCopiedCannedQuery extends AbstractCannedQuery<CopyInfo>
    {
        private GetCopiedCannedQuery(CannedQueryParameters parameters)
        {
            super(parameters);
        }
        
        @Override
        protected List<CopyInfo> queryAndFilter(CannedQueryParameters parameters)
        {
            throw new UnsupportedOperationException();
        }
    }
}