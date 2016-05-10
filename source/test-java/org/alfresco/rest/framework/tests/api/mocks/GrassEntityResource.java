package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

import java.util.List;

@EntityResource(name = "grass", title="Grass")
public class GrassEntityResource implements EntityResourceAction.ReadById<Grass>, EntityResourceAction.Create<Grass>, EntityResourceAction.Delete {

    @Override
    @WebApiDescription(title = "Gets grass by id")
    @WebApiParam(name = "justone", title = "Only 1 param and its required.",required=true)
    public Grass readById(String id, Parameters parameters)
    {
        return new Grass(id);
    }

    @Operation("cut")
    public String cutLawn(String id, Void notused, Parameters parameters) {
        return "All done";
    }

    @Operation("grow")
    @WebApiDescription(title = "Grow the grass")
    @WebApiParam(name = "Grass", title = "The grass.",required=true, kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    public String growTheLawn(String id, Grass grass, Parameters parameters) {
        return "Growing well";
    }

    @Override
    @WebApiDescription(title = "Create some grass")
    public List<Grass> create(List<Grass> entity, Parameters parameters)
    {
        return entity;
    }

    @Override
    public void delete(String id, Parameters parameters)
    {
        //I did a delete
    }
}
