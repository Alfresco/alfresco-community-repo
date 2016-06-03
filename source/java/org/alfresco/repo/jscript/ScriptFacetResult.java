package org.alfresco.repo.jscript;

import java.io.Serializable;

/**
 * Scriptable facet. Specific for use by Search script as part of the object model.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class ScriptFacetResult implements Serializable
{
    private static final long serialVersionUID = -6948514033689491531L;

    private final String facetValue;
    private final String facetLabel;
    private final int facetLabelIndex;
    private final int hits;

    /**
     * @param facetValue the facet value. e.g. the content creator's userID
     * @param facetLabel the display name of the {@code facetValue}. e.g. jdoe => John Doe
     * @param facetLabelIndex the label index to be used for sorting (Optional).The default value is -1
     * @param hits the number of hits
     */
    public ScriptFacetResult(String facetValue, String facetLabel, int facetLabelIndex, int hits)
    {
        this.facetValue = facetValue;
        this.facetLabel = facetLabel;
        this.facetLabelIndex = facetLabelIndex;
        this.hits = hits;
    }

    /**
     * @return the facetValue
     */
    public String getFacetValue()
    {
        return this.facetValue;
    }

    /**
     * @return the facetLabel
     */
    public String getFacetLabel()
    {
        return this.facetLabel;
    }

    /**
     * @return the facetLabelIndex
     */
    public int getFacetLabelIndex()
    {
        return this.facetLabelIndex;
    }

    /**
     * @return the hits
     */
    public int getHits()
    {
        return this.hits;
    }
}
