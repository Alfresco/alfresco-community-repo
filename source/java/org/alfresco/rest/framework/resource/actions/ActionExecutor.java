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
     * @param boolean should we use a readonly transaction.
     */
    @SuppressWarnings("rawtypes")
    public Object execute(ResourceWithMetadata resource, Params params, WebScriptResponse res, boolean isReadOnly);

}
