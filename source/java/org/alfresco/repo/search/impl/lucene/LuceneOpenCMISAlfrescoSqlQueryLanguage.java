package org.alfresco.repo.search.impl.lucene;

import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.opencmis.search.CMISResultSetMetaData;
import org.alfresco.opencmis.search.CMISResultSetRow;
import org.alfresco.repo.search.results.ResultSetSPIWrapper;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Support for sql-cmis-strict in the search service
 * 
 * @author andyh
 * 
 */
public class LuceneOpenCMISAlfrescoSqlQueryLanguage extends AbstractLuceneQueryLanguage
{
    private CMISQueryService cmisQueryService;

    public LuceneOpenCMISAlfrescoSqlQueryLanguage()
    {
        this.setName("index.cmis.alfresco");
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
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        return new ResultSetSPIWrapper<CMISResultSetRow, CMISResultSetMetaData>(cmisQueryService.query(options));
    }
}
