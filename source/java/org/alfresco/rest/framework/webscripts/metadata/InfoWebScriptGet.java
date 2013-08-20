package org.alfresco.rest.framework.webscripts.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionary;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.webscripts.ApiWebScript;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Provides general information about the Api calls and methods.
 * 
 * @author Gethin James
 */
public class InfoWebScriptGet extends ApiWebScript
{

    private ResourceLookupDictionary lookupDictionary;
    

    public void setLookupDictionary(ResourceLookupDictionary lookupDictionary)
    {
        this.lookupDictionary = lookupDictionary;
    }


    @Override
    public void execute(final Api api, WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        
        ResourceDictionary resourceDic = lookupDictionary.getDictionary();
        final Map<String, ResourceWithMetadata> apiResources = resourceDic.getAllResources().get(api);
        if (apiResources == null)
        {
          throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_INVALID_API);         
        }
        
        jsonHelper.withWriter(res.getOutputStream(), new Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {   

               List<ExecutionResult> entities = new ArrayList<ExecutionResult>();
               for (ResourceWithMetadata resource : apiResources.values())
               {
                 entities.add(new ExecutionResult(resource.getMetaData(), null));
               }
               Collections.sort(entities, new Comparator<ExecutionResult>()
               {
                    public int compare(ExecutionResult r1, ExecutionResult r2)
                    {
                        return ((ResourceMetadata) r1.getRoot()).getUniqueId().compareTo(((ResourceMetadata) r2.getRoot()).getUniqueId());
                    }
               });
               objectMapper.writeValue(generator, CollectionWithPagingInfo.asPaged(Paging.DEFAULT,entities));
            }
        });
    }

}
