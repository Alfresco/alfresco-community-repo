
package org.alfresco.repo.search.impl.solr.facet;

import java.util.List;
import java.util.Map;

/**
 * A contract for classes which need to create a set of predefined facet queries.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface FacetQueryProvider
{

    /**
     * Gets the created facet queries
     * 
     * @return read-only map of facet queries or an empty map.
     */
    public Map<String, List<String>> getFacetQueries();
}
