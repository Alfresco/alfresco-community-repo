package org.alfresco.rest.framework.webscripts;

import org.alfresco.rest.framework.core.HttpMethodSupport;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.springframework.extensions.webscripts.WebScriptRequest;

/*
 * Extracts parameters from the HTTP request.
 * 
 */
public interface ParamsExtractor extends HttpMethodSupport
{
    public Params extractParams(ResourceMetadata resourceMeta,WebScriptRequest req);
}
