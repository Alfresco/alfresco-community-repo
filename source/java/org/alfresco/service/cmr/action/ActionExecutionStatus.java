package org.alfresco.service.cmr.action;

import java.io.Serializable;

/**
 * Action execution status enumeration
 * 
 * @deprecated Use {@link ActionStatus} instead
 * @author Roy Wetherall
 */
public enum ActionExecutionStatus implements Serializable
{
	PENDING,		// The action is queued pending execution
	RUNNING,		// The action is currently executing
	SUCCEEDED,		// The action has completed successfully
	FAILED,			// The action has failed
	COMPENSATED		// The action has failed and a compensating action has been been queued for execution
}
