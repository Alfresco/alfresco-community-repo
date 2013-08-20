package org.alfresco.rest.framework.resource.parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A wrapper around Collection that supports paging information.
 *
 * CollectionWithPagingInfo is immutable and collections are unmodifiable. Use the asPaged methods to construct it.
 * 
 * collection - The collection
 * hasMoreItems - Indicates the total number of items available. Can be greater than the number of items returned in the list.
 * totalItems - Indicates the total number of items available. Can be greater than the number of items returned in the list.
 * 
 * @author Gethin James.
 */
public class CollectionWithPagingInfo<T>
{

    private final Collection<T> collection;
    private final boolean hasMoreItems;
    private final Integer totalItems;
    private final Paging paging;
        
    /**
     * Constructs a new CollectionWithPagingInfo.
     * @param aCollection - the collection that needs to be paged.
     * @param hasMoreItems - Are there more items after this Collection?
     * @param paging - Paging request info
     * @param totalItems - The total number of items available.
     * @return CollectionWithPagingInfo
     */
    protected CollectionWithPagingInfo(Collection<T> collection, Paging paging, boolean hasMoreItems, Integer totalItems)
    {
        super();
        this.hasMoreItems = hasMoreItems;
        this.paging = paging;
        
        if (collection == null)
        {
            this.collection = Collections.emptyList();
            this.totalItems = 0;        
        }
        else
        {
            this.collection = collection;
            this.totalItems = totalItems;    
        }
    }
    
    /**
     * Constructs a new CollectionWithPagingInfo.
     * It automatically sets the total items based on the collection size and
     * sets the hasMoreItems variable to false.
     * 
     * @param paging - Paging request info
     * @param aCollection - the collection that needs to be paged.
     * @return CollectionWithPagingInfo
     */
    public static <T> CollectionWithPagingInfo<T> asPaged(Paging paging, Collection<T> aCollection)
    {
        int collectionSize = aCollection==null?0:aCollection.size();
        return new CollectionWithPagingInfo<T>(aCollection, paging, false, collectionSize);
    }
    
    /**
     * Constructs a new CollectionWithPagingInfo using a number of entity values.
     * It automatically creates a Collection, sets the total items and
     * sets the hasMoreItems variable to false.  Paging is set to the default values.
     * 
     * @param entity - the entities to turn into a collection
     * @return CollectionWithPagingInfo
     */
    public static <T> CollectionWithPagingInfo<T> asPagedCollection(T ...entity)
    {
        Collection<T> aNewCollection = Arrays.asList(entity);
        return new CollectionWithPagingInfo<T>(aNewCollection, Paging.DEFAULT, false, aNewCollection.size());
    }
    
    /**
     * Constructs a new CollectionWithPagingInfo.
     * 
     * @param paging - Paging request info
     * @param aCollection - the collection that needs to be paged.
     * @param hasMoreItems - Are there more items after this Collection?
     * @param totalItems - The total number of items available.
     * @return CollectionWithPagingInfo
     */
    public static <T> CollectionWithPagingInfo<T> asPaged(Paging paging, Collection<T> aCollection, boolean hasMoreItems, Integer totalItems)
    {
        return new CollectionWithPagingInfo<T>(aCollection, paging, hasMoreItems, totalItems);
    }
    
    /**
     * Returns the Collection object
     * @return Collection
     */
    public Collection<T> getCollection()
    {
        return this.collection;
    }

    /**
     * Indicates if the returned collection has more items after the current returned list.
     * 
     * @param hasMoreItems
     */
    public boolean hasMoreItems()
    {
        return this.hasMoreItems;
    }
    
    /**
     * Indicates the total number of items available.
     * 
     * Can be greater than the number of items returned in the list.
     * 
     */
    public Integer getTotalItems()
    {
        return this.totalItems;
    }

    /**
     * The requested paging parameters set by the client
     */
    public Paging getPaging()
    {
        return this.paging;
    }
}
