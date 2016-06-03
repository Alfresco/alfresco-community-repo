package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Andy
 *
 */
public abstract class AbstractLuceneQueryLanguage implements LuceneQueryLanguageSPI, InitializingBean
{
    private String name;
    
    private List<IndexerAndSearcher> factories;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI#setFactories(java.util.List)
     */
    @Override
    final public void setFactories(List<IndexerAndSearcher> factories)
    {
        this.factories = factories;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        for (IndexerAndSearcher factory : factories)
        {
            factory.registerQueryLanguage(this);
        }
        
    }

    public final String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<IndexerAndSearcher> getFactories()
    {
        return factories;
    }
    
    

}
