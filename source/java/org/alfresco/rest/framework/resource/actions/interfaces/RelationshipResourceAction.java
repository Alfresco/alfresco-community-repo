package org.alfresco.rest.framework.resource.actions.interfaces;

import java.util.List;

import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Permissible actions for an Relationship Resources
 * Based around CRUD - Create, ReadAll, ReadById, Update, Delete
 * 
 * @author Gethin James
 */
public interface RelationshipResourceAction
{

    /**
     * HTTP GET - Retrieve list of related entities by its related entityResource Id
     */
    public static interface Read<E> extends ResourceAction
    {
        /**
         * Reads all the relationship entities from the collection using the related entityResourceId.
         * 
         * Paging information is provided.  
         * @param entityResourceId Entity resource context for this relationship
         * @param params - will never be null and will have the PAGING default values
         * @return
         */
        public CollectionWithPagingInfo<E> readAll (String entityResourceId, Parameters params);
    }
    
    /**
     * HTTP GET - Retrieve a relation by its unique id & entity context
     */
    public static interface ReadById<E> extends ResourceAction
    {
        public E readById (String entityResourceId, String id,  Parameters parameters) throws RelationshipResourceNotFoundException;
    }
    
    /**
     * HTTP PUT - Update entity (by its related entityResource Id) if it exists, error if not
     */
    public static interface Update<E> extends ResourceAction
    {
        public E update (String entityResourceId, E entity,  Parameters parameters);
    }
    
    /**
     * HTTP POST - Create one or more new entity
     */
    public static interface Create<E> extends ResourceAction
    {
        public List<E> create (String entityResourceId, List<E> entity,  Parameters parameters);
    }
    
    /**
     * HTTP DELETE - Deletes a relation by its unique id & entity context
     */
    public static interface Delete extends ResourceAction
    {
        public void delete (String entityResourceId, String id,  Parameters parameters);
    }

}
