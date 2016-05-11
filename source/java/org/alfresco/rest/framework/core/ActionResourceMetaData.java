package org.alfresco.rest.framework.core;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.resource.actions.interfaces.ResourceAction;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends ResourceMetaData to give more information about an Action
 *
 * @author Gethin James
 */
public class ActionResourceMetaData extends ResourceMetadata
{
    private final Method actionMethod;

    /**
     * Use this constructor to create the resource metadata
     * @param uniqueId
     * @param operations
     * @param api
     * @param actionMethod
     */
    public ActionResourceMetaData(String uniqueId, List<ResourceOperation> operations, Api api, Method actionMethod)
    {
        super(uniqueId, RESOURCE_TYPE.ACTION, operations, api, null, null);
        if (operations.size()!= 1)
        {
            throw new IllegalArgumentException("Only 1 action per url is supported for an entity");
        }
        this.actionMethod = actionMethod;
    }

    /**
     * Constructor to use when it has been deleted
     * @param uniqueId
     * @param api
     * @param apiDeleted
     */
    public ActionResourceMetaData(String uniqueId, Api api, Set<Class<? extends ResourceAction>> apiDeleted)
    {
        super(uniqueId, RESOURCE_TYPE.ACTION, null, api, apiDeleted, null);
        this.actionMethod = null;
    }

    public Method getActionMethod()
    {
        return actionMethod;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("ActionResourceMetaData [api=");
        builder.append(this.getApi());
        builder.append(", uniqueId=");
        builder.append(this.getUniqueId());
        builder.append(", type=");
        builder.append(this.getType());
        builder.append(", parent=");
        builder.append(this.getParentResource());
        builder.append(", operations=");
        builder.append(this.getOperations());
        builder.append(", apiDeleted=");
        builder.append(this.getApiDeleted());
        builder.append("actionMethod=").append(actionMethod);
        builder.append("]");
        return builder.toString();
    }
}
