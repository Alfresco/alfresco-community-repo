/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.search.impl.solr.facet.handler;

/**
 * A class to encapsulate the result of the facet label display handler.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class FacetLabel
{
    private final String value;
    private final String label;
    private final int labelIndex;

    /**
     * @param value
     * @param label
     * @param labelIndex
     */
    public FacetLabel(String value, String label, int labelIndex)
    {
        this.value = value;
        this.label = label;
        this.labelIndex = labelIndex;
    }

    /**
     * Gets the original facet value or a new modified value
     * 
     * @return the original facet value or a new modified value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Gets the facet display label
     * 
     * @return the label
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Gets the label index to be used for sorting. The index only relevant to
     * to Date and Size facets.
     * 
     * @return the index or -1, if it isn't relevant to the facet label
     */
    public int getLabelIndex()
    {
        return this.labelIndex;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
        result = prime * result + this.labelIndex;
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FacetLabel))
            return false;
        FacetLabel other = (FacetLabel) obj;
        if (this.label == null)
        {
            if (other.label != null)
                return false;
        }
        else if (!this.label.equals(other.label))
            return false;
        if (this.labelIndex != other.labelIndex)
            return false;
        if (this.value == null)
        {
            if (other.value != null)
                return false;
        }
        else if (!this.value.equals(other.value))
            return false;
        return true;
    }
}