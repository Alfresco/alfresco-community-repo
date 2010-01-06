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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script to GET the rule collection associated with the given rule owning node.
 * The following optional parameters can be provided:
 * 
 *      - includeInherited: if provided, then this parameter indicates whether or not to include rules 
 *          inherited from the owning node's parents. If this parameter is not provided , then rules 
 *          inherited from the node's parents are included by default.
 *      
 *      - ruleTypeName: if this parameter is provided, then only rules of this given rule type are returned.
 *          If this parameter is not provided then rules of all types will be returned.     
 * 
 * @author glen johnson at alfresco dot com
 */
public class RulesGet extends DeclarativeWebScript
{
    // private constants 
    private static final String REQ_TEMPL_VAR_STORE_TYPE = "store_type";
    private static final String REQ_TEMPL_VAR_STORE_ID = "store_id";
    private static final String REQ_TEMPL_VAR_NODE_ID = "id";
    
    private static final String REQ_PARAM_INCLUDE_INHERITED = "includeInherited";
    private static final String REQ_PARAM_RULE_TYPE_NAME = "ruleTypeName";
    
    // model property keys
    private static final String MODEL_PROP_KEY_RULES = "rules";
    private static final String MODEL_PROP_KEY_OWNING_NODE_REF = "owningNodeRef";
    
    // properties for services
    private RuleService ruleService;
    
    // properties for dependencies
    private RulesHelper rulesHelper;

    /**
     * Set the ruleService property.
     * 
     * @param ruleService The rule service instance to set
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * @param rulesHelper the rulesHelper to set
     */
    public void setRulesHelper(RulesHelper rulesHelper)
    {
        this.rulesHelper = rulesHelper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(
     *      org.alfresco.web.scripts.WebScriptRequest,
     *      org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();
        
        String storeType = req.getServiceMatch().getTemplateVars().get(REQ_TEMPL_VAR_STORE_TYPE);
        // Handle if 'store_type' URL template token not provided
        if ((storeType == null) || (storeType.length() == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "The 'store_type' URL template token has not been provided in URL");
        }                
        
        String storeId = req.getServiceMatch().getTemplateVars().get(REQ_TEMPL_VAR_STORE_ID);
        // Handle if 'storeId' URL template token not provided
        if ((storeId == null) || (storeId.length() == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "The 'storeId' URL template token has not been provided in URL");
        }                        
        
        String nodeId = req.getServiceMatch().getTemplateVars().get(REQ_TEMPL_VAR_NODE_ID);
        // Handle if 'nodeId' URL template token not provided
        if ((nodeId == null) || (nodeId.length() == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "The 'nodeId' URL template token has not been provided in URL");
        }                        
        
        //
        // get URL parameters
        //
        
        // get the 'includeInherited' parameter. The value defaults to 'true' if
        // the parameter has not been provided
        boolean includeInherited = true;
        String includeInheritedParam = req.getParameter(REQ_PARAM_INCLUDE_INHERITED);
        if ((includeInheritedParam != null) && (includeInheritedParam.length() > 0))
        {
            includeInherited = Boolean.parseBoolean(includeInheritedParam);
        }
        
        // get the 'ruleTypeName' parameter. The value defaults to 'null' if
        // the parameter has not been provided
        String ruleTypeName = null;
        String ruleTypeNameParam = req.getParameter(REQ_PARAM_RULE_TYPE_NAME);
        if ((ruleTypeNameParam != null) && (ruleTypeNameParam.length() > 0))
        {
            ruleTypeName = ruleTypeNameParam;
        }
        
        // create the rule owning node reference from the given URL template tokens
        NodeRef owningNodeRef = this.rulesHelper.getNodeRefFromWebScriptUrl(req, storeType, storeId, nodeId);
        
        // get rule collection associated with the rule owning node
        List<Rule> rules = this.ruleService.getRules(owningNodeRef, includeInherited, ruleTypeName);
        
        // add objects to model for the template to render
        model.put(MODEL_PROP_KEY_RULES, rules);
        model.put(MODEL_PROP_KEY_OWNING_NODE_REF, owningNodeRef.toString());
        
        return model;
    }        
}
