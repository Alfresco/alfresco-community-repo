package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.TempFileProvider;

import java.io.File;

/**
 * The goat has a herd
 * 
 * @author Gethin James
 */
@RelationshipResource(name = "herd",entityResource=GoatEntityResourceForV3.class, title = "Goat Herd")
public class GoatRelationshipResource implements RelationshipResourceAction.Read<Herd>,  RelationshipResourceBinaryAction.Read, RelationshipResourceBinaryAction.Delete
{
    @Override
    public CollectionWithPagingInfo<Herd> readAll(String entityResourceId, Parameters params)
    {
        return CollectionWithPagingInfo.asPagedCollection(new Herd("bigun"));
    }

    @WebApiDescription(title = "Download content", description = "Download content")
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String herdId, String entityResourceId, Parameters parameters) throws EntityNotFoundException
    {
        File file = TempFileProvider.createTempFile("Its a goat", ".txt");
        return new FileBinaryResource(file);
    }

    @Override
    @BinaryProperties({"content"})
    public void deleteProperty(String entityId, String entityResourceId, Parameters parameters)
    {
        //Its deleted
    }
}
