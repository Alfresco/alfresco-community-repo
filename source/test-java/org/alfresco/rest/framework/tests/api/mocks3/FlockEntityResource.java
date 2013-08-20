package org.alfresco.rest.framework.tests.api.mocks3;

import java.io.File;
import java.io.InputStream;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.TempFileProvider;

@EntityResource(name="flock",title="A resource used for testing binary properties")
public class FlockEntityResource implements BinaryResourceAction.Read, BinaryResourceAction.Delete, BinaryResourceAction.Update
{

    //versions/1/flock/xyz/photo PUT
    @Override
    @WebApiDescription(title = "Updates a photo")
    @BinaryProperties("photo")
    public void update(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters params)
    {
        return;
    }

    //versions/1/flock/xyz/photo DELETE
    @Override
    @WebApiDescription(title = "Deletes a photo")
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, Parameters parameters)
    {
    }

    //versions/1/flock/xyz/photo GET
    @Override
    @WebApiDescription(title = "Reads a photo as a Stream")
    @BinaryProperties("photo")
    public BinaryResource readProperty(String entityId, Parameters parameters) throws EntityNotFoundException
    {
        
        File file = TempFileProvider.createTempFile("doesn't matter", ".txt");
        return new FileBinaryResource(file);
    }

}
