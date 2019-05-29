/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import org.alfresco.rest.api.model.Favourite;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Centralises access to favourites functionality and maps between representations repository and api representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public interface Favourites
{
    String PARAM_INCLUDE_PATH = Nodes.PARAM_INCLUDE_PATH;

    String PARAM_INCLUDE_PROPERTIES = Nodes.PARAM_INCLUDE_PROPERTIES;

    /**
     * Add a favourite for user personId
     *
     * @param personId the personId for which the favourite is to be added
     * @param favourite the favourite to add
     */
    Favourite addFavourite(String personId, Favourite favourite);

    /**
     * Add a favourite for user personId taking parameters into account
     *
     * @param personId   the personId for which the favourite is to be added
     * @param favourite  the favourite to add
     * @param parameters the parameters
     */
    Favourite addFavourite(String personId, Favourite favourite, Parameters parameters);

    /**
     * Add a favourite for user personId
     *
     * @param personId the personId for which the favourite is to be removed
     * @param id the id of the favourite to remove (id is a uuid)
     */
    void removeFavourite(String personId, String id);

    /**
     * Get a paged list of favourites for user personId
     *
     * @param personId the personId for which the favourite is to be removed
     * @param parameters Parameters
     * @return paged favourites
     */
    CollectionWithPagingInfo<Favourite> getFavourites(String personId, final Parameters parameters);

    /**
     * Get a specific favourite for user personId
     *
     * @param personId the personId for which the favourite is to be removed
     * @param favouriteId the favourite id
     * @return the favourite
     */
    Favourite getFavourite(String personId, String favouriteId);

    /**
     * Get a specific favourite for user personId taking parameters into account
     *
     * @param personId    the personId for which the favourite is to be removed
     * @param favouriteId the favourite id
     * @param parameters  the parameters
     * @return the favourite
     */
    Favourite getFavourite(String personId, String favouriteId, Parameters parameters);
}
