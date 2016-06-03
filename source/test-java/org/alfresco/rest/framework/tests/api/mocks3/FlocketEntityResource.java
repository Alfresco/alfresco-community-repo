/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
public class FlocketEntityResource implements BinaryResourceAction.Read, BinaryResourceAction.Delete, BinaryResourceAction.Update<Flocket>
{

    @Override
    @WebApiDescription(title = "Updates a flocket")
    @BinaryProperties({"photo","album"})
    public Flocket updateProperty(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters params)
    {
        return null;
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
