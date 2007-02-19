/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.action;

import java.util.List;

import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Rule action implementation class
 * 
 * @author Roy Wetherall
 */
public class ActionDefinitionImpl extends ParameterizedItemDefinitionImpl
                            implements ActionDefinition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4048797883396863026L;    
    
    /**
     * The rule action executor
     */
    private String ruleActionExecutor;
    
    /** List of applicable types */
    private List<QName> applicableTypes;

    /**
     * Constructor
     * 
     * @param name  the name
     */
    public ActionDefinitionImpl(String name)
    {
        super(name);
    }
        
    /**
     * Set the rule action executor
     * 
     * @param ruleActionExecutor    the rule action executor
     */
    public void setRuleActionExecutor(String ruleActionExecutor)
    {
        this.ruleActionExecutor = ruleActionExecutor;
    }
    
    /**
     * Get the rule aciton executor
     * 
     * @return  the rule action executor
     */
    public String getRuleActionExecutor()
    {
        return ruleActionExecutor;
    }
    
    /**
     * Gets the list of applicable types
     * 
     * @return  the list of qnames
     */
    public List<QName> getApplicableTypes()
    {
        return this.applicableTypes;
    }
    
    /**
     * Sets the list of applicable types
     * 
     * @param applicableTypes   the applicable types
     */
    public void setApplicableTypes(List<QName> applicableTypes)
    {
        this.applicableTypes = applicableTypes;
    }
}
