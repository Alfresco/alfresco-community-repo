package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.opencmis.search.CMISResultSetMetaData;
import org.alfresco.opencmis.search.CMISResultSetRow;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.results.ResultSetSPIWrapper;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Support for Alfresco SQL in the search service
 * 
 * @author andyh
 * 
 */
public class LuceneOpenCMISStrictSqlQueryLanguage extends AbstractLuceneQueryLanguage
{
    private CMISQueryService cmisQueryService;

    public LuceneOpenCMISStrictSqlQueryLanguage()
    {
        this.setName(SearchService.LANGUAGE_CMIS_STRICT);
    }
    
    /**
     * Set the search service
     * 
     * @param cmisQueryService CMISQueryService
     */
    public void setCmisQueryService(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService = cmisQueryService;
    }

    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        CMISQueryOptions options = CMISQueryOptions.create(searchParameters);
        options.setQueryMode(CMISQueryMode.CMS_STRICT);
        return new ResultSetSPIWrapper<CMISResultSetRow, CMISResultSetMetaData>(cmisQueryService.query(options));
    }
}
