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
package org.alfresco.rest.api.downloads;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.Downloads;
import org.alfresco.rest.api.model.Download;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author cpopa
 *
 */
@EntityResource(name = "downloads", title = "Downloads")
public class DownloadsEntityResource implements EntityResourceAction.Create<Download>, EntityResourceAction.ReadById<Download>, EntityResourceAction.Delete, InitializingBean
{
    private Downloads downloads;

    public void setDownloads(Downloads downloads)
    {
        this.downloads = downloads;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("downloads", this.downloads);
    }

    @Override
    @WebApiDescription(title = "Create download", description = "Create a download node whose content will be a zip which is being created asynchronously.", successStatus = HttpServletResponse.SC_ACCEPTED)
    @WebApiParam(name = "entity", title = "Download request", description = "Download request which contains the node ids for the zip elements.", 
                 kind = ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple = false)
    public List<Download> create(List<Download> entity, Parameters parameters)
    {
        Download downloadNode = downloads.createDownloadNode(entity.get(0));
        return Collections.singletonList(downloadNode);
    }

    @Override
    @WebApiDescription(title = "Get download information", description = "Get information about the progress of the zip creation.")
    @WebApiParam(name = "nodeId", title = "Download nodeId")
    public Download readById(String nodeId, Parameters parameters) throws EntityNotFoundException
    {
        return downloads.getDownloadStatus(nodeId);
    }
    
    @WebApiDescription(title = "Cancel download", description = "Stop the zip creation if still in progress.", successStatus = HttpServletResponse.SC_ACCEPTED)
    @Override
    public void delete(String nodeId, Parameters parameters)
    {
        downloads.cancel(nodeId);
    }

}