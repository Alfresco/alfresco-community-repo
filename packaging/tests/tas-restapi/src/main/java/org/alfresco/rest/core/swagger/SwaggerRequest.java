/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.core.swagger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.models.Operation;

public class SwaggerRequest
{
    private Configuration cfg;
    private Operation swaggerRequest;
    private String httpMethod;
    private String pathUrl;

    private Configuration getConfig()
    {
        if (cfg == null)
        {
            cfg = new Configuration(Configuration.VERSION_2_3_23);
            try
            {
                cfg.setDirectoryForTemplateLoading(new File("src/main/resources"));
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Exception while configuring Freemarker template directory.", e);
            }
        }
        return cfg;
    }
    
    public SwaggerRequest(String httpMethod, String pathUrl, Operation swaggerRequest)
    {
        this.swaggerRequest = swaggerRequest;
        this.httpMethod = httpMethod;
        this.pathUrl = pathUrl;
    }

    public String getRequestSample()
    {
        try
        {
            Template template = getConfig().getTemplate("rest-request.ftl");
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("operationId", swaggerRequest.getOperationId());
            data.put("httpMethod", httpMethod);
            data.put("pathUrl", pathUrl);

            Writer append = new StringWriter();
            template.process(data, append);

            append.close();
            return append.toString();
        }
        catch (IOException | TemplateException e)
        {
            throw new IllegalStateException("Exception while loading sample request.", e);
        }
    }
}
