package org.alfresco.rest.core.swagger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.swagger.models.Operation;

public class SwaggerRequest
{
    private Configuration cfg;
    private Operation swaggerRequest;
    private String httpMethod;
    private String pathUrl;

    private Configuration getConfig() throws IOException
    {
        if (cfg == null)
        {
            cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setDirectoryForTemplateLoading(new File("src/main/resources"));
        }
        return cfg;
    }
    
    public SwaggerRequest(String httpMethod, String pathUrl, Operation swaggerRequest)
    {
        this.swaggerRequest = swaggerRequest;
        this.httpMethod = httpMethod;
        this.pathUrl = pathUrl;
    }

    public String getRequestSample() throws Exception
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
}
