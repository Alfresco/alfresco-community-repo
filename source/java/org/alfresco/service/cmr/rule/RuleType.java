/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
     * @prarm executeRuleImmediately    indicates whether the rule should be executed immediately or not
	 */
	public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately);
}