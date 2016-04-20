
package org.alfresco.repo.search.impl.solr.facet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.junit.Test;

/**Some Unit tests for {@link SolrFacetComparator}. */
public class SolrFacetComparatorTest
{
    @Test public void simpleSortOfSortedFacets() throws Exception
    {
        List<String> expectedIds = Arrays.asList(new String[] { "a", "b", "c", "d"});
        
        SolrFacetProperties.Builder builder = new SolrFacetProperties.Builder();
        
        List<SolrFacetProperties> facets = Arrays.asList(new SolrFacetProperties[]
                                                         {
                                                            builder.filterID("a").build(),
                                                            builder.filterID("d").build(),
                                                            builder.filterID("b").build(),
                                                            builder.filterID("c").build(),
                                                         });
        Collections.sort(facets, new SolrFacetComparator(expectedIds));
        
        assertEquals(expectedIds, toFacetIds(facets));
    }
    
    private List<String> toFacetIds(List<SolrFacetProperties> facets)
    {
        return CollectionUtils.transform(facets, new Function<SolrFacetProperties, String>()
                {
                    @Override public String apply(SolrFacetProperties value)
                    {
                        return value.getFilterID();
                    }
                });
    }
}
