
package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry which holds and provides the appropriate display handler for a
 * particular facet field.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class FacetLabelDisplayHandlerRegistry
{
    private final ConcurrentMap<String, FacetLabelDisplayHandler> registry;

    public FacetLabelDisplayHandlerRegistry()
    {
        this.registry = new ConcurrentHashMap<String, FacetLabelDisplayHandler>();
    }

    /**
     * Register an instance of {@code FacetLabelDisplayHandler} with the
     * specified field facet.
     * 
     * @param fieldFacet the field facet
     * @param displayHandler the display handler
     */
    public void addDisplayHandler(String fieldFacet, FacetLabelDisplayHandler displayHandler)
    {
        registry.putIfAbsent(fieldFacet, displayHandler);
    }

    /**
     * Gets the display handler.
     * 
     * @param fieldFacet the field facet to perform the lookup
     * @return the display handler or null if none found
     */
    public FacetLabelDisplayHandler getDisplayHandler(String fieldFacet)
    {
        return registry.get(fieldFacet);
    }
}
