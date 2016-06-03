
package org.alfresco.repo.search.impl.solr.facet.handler;


/**
 * An interface for Solr facet value and facet query result display label handler.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface FacetLabelDisplayHandler
{
    /**
     * Gets the user friendly display label for the returned facet value
     * 
     * @param value the facet value
     * @return user friendly display label or the original value, if there is no result
     */
    public FacetLabel getDisplayLabel(String value);
}
