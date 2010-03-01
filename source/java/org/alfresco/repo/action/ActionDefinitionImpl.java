/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
