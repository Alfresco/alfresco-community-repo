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
package org.alfresco.rest.api.people;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.Favourites;
import org.alfresco.rest.api.model.Favourite;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "favorites", entityResource = PeopleEntityResource.class, title = "Person Favorites")
public class PersonFavouritesRelation implements RelationshipResourceAction.Read<Favourite>, RelationshipResourceAction.ReadById<Favourite>,
RelationshipResourceAction.Create<Favourite>,  RelationshipResourceAction.Delete, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonFavouritesRelation.class);

    private Favourites favourites;

	public void setFavourites(Favourites favourites)
	{
		this.favourites = favourites;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("favourites", this.favourites);
    }

    /**
     * List the user's favourites.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get Person Favorites", description = "Get a paged list of the person's favorites")
    public CollectionWithPagingInfo<Favourite> readAll(String personId, Parameters parameters)
    {
        return favourites.getFavourites(personId, parameters);
    }

	/**
     * Adds the given site as a favourite site for the user.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Create#create(String, java.util.List, org.alfresco.rest.framework.resource.parameters.Parameters)
	 */
	@Override
    @WebApiDescription(title = "Add Person Favorite", description = "Favorite something")
	public List<Favourite> create(String personId, List<Favourite> entity, Parameters parameters)
	{
		List<Favourite> ret = new ArrayList<Favourite>(entity.size());
        for(Favourite favourite : entity)
        {
        	ret.add(favourites.addFavourite(personId, favourite));
        }
        return ret;
	}

	@Override
    @WebApiDescription(title = "Remove Person Favorite", description = "Un-favorite something")
	public void delete(String personId, String id, Parameters parameters)
	{
		favourites.removeFavourite(personId, id);
	}

	@Override
	public Favourite readById(String personId, String favouriteId, Parameters parameters)
			throws RelationshipResourceNotFoundException
	{
		return favourites.getFavourite(personId, favouriteId);
	}
}
