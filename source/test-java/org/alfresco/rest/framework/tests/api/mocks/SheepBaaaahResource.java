package org.alfresco.rest.framework.tests.api.mocks;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Implements Get
 *
 * @author Gethin James
 */
@RelationshipResource(name = "baaahh", entityResource=SheepEntityResource.class, title = "Sheep baaah")
public class SheepBaaaahResource implements RelationshipResourceAction.Read<Sheep>, RelationshipResourceAction.ReadById<Sheep>, BinaryResourceAction.Read, BinaryResourceAction.Delete,BinaryResourceAction.Update
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


    @Override
    @WebApiDescription(title = "Deletes a photo")
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, Parameters parameters)
    {

    }

    @Override
    @WebApiDescription(title = "Reads a photo")
    @BinaryProperties("photo")
    public BinaryResource readProperty(String entityId, Parameters parameters) throws EntityNotFoundException
    {
        return null;
    }

    @Override
    @WebApiDescription(title = "Updates a photo")
    @BinaryProperties("photo")
    public Object updateProperty(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters params)
    {
        return null;
    }

    @Operation("chew")
    public String chewTheGrass(String entityId, String id, Void notused, Parameters parameters) {
        return "All done";
    }
}
