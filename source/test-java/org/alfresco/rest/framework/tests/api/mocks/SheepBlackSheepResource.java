
package org.alfresco.rest.framework.tests.api.mocks;

import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Implements Get
 * 
 * @author Gethin James
 */
@RelationshipResource(name = "blacksheep",entityResource=SheepEntityResource.class, title = "BlackSheep")
public class SheepBlackSheepResource implements RelationshipResourceAction.Read<Sheep>,
            RelationshipResourceAction.Update<Sheep>, RelationshipResourceAction.Delete,
            RelationshipResourceAction.Create<Sheep>
{


    @Override
    public CollectionWithPagingInfo<Sheep> readAll(String entityResourceId, Parameters params)
    {
        return CollectionWithPagingInfo.asPaged(params.getPaging(),Arrays.asList(new Sheep("D1"), new Sheep("Z2"), new Sheep("4X"), new Sheep("S4")));
    }

    @Override
    public void delete(String entityResourceId, String id, Parameters parameters)
    {
    }

    @Override
    public Sheep update(String entityResourceId, Sheep entity, Parameters parameters)
    {
        return entity;
    }

    @Override
    @WebApiParam(name="entity", title="A single shepp", description="A single sheep, multiples are not supported.", 
    kind=ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false)
    public List<Sheep> create(String entityResourceId, List<Sheep> entity, Parameters parameters)
    {
        return entity;
    }

}
