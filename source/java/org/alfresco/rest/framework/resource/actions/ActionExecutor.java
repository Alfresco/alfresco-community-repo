package org.alfresco.rest.framework.resource.actions;

import org.alfresco.rest.framework.core.HttpMethodSupport;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.Params;

/**
 * Executes an action in the system
 *
 * @author Gethin James
 */
public interface ActionExecutor extends HttpMethodSupport
{

    /**
     * Invokes the resource with the Params calling the callback onSuccess
     * @param resource
     * @param params
     * @param executionCallback
     */
    @SuppressWarnings("rawtypes")
    public void execute(ResourceWithMetadata resource, Params params, ExecutionCallback executionCallback);
    
    /**
     * The result of an Action execution.
     *
     * @author Gethin James
     */
    public interface ExecutionCallback<R>
    {
        public void onSuccess(R result, ContentInfo contentInfo);
    }
    
}
