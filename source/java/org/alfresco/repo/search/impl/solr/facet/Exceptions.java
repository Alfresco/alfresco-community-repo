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
