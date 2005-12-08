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
package org.alfresco.repo.action.executer;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public interface ActionExecuter
{
	/**
	 * Get the action definition for the action
	 * 
	 * @return  the action definition
	 */
	public ActionDefinition getActionDefinition();
	
    /**
     * Execute the action executer
     * 
     * @param action				the action
     * @param actionedUponNodeRef	the actioned upon node reference
     */
    public void execute(
			Action action,
            NodeRef actionedUponNodeRef);
}
