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

package org.alfresco.repo.search.impl.solr.facet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Domain-Specific Language (DSL) style builder class for encapsulating the
 * facet properties.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetProperties
{
    private final String filterID;
    private final QName facetQName;
    private final String displayName;
    private final int maxFilters;
    private final int hitThreshold;
    private final int minFilterValueLength;
    private final String sortBy;
    private final String scope;
    private final Set<String> scopedSites;
    private final int index;
    private final boolean isEnabled;

    /**
     * Initialises a newly created <code>SolrFacetProperty</code> object
     *
     * @param builder the builder object
     */
    private SolrFacetProperties(Builder builder)
    {
        this.filterID = builder.filterID;
        this.facetQName = builder.facetQName;
        this.displayName = builder.displayName;
        this.maxFilters = builder.maxFilters;
        this.hitThreshold = builder.hitThreshold;
        this.minFilterValueLength = builder.minFilterValueLength;
        this.sortBy = builder.sortBy;
        this.scope = builder.scope;
        this.index = builder.index;
        this.isEnabled = builder.isEnabled;
        this.scopedSites = (builder.scopedSites == null) ? null :Collections.unmodifiableSet(new HashSet<String>(builder.scopedSites));
    }

    /**
     * @return the filterID
     */
    public String getFilterID()
    {
        return this.filterID;
    }

    /**
     * @return the facetQName
     */
    public QName getFacetQName()
    {
        return this.facetQName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName()
    {
        return this.displayName;
    }

    /**
     * @return the maxFilters
     */
    public int getMaxFilters()
    {
        return this.maxFilters;
    }

    /**
     * @return the hitThreshold
     */
    public int getHitThreshold()
    {
        return this.hitThreshold;
    }

    /**
     * @return the minFilterValueLength
     */
    public int getMinFilterValueLength()
    {
        return this.minFilterValueLength;
    }

    /**
     * @return the sortBy
     */
    public String getSortBy()
    {
        return this.sortBy;
    }

    /**
     * @return the scope
     */
    public String getScope()
    {
        return this.scope;
    }

    /**
     * Returns an unmodifiable view of the Scoped Sites set or null
     *
     * @return the scopedSites
     */
    public Set<String> getScopedSites()
    {
        if (this.scopedSites == null)
        {
            return null;
        }
        return Collections.unmodifiableSet(new HashSet<String>(this.scopedSites));
    }

    /**
     * @return the index
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     * @return the isEnabled
     */
    public boolean isEnabled()
    {
        return this.isEnabled;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.filterID == null) ? 0 : this.filterID.hashCode());
        return result;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof SolrFacetProperties))
        {
            return false;
        }
        SolrFacetProperties other = (SolrFacetProperties) obj;
        if (this.filterID == null)
        {
            if (other.filterID != null)
            {
                return false;
            }
        }
        else if (!this.filterID.equals(other.filterID))
        {
            return false;
        }
        return true;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder2 = new StringBuilder(270);
        builder2.append("FacetProperty [filterID=").append(this.filterID).append(", facetQName=")
                    .append(this.facetQName).append(", displayName=").append(this.displayName).append(", maxFilters=")
                    .append(this.maxFilters).append(", hitThreshold=").append(this.hitThreshold)
                    .append(", minFilterValueLength=").append(this.minFilterValueLength).append(", sortBy=")
                    .append(this.sortBy).append(", scope=").append(this.scope).append(", scopedSites=")
                    .append(this.scopedSites).append(", index=").append(this.index).append(", isEnabled=").append(this.isEnabled)
                    .append("]");
        return builder2.toString();
    }

    public static class Builder
    {
        private String filterID;
        private QName facetQName;
        private String displayName;
        private int maxFilters;
        private int hitThreshold;
        private int minFilterValueLength;
        private String sortBy;
        private String scope;
        private Set<String> scopedSites;
        private int index;
        private boolean isEnabled;

        public Builder filterID(String filterID)
        {
            this.filterID = filterID;
            return this;
        }

        public Builder facetQName(QName facetQName)
        {
            this.facetQName = facetQName;
            return this;
        }

        public Builder displayName(String displayName)
        {
            this.displayName = displayName;
            return this;
        }

        public Builder maxFilters(int maxFilters)
        {
            this.maxFilters = maxFilters;
            return this;
        }

        public Builder hitThreshold(int hitThreshold)
        {
            this.hitThreshold = hitThreshold;
            return this;
        }

        public Builder minFilterValueLength(int minFilterValueLength)
        {
            this.minFilterValueLength = minFilterValueLength;
            return this;
        }

        public Builder sortBy(String sortBy)
        {
            this.sortBy = sortBy;
            return this;
        }

        public Builder scope(String scope)
        {
            this.scope = scope;
            return this;
        }

        public Builder scopedSites(Set<String> scopedSites)
        {
            this.scopedSites = scopedSites;
            return this;
        }

        public Builder index(int index)
        {
            this.index = index;
            return this;
        }

        public Builder isEnabled(boolean isEnabled)
        {
            this.isEnabled = isEnabled;
            return this;
        }

        public SolrFacetProperties build()
        {
            return new SolrFacetProperties(this);
        }
    }
}
