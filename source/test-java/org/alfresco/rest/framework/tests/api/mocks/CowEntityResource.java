package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;

import java.util.Arrays;
import java.util.List;

@EntityResource(name="cow", title="Cow")
public class CowEntityResource implements EntityResourceAction.ReadByIdWithResponse<Goat>,
                                            EntityResourceAction.ReadWithResponse<Goat>,
                                            EntityResourceAction.CreateWithResponse<Goat>,
                                            EntityResourceAction.UpdateWithResponse<Goat>,
                                            EntityResourceAction.DeleteWithResponse{

    @Override
    public Goat readById(String id, Parameters parameters, WithResponse withResponse)
    {
        return new Goat("Goat"+id);
    }

    @Override
    public CollectionWithPagingInfo<Goat> readAll(Parameters params, WithResponse withResponse)
    {
        return CollectionWithPagingInfo.asPaged(params.getPaging(), Arrays.asList(new Goat("Cow1")));
    }

    @Override
    public List<Goat> create(List<Goat> entities, Parameters parameters, WithResponse withResponse)
    {
        return entities;
    }

    @Override
    public void delete(String id, Parameters parameters, WithResponse withResponse)
    {

    }

    @Override
    public Goat update(String id, Goat entity, Parameters parameters, WithResponse withResponse)
    {
        return entity;
    }
}
