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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.action;


/**
 * Rule condition interface
 * 
 * @author Roy Wetherall
 */
public interface ActionCondition extends ParameterizedItem
{
	/**
	 * Get the action condition definition name
	 * 
	 * @param	the action condition definition name
	 */
    public String getActionConditionDefinitionName();
    
    /**
     * Set whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the
     * result.
     * <p>
     * The default value is false.
     * 
     * @param invertCondition   true indicates that the result of the condition
     *                          is inverted, false otherwise.
     */
    public void setInvertCondition(boolean invertCondition);
    
    /**
     * Indicates whether the condition result should be inverted.
     * <p>
     * This is achieved by applying the NOT logical operator to the result.
     * <p>
     * The default value is false.
     * 
     * @return  true indicates that the result of the condition is inverted, false 
     *          otherwise
     */
    public boolean getInvertCondition();
}
