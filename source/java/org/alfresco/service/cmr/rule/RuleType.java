/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
    public static final String UPDATE = "update";
	public static final String OUTBOUND = "outbound";
	
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
	 * @param nodeRef			   	    the node ref whos rule of rule type are to be triggered
	 * @param actionedUponNodeRef	    the node ref that the triggered rule will action upon
     * @param executeRuleImmediately    indicates whether the rule should be executed immediately or not
	 */
	public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately);
}