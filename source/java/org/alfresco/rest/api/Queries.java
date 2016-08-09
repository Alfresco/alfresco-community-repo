/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;

/**
 * Queries API
 *
 * @author janv
 * @author Alan Davis
 */
public interface Queries
{
    // General
    static String PARAM_TERM    = "term";
    static String PARAM_ORDERBY = ResourceWebScriptHelper.PARAM_ORDERBY;
    static String PARAM_FIELDS = ResourceWebScriptHelper.PARAM_FILTER_FIELDS;
    static String PARAM_INCLUDE = ResourceWebScriptHelper.PARAM_INCLUDE;
    
    // Node query
    static String PARAM_ROOT_NODE_ID = "rootNodeId";
    static String PARAM_NODE_TYPE    = "nodeType";
    static String PARAM_NAME         = "name";
    static String PARAM_CREATEDAT    = "createdAt";
    static String PARAM_MODIFIEDAT   = "modifiedAt";
    static int MIN_TERM_LENGTH_NODES = 3;
    
    // People query
    static String PARAM_PERSON_ID  = "id";
    static String PARAM_FIRSTNAME = ContentModel.PROP_FIRSTNAME.getLocalName();
    static String PARAM_LASTNAME  = ContentModel.PROP_LASTNAME.getLocalName();
    static int MIN_TERM_LENGTH_PEOPLE = 2;
    
    // Sites query
    static String PARAM_SITE_ID          = "id";
    static String PARAM_SITE_TITLE       = "title";
    static String PARAM_SITE_DESCRIPTION = "description";
    static int MIN_TERM_LENGTH_SITES     = 2;
    
    /**
     * Find Nodes
     *
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *
     * @return the search query results
     */
    CollectionWithPagingInfo<Node> findNodes(Parameters parameters);

    /**
     * Find People
     * 
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *
     * @return the search query results
     */
    CollectionWithPagingInfo<Person> findPeople(Parameters parameters);

    /**
     * Find Sites
     *
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *
     * @return the search query results
     */
    CollectionWithPagingInfo<Site> findSites(Parameters parameters);
}
