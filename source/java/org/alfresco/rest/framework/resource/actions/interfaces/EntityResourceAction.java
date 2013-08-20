package org.alfresco.rest.framework.resource.actions.interfaces;

import java.util.List;

import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Permissible actions for an Entity Resource
 * 
 * Supports full CRUD (Create, Read, Update, Delete) and ReadAll
 *
 * @author Gethin James
 */

public interface EntityResourceAction
{
    /**
     * HTTP POST - Create a new entity
     */
    public static interface Create<E> extends ResourceAction
    {
        public List<E> create (List<E> entity,  Parameters parameters);
    }
  
    /**
     * HTTP GET - Retrieve list of entities
     */
    public static interface Read<E> extends ResourceAction
    {
        
        /**
         * Reads all the entries from the collection.
         * 
         * Paging information is provided.
         * @param params - will never be null and will have the PAGING default values
         * @return
         */
        public CollectionWithPagingInfo<E> readAll (Parameters params);
    }
    
    /**
     * HTTP GET - Retrieve an entity by its unique id
     */
    public static interface ReadById<E> extends ResourceAction
    {
        public E readById (String id,  Parameters parameters) throws EntityNotFoundException;
    }
    
    /**
     * HTTP PUT - Update entity if it exists, error if not
     */
    public static interface Update<E> extends ResourceAction
    {
        public E update (String id, E entity,  Parameters parameters);
    }
    
    /**
     * HTTP DELETE - Deletes an entity
     */
    public static interface Delete extends ResourceAction
    {
        public void delete (String id,  Parameters parameters);
    }
}
