package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * A mock version 3 Goat Entity resource.
 * 
 * This replaces the Goat Entity resource for version 1.  Instead of returning a Goat it now
 * returns a Slim Goat with only 2 properties instead of the original 5
 * 
 * @author Gethin James
 */
@EntityResource(name="goat",title="This replaces the Goat Entity resource for version 1")
public class GoatEntityResourceForV3 implements EntityResourceAction.ReadById<SlimGoat>{

    @Override
    public SlimGoat readById(String id, Parameters parameters)
    {
        return new SlimGoat();
    }

}
