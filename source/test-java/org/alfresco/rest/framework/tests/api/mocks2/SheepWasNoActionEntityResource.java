package org.alfresco.rest.framework.tests.api.mocks2;

import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;

/**
 * A mock version 2 that now does an action
 * 
 * @author Gethin James
 */
@EntityResource(name="sheepnoaction",title="Sheep No Action has been deprecated.")
@Deprecated
public class SheepWasNoActionEntityResource implements EntityResourceAction.ReadById<Sheep> 
{

    @Override
    @Deprecated
    public Sheep readById(String id, Parameters parameters)
    {
        return new Sheep(id);
    }

}
