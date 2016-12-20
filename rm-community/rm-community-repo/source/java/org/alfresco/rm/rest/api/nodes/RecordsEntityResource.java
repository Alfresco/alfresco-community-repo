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

import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMNodes;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a record
 *
 * @author Ana Bozianu
 * @since 2.6
 */
@EntityResource(name="records", title = "Records")
public class RecordsEntityResource implements BinaryResourceAction.Read,
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
        ParameterCheck.mandatory("nodes", this.nodes);
    }

    /**
     * Download content
     * 
     * @param recordId the id of the record to get the content from
     * @param parameters {@link Parameters}
     * @return binary content resource
     * @throws EntityNotFoundException
     */
    @Override
    @WebApiDescription(title = "Download content", description = "Download content")
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String recordId, Parameters parameters) throws EntityNotFoundException
    {
        return nodes.getContent(recordId, parameters, true);
    }

}
