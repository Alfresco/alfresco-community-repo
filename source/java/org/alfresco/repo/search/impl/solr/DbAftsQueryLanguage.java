package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.admin.patch.OptionalPatchApplicationCheckBootstrapBean;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;

/**
 * @author Andy
 *
 */
public class DbAftsQueryLanguage  extends AbstractAlfrescoFtsQueryLanguage
{
    NamespaceService namespaceService;
    
    DictionaryService dictionaryService;
    
    OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck1;
    
    OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck2;

    /**
     * @param metadataIndexCheck1 the metadataIndexCheck1 to set
     */
    public void setMetadataIndexCheck1(OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck1)
    {
        this.metadataIndexCheck1 = metadataIndexCheck1;
    }

    /**
     * @param metadataIndexCheck2 the metadataIndexCheck2 to set
     */
    public void setMetadataIndexCheck2(OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck2)
    {
        this.metadataIndexCheck2 = metadataIndexCheck2;
    }

    /**
     ** @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage#getNamespacePrefixResolver(org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    protected NamespacePrefixResolver getNamespacePrefixResolver(ADMLuceneSearcherImpl admLuceneSearcher)
    {
        return namespaceService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.AbstractAlfrescoFtsQueryLanguage#getDictionaryService(org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    protected DictionaryService getDictionaryService(ADMLuceneSearcherImpl admLuceneSearcher)
    {
        return dictionaryService;
    }

    public DbAftsQueryLanguage()
    {
        this.setName("db-afts");
    }

    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        if(metadataIndexCheck1.getPatchApplied())
        {
            return super.executeQuery(searchParameters, admLuceneSearcher);
        }
        else
        {
            throw new QueryModelException("The patch to add the indexes to support in-transactional metadata queries has not been applied");
        }
    }
}
