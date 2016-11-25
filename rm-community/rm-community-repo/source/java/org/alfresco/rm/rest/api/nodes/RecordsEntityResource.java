/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.nodes;

import java.io.InputStream;

import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMNodes;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a record
 *
 * @author Ana Bozianu
 * @since 2.6
 */
@EntityResource(name="records", title = "Records")
public class RecordsEntityResource implements BinaryResourceAction.Update<Node>,
                                              BinaryResourceAction.Read,
                                              InitializingBean
{

    private RMNodes nodes;

    public void setNodes(RMNodes nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        
    }

    /**
     * Download content
     * 
     * @param entityId
     * @param parameters {@link Parameters}
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    @WebApiDescription(title = "Download content", description = "Download content")
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String entityId, Parameters parameters) throws EntityNotFoundException
    {
        return nodes.getContent(entityId, parameters, true);
    }

    /**
     * Upload new version of content
     * 
     * This allow binary content update of an existing record.
     * 
     * Note: alternatively, can upload via POST (multipart/form-data) with existing file name and form "overwrite=true".
     * 
     * @param entityId
     * @param contentInfo Basic information about the content stream
     * @param stream An inputstream
     * @param parameters
     * @return
     */
    @Override
    @WebApiDescription(title = "Upload content", description = "Upload content")
    @BinaryProperties({"content"})
    public Node updateProperty(String entityId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters)
    {
        return nodes.updateContent(entityId, contentInfo, stream, parameters);
    }
}
