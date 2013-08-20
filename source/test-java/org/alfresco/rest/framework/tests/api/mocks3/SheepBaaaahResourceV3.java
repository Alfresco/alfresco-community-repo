package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks.SheepNoActionEntityResource;

/**
 * A mock version 3 relationship resource.  This is attached to the SheepNoActionEntityResource action (version 1).
 * In version 1, SheepNoActionEntityResource did no action.
 * In version 2, SheepNoActionEntityResource was overridden by SheepWasNoActionEntityResource and given an action
 * In this version (3) - a relationship resource is being added to the ORIGNAL entity (which has been overridden).
 * 
 * It is expected that this is correct behavior.  This relationship resource could have been attached to either
 * the SheepNoActionEntityResource or the SheepWasNoActionEntityResource with the same result.  This is because
 * they both use the same entity key (name="sheepnoaction")
 * 
 * @author Gethin James
 */
@RelationshipResource(name = "v3isaresource", entityResource=SheepNoActionEntityResource.class, title = "Sheep Version 3 resource")
public class SheepBaaaahResourceV3 implements RelationshipResourceAction.ReadById<Sheep>
{

    @Override
    public Sheep readById(String entityResourceId, String id, Parameters parameters)
    {
        return new Sheep("Z2");
    }


}
