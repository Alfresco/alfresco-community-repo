package org.alfresco.repo.search.impl.solr;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author Andy
 *
 */
public class NoIndexQueryLanguage extends AbstractLuceneQueryLanguage
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI#executeQuery(org.alfresco.service.cmr.search.SearchParameters, org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
       throw new AlfrescoRuntimeException("There is no index to execute the query");
    }

}
