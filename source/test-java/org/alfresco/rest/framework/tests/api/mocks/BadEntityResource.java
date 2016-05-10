package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.Status;

@EntityResource(name= BadEntityResource.ENTITY_KEY,title="bad resource that does bad things")
public class BadEntityResource implements EntityResourceAction.Read<Sheep>,EntityResourceAction.ReadById<Sheep>
{
    public static final String ENTITY_KEY = "bad";


    @Override
    public Sheep readById(String id, Parameters parameters)
    {
        throw new IntegrityException("bad integrity", null);
    }

    @Override
    @WebApiDescription(title = "Gets all the Sheep", successStatus = Status.STATUS_ACCEPTED)
    public CollectionWithPagingInfo<Sheep> readAll(Parameters params)
    {
        throw new RuntimeException("read all");
    }

}
