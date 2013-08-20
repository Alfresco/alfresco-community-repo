package org.alfresco.rest.framework.tests.api.mocks3;

import java.io.InputStream;

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@EntityResource(name="flocket",title="A resource used for testing binary properties with lost of properties")
public class FlocketEntityResource implements BinaryResourceAction.Read, BinaryResourceAction.Delete, BinaryResourceAction.Update
{

    @Override
    @WebApiDescription(title = "Updates a flocket")
    @BinaryProperties({"photo","album"})
    public void update(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters params)
    {
        return;
    }

    @Override
    @WebApiDescription(title = "Deletes a photo")
    @BinaryProperties("photo")
    public void deleteProperty(String entityId, Parameters parameters)
    {
    }

    @Override
    @WebApiDescription(title = "Reads a photo as a Stream")
    @BinaryProperties({"photo","album", "madeUpProp"})
    public BinaryResource readProperty(String entityId, Parameters parameters) throws EntityNotFoundException
    {
        return null;
    }

}
