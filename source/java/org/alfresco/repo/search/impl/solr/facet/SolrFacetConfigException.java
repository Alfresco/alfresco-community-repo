
package org.alfresco.repo.search.impl.solr.facet;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetConfigException extends AlfrescoRuntimeException
{
    /** Serial version UID */
    private static final long serialVersionUID = -6602241998042605142L;

    /**
     * Constructor
     * 
     * @param msgId message id
     */
    public SolrFacetConfigException(String msgId)
    {
        super(msgId);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param msgParams message params
     */
    public SolrFacetConfigException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param cause causing exception
     */
    public SolrFacetConfigException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param msgParams message params
     * @param cause causing exception
     */
    public SolrFacetConfigException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
