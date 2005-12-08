/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.action;

import java.io.Serializable;

/**
 * Action execution status enumeration
 * 
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
