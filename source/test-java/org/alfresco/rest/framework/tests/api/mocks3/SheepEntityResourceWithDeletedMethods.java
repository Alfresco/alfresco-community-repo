package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.WebApiDeleted;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;

/**
 * This no longer does all the actions.
 * 
 * The original SheepEntityResource had 4 actions: update, readById, readAll, delete
 * update and delete methods are marked as deleted
 * readAll is untouched - (only present in SheepEntityResource);
 * readById is overridden.
 *
 * @author Gethin James
 */
public class SheepEntityResourceWithDeletedMethods extends SheepEntityResource 
{

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource#update(java.lang.String, org.alfresco.rest.framework.tests.api.mocks.Sheep)
     */
    @Override
    @WebApiDeleted
    public Sheep update(String id, Sheep entity, Parameters parameters)
    {
        return null; //No implementation because its deleted
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource#readById(java.lang.String)
     */
    @Override
    public Sheep readById(String id, Parameters parameters)
    {
        return new Sheep("v3_"+id);
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource#delete(java.lang.String)
     */
    @Override
    @WebApiDeleted
    public void delete(String id, Parameters parameters)
    {
        //No implementation because its deleted
    }

}
