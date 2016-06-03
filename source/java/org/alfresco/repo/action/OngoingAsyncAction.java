package org.alfresco.repo.action;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.EqualsHelper;

/**
 * This class represents an ongoing asynchronous action.
 */
public class OngoingAsyncAction
{
	private final NodeRef node;
	private final Action action;
	
	public OngoingAsyncAction(NodeRef node, Action action)
	{
		this.node = node;
		this.action = action;
	}

	public NodeRef getNodeRef()
	{
		return node;
	}

	public Action getAction()
	{
		return action;
	}

	@Override
	public boolean equals(Object otherObj)
	{
		if (otherObj == null || !otherObj.getClass().equals(this.getClass()))
		{
			return false;
		}
		OngoingAsyncAction otherNodeBeingActioned = (OngoingAsyncAction)otherObj;
		
        return EqualsHelper.nullSafeEquals(this.node, otherNodeBeingActioned.node) &&
            EqualsHelper.nullSafeEquals(this.action.getActionDefinitionName(), otherNodeBeingActioned.action.getActionDefinitionName());
	}

	@Override
	public int hashCode() {
		return this.node.hashCode() + 7 * this.action.getActionDefinitionName().hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder msg = new StringBuilder();
		msg.append(node).append(", ").append(action.getActionDefinitionName());
		return msg.toString();
	}
}
