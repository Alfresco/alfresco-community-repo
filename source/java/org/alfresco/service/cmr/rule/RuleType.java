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
package org.alfresco.service.cmr.rule;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Rule type interface.
 * 
 * @author Roy Wetherall
 */
public interface RuleType
{
	/**
	 * Some rule type constants
	 */
	public static final String INBOUND = "inbound";
	public static final String OUTGOING = "outgoing";
	
	/**
	 * Get the name of the rule type.
	 * <p>
	 * The name is unique and is used to identify the rule type.
	 * 
	 * @return	the name of the rule type
	 */
	public String getName();
	
	/**
	 * Get the display label of the rule type.
	 * 
	 * @return	the display label
	 */
	public String getDisplayLabel();

	/**
	 * Trigger the rules of the rule type for the node on the actioned upon node.
	 * 
	 * @param nodeRef				the node ref whos rule of rule type are to be triggered
	 * @param actionedUponNodeRef	the node ref that the triggered rule will action upon
	 */
	public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef);
}