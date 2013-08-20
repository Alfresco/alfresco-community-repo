package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@EntityResource(name="goat", title="Goat")
public class GoatEntityResource implements EntityResourceAction.ReadById<Goat>{

    @Override
    public Goat readById(String id, Parameters parameters)
    {
        return new Goat("Goat"+id);
    }

}
