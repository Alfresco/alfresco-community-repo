package org.alfresco.service.cmr.favourites;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.favourites.PersonFavourite;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * A service for managing a person's favourites.
 * 
 * Currently supports favouriting of sites, files and folders.
 * 
 * @author steveglover
 *
 */
public interface FavouritesService
{
	/*
	 * Supported favourite types.
	 */
    public static enum Type
    {
    	// Note: ordered
    	FILE, FOLDER, SITE;
    	
    	public static Set<Type> ALL_FILTER_TYPES;
    	
    	static
    	{
    		ALL_FILTER_TYPES = new HashSet<Type>();
    		ALL_FILTER_TYPES.add(FILE);
    		ALL_FILTER_TYPES.add(FOLDER);
    		ALL_FILTER_TYPES.add(SITE);
    		ALL_FILTER_TYPES = Collections.unmodifiableSet(ALL_FILTER_TYPES);
    	}
    };

	public enum SortFields { username, type, createdAt, title };

	/*
	 * Default ordering is (userName ASC, type ASC, createdAt DESC)
	 */
	@SuppressWarnings("unchecked")
	public static List<Pair<FavouritesService.SortFields, Boolean>> DEFAULT_SORT_PROPS = Arrays.asList(
			new Pair<FavouritesService.SortFields, Boolean>(FavouritesService.SortFields.username, Boolean.TRUE),
			new Pair<FavouritesService.SortFields, Boolean>(FavouritesService.SortFields.type, Boolean.TRUE),
			new Pair<FavouritesService.SortFields, Boolean>(FavouritesService.SortFields.createdAt, Boolean.FALSE));

    Type getType(NodeRef nodeRef);
    
    /**
     * Add the entity identified by nodeRef as a favourite for user "userName".
     * 
     * If the nodeRef is already favourited, the favourite entity is returned. No
     * information regarding the favourite e.g. createdAt is updated.
     * 
     * @param userName
     * @param nodeRef
     * @return
     */
    @Auditable(parameters = {"userName", "nodeRef"})
    PersonFavourite addFavourite(String userName, NodeRef nodeRef);

    /**
     * Is the entity identified by nodeRef a favourite document of user "userName".
     * 
     * @param userName
     * @param nodeRef
     * @return
     */
    @Auditable(parameters = {"userName", "nodeRef"})
    boolean isFavourite(String userName, NodeRef nodeRef);

    /**
     * Remove the document identified by nodeRef as a favourite for user "userName".
     * 
     * @param userName
     * @param nodeRef
     * @return
     */
    @Auditable(parameters = {"userName", "nodeRef"})
    boolean removeFavourite(String userName, NodeRef nodeRef);
    
    /**
     * A paged list of favourites for user "userName".
     * 
     * @param userName
     * @param types
     * @param sortProps
     * @param pagingRequest
     * @return
     */
    @Auditable(parameters = {"userName", "types", "pagingRequest"})
    PagingResults<PersonFavourite> getPagedFavourites(String userName, Set<Type> types,
    		List<Pair<FavouritesService.SortFields, Boolean>> sortProps, PagingRequest pagingRequest);

    /**
     * Get a specific favourite for user "userName".
     * 
     * @param userName
     * @param nodeRef
     * @return
     */
    @Auditable(parameters = {"userName", "nodeRef"})
    PersonFavourite getFavourite(String userName, NodeRef nodeRef);
}
