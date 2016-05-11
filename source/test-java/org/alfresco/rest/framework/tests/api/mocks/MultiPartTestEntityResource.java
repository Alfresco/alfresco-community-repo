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

package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.ResponseCallBack;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "multiparttest", title = "multi-part upload test")
public class MultiPartTestEntityResource
            implements MultiPartResourceAction.Create<MultiPartTestResponse>
{

    @Override
    @WebApiDescription(title = "Creates a multipart", successStatus = Status.STATUS_ACCEPTED)
    public MultiPartTestResponse create(FormData formData, Parameters parameters, ResponseCallBack withResponse)
    {
        return new MultiPartTestResponse(formData.getParameters().get("filename")[0]);
    }
}
