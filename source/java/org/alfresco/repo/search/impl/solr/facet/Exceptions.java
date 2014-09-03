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

import org.alfresco.error.AlfrescoRuntimeException;

/** These exceptions are thrown by the {@link SolrFacetService}. */
public class Exceptions extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1L;
    
    // Constructors for the basic SolrFacet Exception itself.
    public Exceptions()                                { this("", null); }
    public Exceptions(String message)                  { this(message, null); }
    public Exceptions(Throwable cause)                 { this("", null); }
    public Exceptions(String message, Throwable cause) { super(message, cause); }
    
    /** This exception is used to signal a bad parameter. */
    public static class IllegalArgument extends Exceptions
    {
        private static final long serialVersionUID = 1L;
        
        public IllegalArgument()               { super(); }
        public IllegalArgument(String message) { super(message); }
    }
    
    public static class MissingFacetId extends IllegalArgument
    {
        private static final long serialVersionUID = 1L;
        
        public MissingFacetId()               { super(); }
        public MissingFacetId(String message) { super(message); }
    }
    
    public static class DuplicateFacetId extends IllegalArgument
    {
        private static final long serialVersionUID = 1L;
        private final String facetId;
        
        public DuplicateFacetId(String facetId)
        {
            this("", facetId);
        }
        public DuplicateFacetId(String message, String facetId)
        {
            super(message);
            this.facetId = facetId;
        }
        
        public String getFacetId() { return this.facetId; }
    }
    
    public static class UnrecognisedFacetId extends IllegalArgument
    {
        private static final long serialVersionUID = 1L;
        private final String facetId;
        
        public UnrecognisedFacetId(String facetId)
        {
            this("", facetId);
        }
        public UnrecognisedFacetId(String message, String facetId)
        {
            super(message);
            this.facetId = facetId;
        }
        
        public String getFacetId() { return this.facetId; }
    }
}
