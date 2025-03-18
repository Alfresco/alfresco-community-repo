/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
     * @param facetValue
     *            the facet value. e.g. the content creator's userID
     * @param facetLabel
     *            the display name of the {@code facetValue}. e.g. jdoe => John Doe
     * @param facetLabelIndex
     *            the label index to be used for sorting (Optional).The default value is -1
     * @param hits
     *            the number of hits
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
