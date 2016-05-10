package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@RelationshipResource(name = "calf", entityResource=CowEntityResource.class, title = "Muma")
public class CalfRelationshipResource implements RelationshipResourceAction.ReadByIdWithResponse<Goat>,
        RelationshipResourceAction.ReadWithResponse<Goat>,
        RelationshipResourceAction.CreateWithResponse<Goat>,
        RelationshipResourceAction.UpdateWithResponse<Goat>,
        RelationshipResourceAction.DeleteWithResponse,
        RelationshipResourceBinaryAction.ReadWithResponse,
        RelationshipResourceBinaryAction.DeleteWithResponse,
        RelationshipResourceBinaryAction.UpdateWithResponse {

    @Override
    public List<Goat> create(String entityResourceId, List<Goat> entities, Parameters parameters, WithResponse withResponse)
    {
        return entities;
    }

    @Override
    public void delete(String entityResourceId, String id, Parameters parameters, WithResponse withResponse)
    {

    }

    @Override
    public Goat readById(String entityResourceId, String id, Parameters parameters, WithResponse withResponse) throws RelationshipResourceNotFoundException
    {
        return null;
    }

    @Override
    public CollectionWithPagingInfo<Goat> readAll(String entityResourceId, Parameters params, WithResponse withResponse)
    {
        return CollectionWithPagingInfo.asPaged(params.getPaging(), Arrays.asList(new Goat("Cow1")));
    }

    @Override
    public Goat update(String entityResourceId, Goat entity, Parameters parameters, WithResponse withResponse)
    {
        return entity;
    }

    @Operation("chew")
    public String chewTheGrass(String entityId, String id, Void notused, Parameters parameters, WithResponse withResponse) {
        return "Yum";
    }


    @Override
    @WebApiDescription(title = "Reads a photo")
    @BinaryProperties("photo")
    public BinaryResource readProperty(String entityId, String id, Parameters parameters, WithResponse withResponse) throws EntityNotFoundException
    {
        return null;
    }

    @Override
    @WebApiDescription(title = "Deletes a photo")
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, String entityResourceId, Parameters parameters, WithResponse withResponse)
    {

    }

    @Override
    @WebApiDescription(title = "Updates a photo")
    @BinaryProperties("photo")
    public Object updateProperty(String entityId, String entityResourceId, BasicContentInfo contentInfo, InputStream stream, Parameters params, WithResponse withResponse)
    {
        return null;
    }
}
