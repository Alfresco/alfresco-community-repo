package org.alfresco.rest.framework.resource.actions;

import org.alfresco.rest.framework.core.HttpMethodSupport;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Executes an action in the system
 *
 * @author Gethin James
 */
public interface ActionExecutor extends HttpMethodSupport
{

    /**
     * Invokes the resource with the Params
     * @param resource ResourceWithMetadata
     * @param params Params
     */
    @SuppressWarnings("rawtypes")
    public Object executeAction(ResourceWithMetadata resource, Params params) throws Throwable;

}
