package org.alfresco.repo.search.impl.solr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.lucene.LuceneCategoryServiceImpl;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.LuceneSearcher;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * @author Andy
 *
 */
public class SolrCategoryServiceImpl extends LuceneCategoryServiceImpl
{

    @Override
    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        AspectDefinition definition = dictionaryService.getAspect(aspectName);
        if(definition == null)
        {
            throw new IllegalStateException("Unknown aspect");
        }
        QName catProperty = null;
        Map<QName, PropertyDefinition> properties = definition.getProperties();
        for(QName pName : properties.keySet())
        {
            if(pName.getNamespaceURI().equals(aspectName.getNamespaceURI()))
            {
                if(pName.getLocalName().equalsIgnoreCase(aspectName.getLocalName()))
                {
                    PropertyDefinition def = properties.get(pName);
                    if(def.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                    {
                        catProperty = pName;
                    }
                }
            }
        }
        if(catProperty == null)
        {
            throw new IllegalStateException("Aspect does not have category property mirroring the aspect name");
        }
        
        String field = "@" + catProperty;
        
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO);
        sp.addStore(storeRef);
        sp.setQuery(catProperty+":*");
        FieldFacet ff = new FieldFacet(field);
        ff.setLimitOrNull(count);
        sp.addFieldFacet(ff);
        
        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(sp);
            List<Pair<String, Integer>> facetCounts = resultSet.getFieldFacet(field);
            List<Pair<NodeRef, Integer>> answer = new LinkedList<Pair<NodeRef, Integer>>();
            for (Pair<String, Integer> term : facetCounts)
            {
                Pair<NodeRef, Integer> toAdd;
                NodeRef nodeRef = new NodeRef(term.getFirst());
                if (nodeService.exists(nodeRef))
                {
                    toAdd = new Pair<NodeRef, Integer>(nodeRef, term.getSecond());
                }
                else
                {
                    toAdd = new Pair<NodeRef, Integer>(null, term.getSecond());
                }
                answer.add(toAdd);
            }
            return answer;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        
    }

}
