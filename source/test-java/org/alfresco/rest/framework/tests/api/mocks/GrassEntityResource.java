package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@EntityResource(name = "grass", title="Grass")
public class GrassEntityResource implements EntityResourceAction.ReadById<Grass>{

    @Override
    @WebApiDescription(title = "Gets grass by id")
    @WebApiParam(name = "justone", title = "Only 1 param and its required.",required=true)
    public Grass readById(String id, Parameters parameters)
    {
        return new Grass(id);
    }

}
