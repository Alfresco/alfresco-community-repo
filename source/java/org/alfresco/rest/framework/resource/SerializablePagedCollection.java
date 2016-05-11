package org.alfresco.rest.framework.resource;

import org.alfresco.rest.framework.resource.parameters.Paging;

import java.util.Collection;

/**
 * A specialist representation of a Collection that can be serialized to json with paging information
 *
 * @author Gethin James.
 */
public interface SerializablePagedCollection<T>
{
    /**
     * Returns the Collection object
     * @return Collection
     */
    Collection<T> getCollection();

    /**
     * Indicates if the returned collection has more items after the current returned list.
     */
    boolean hasMoreItems();

    /**
     * Indicates the total number of items available.
     *
     * Can be greater than the number of items returned in the list.
     *
     */
    Integer getTotalItems();

    /**
     * The requested paging parameters set by the client
     */
    Paging getPaging();
}
