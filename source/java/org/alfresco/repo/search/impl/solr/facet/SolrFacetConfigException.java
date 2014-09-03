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
