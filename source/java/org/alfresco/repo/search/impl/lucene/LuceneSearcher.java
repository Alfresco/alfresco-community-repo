package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.util.Pair;

/**
 * Lucene implementation specific entension to the seracher API
 * @author andyh
 *
 */
public interface LuceneSearcher extends SearchService
{
    /**
     * Check if the index exists 
     * @return - true if it exists
     */
   public boolean indexExists();
   /**
    * Ste the node service
    * @param nodeService NodeService
    */
   public void setNodeService(NodeService nodeService);
   /**
    * Set the name space service
    * @param namespacePrefixResolver NamespacePrefixResolver
    */
   public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver);
   
   /**
    * Get top terms
    * 
    * @param field String
    * @param count int
    * @return List
    */
   public List<Pair<String, Integer>> getTopTerms(String field, int count);
   
   /**
    * Get a lucene searcher 
    * @return ClosingIndexSearcher
    */
   public ClosingIndexSearcher getClosingIndexSearcher();
}
