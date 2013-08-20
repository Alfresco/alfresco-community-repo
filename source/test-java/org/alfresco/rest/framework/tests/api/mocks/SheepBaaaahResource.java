package org.alfresco.rest.framework.tests.api.mocks;

import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Implements Get
 *
 * @author Gethin James
 */
@RelationshipResource(name = "baaahh", entityResource=SheepEntityResource.class, title = "Sheep baaah")
public class SheepBaaaahResource implements RelationshipResourceAction.Read<Sheep>, RelationshipResourceAction.ReadById<Sheep>
{

    @Override
    public Sheep readById(String entityResourceId, String id, Parameters parameters)
    {
        return new Sheep("Z2");
    }

    @Override
    public CollectionWithPagingInfo<Sheep> readAll(String entityResourceId, Parameters params)
    {
        List<Sheep> toReturn = Arrays.asList(new Sheep("D1"), new Sheep("Z2"), new Sheep("4X"));
        toReturn = toReturn.subList(0, params.getPaging().getMaxItems()>toReturn.size()?toReturn.size():params.getPaging().getMaxItems());
        return CollectionWithPagingInfo.asPaged(params.getPaging(),toReturn,toReturn.size()!=3 ,3);
    }

}
