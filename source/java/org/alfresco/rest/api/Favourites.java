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
	/**
	 * Add a favourite for user personId
	 * 
	 * @param personId the personId for which the favourite is to be added
	 * @param favourite the favourite to add
	 */
	Favourite addFavourite(String personId, Favourite favourite);

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
	 * @param parameters
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
}
