/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.jscript.ScriptAction;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.UrlUtil;

/**
 * Action to execute a JavaScript. The script has access to the default model.
 * The actionedUponNodeRef is added to the default model as the 'document' and the owning
 * NodeRef is added as the 'space'.
 * 
 * @author Kevin Roast
 */
public class ScriptActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "script";
    public static final String PARAM_SCRIPTREF = "script-ref";
    
    private ServiceRegistry serviceRegistry;
    private SysAdminParams sysAdminParams;
    private PersonService personService;
    private String companyHomePath;
    private StoreRef storeRef;
    private ScriptLocation scriptLocation;

    /**
     * @param serviceRegistry       The serviceRegistry to set.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * @param sysAdminParams The sysAdminParams to set.
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * @param personService         The personService to set.
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }

    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }
    
    /**
     * Set the script location from Spring
     * 
     * @param scriptLocation    the script location
     */
    public void setScriptLocation(ScriptLocation scriptLocation)
    {
        this.scriptLocation = scriptLocation;
    }

    /**
     * Allow adhoc properties to be passed to this action
     * 
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#getAdhocPropertiesAllowed()
     */
    protected boolean getAdhocPropertiesAllowed()
    {
        return true;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        NodeService nodeService = this.serviceRegistry.getNodeService();
        if (nodeService.exists(actionedUponNodeRef))
        {
            NodeRef scriptRef = (NodeRef)action.getParameterValue(PARAM_SCRIPTREF);
            NodeRef spaceRef = this.serviceRegistry.getRuleService().getOwningNodeRef(action);
            if (spaceRef == null)
            {
                // the actionedUponNodeRef may actually be a space
                if (this.serviceRegistry.getDictionaryService().isSubClass(
                        nodeService.getType(actionedUponNodeRef), ContentModel.TYPE_FOLDER))
                {
                    spaceRef = actionedUponNodeRef;
                }
                else
                {
                    spaceRef = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
                }
            }
            
            if (this.scriptLocation != null || (scriptRef != null && nodeService.exists(scriptRef) == true))
            {
                // get the references we need to build the default scripting data-model
                String userName = this.serviceRegistry.getAuthenticationService().getCurrentUserName();
                NodeRef personRef = this.personService.getPerson(userName);
                NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
                
                // the default scripting model provides access to well known objects and searching
                // facilities - it also provides basic create/update/delete/copy/move services
                Map<String, Object> model = this.serviceRegistry.getScriptService().buildDefaultModel(
                        personRef,
                        getCompanyHome(),
                        homeSpaceRef,
                        scriptRef,
                        actionedUponNodeRef,
                        spaceRef);
                
                // Add the action to the default model
                ScriptAction scriptAction = new ScriptAction(this.serviceRegistry, action, this.actionDefinition);
                model.put("action", scriptAction);

                model.put("webApplicationContextUrl", UrlUtil.getAlfrescoUrl(sysAdminParams)); 

                Object result = null;
                if (this.scriptLocation == null)
                {
                    // execute the script against the default model
                    result = this.serviceRegistry.getScriptService().executeScript(
                        scriptRef,
                        ContentModel.PROP_CONTENT,
                        model);
                }
                else
                {
                    // execute the script at the specified script location
                    result = this.serviceRegistry.getScriptService().executeScript(this.scriptLocation, model);
                }
                
                // Set the result
                if (result != null)
                {
                	action.setParameterValue(PARAM_RESULT, (Serializable)result);
                }
            }
        }
    }
    
    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_SCRIPTREF, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_SCRIPTREF), false, "ac-scripts"));
    }
    
    /**
     * Gets the company home node
     * 
     * @return  the company home node ref
     */
    private NodeRef getCompanyHome()
    {
        NodeRef companyHomeRef;
        
        List<NodeRef> refs = this.serviceRegistry.getSearchService().selectNodes(
                this.serviceRegistry.getNodeService().getRootNode(storeRef),
                companyHomePath,
                null,
                this.serviceRegistry.getNamespaceService(),
                false);
        if (refs.size() != 1)
        {
            throw new IllegalStateException("Invalid company home path: " + companyHomePath + " - found: " + refs.size());
        }
        companyHomeRef = refs.get(0);

        return companyHomeRef;
    }
}
