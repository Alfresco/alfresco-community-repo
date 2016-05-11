package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.springframework.extensions.webscripts.Status;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@EntityResource(name="cow", title="Cow")
public class CowEntityResource implements EntityResourceAction.ReadByIdWithResponse<Goat>,
                                            EntityResourceAction.ReadWithResponse<Goat>,
                                            EntityResourceAction.CreateWithResponse<Goat>,
                                            EntityResourceAction.UpdateWithResponse<Goat>,
                                            EntityResourceAction.DeleteWithResponse,
        BinaryResourceAction.ReadWithResponse,
        BinaryResourceAction.DeleteWithResponse,
        BinaryResourceAction.UpdateWithResponse<Goat>
{

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
        withResponse.setStatus(Status.STATUS_ACCEPTED);
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

    
    @Override
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, Parameters parameters, WithResponse withResponse)
    {

    }

    @Override
    @BinaryProperties("photo")
    public BinaryResource readProperty(String entityId, Parameters parameters, WithResponse withResponse) throws EntityNotFoundException
    {
        return null;
    }

    @Override
    @BinaryProperties("photo")
    public Goat updateProperty(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters params, WithResponse withResponse)
    {
        return null;
    }
}
