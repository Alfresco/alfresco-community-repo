package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@EntityResource(name=SheepEntityResource.ENTITY_KEY,title="Sheep")
public class SheepEntityResource implements EntityResourceAction.Read<Sheep>,EntityResourceAction.ReadById<Sheep>, EntityResourceAction.Update<Sheep>, EntityResourceAction.Delete
{
    public static final String ENTITY_KEY = "sheep";
    @Override
    public void delete(String id, Parameters parameters)
    {
    }

    @Override
    public Sheep update(String id, Sheep entity, Parameters parameters)
    {
        return entity;
    }

    @Override
    public Sheep readById(String id, Parameters parameters)
    {
        return new Sheep(id);
    }

    @Override
    @WebApiDescription(title = "Gets all the Sheep")
    @WebApiParameters({
                @WebApiParam(name = "siteId", title = "Site id", description="What ever."),
                @WebApiParam(name = "who", title = "Who", kind=ResourceParameter.KIND.HTTP_HEADER),
                @WebApiParam(name = "body", title = "aintnobody", kind=ResourceParameter.KIND.HTTP_BODY_OBJECT),
                @WebApiParam(name = "requiredParam", title = "",required=true, kind=ResourceParameter.KIND.QUERY_STRING)})
    public CollectionWithPagingInfo<Sheep> readAll(Parameters params)
    {
        return CollectionWithPagingInfo.asPagedCollection(new Sheep("paged"));
    }

}
