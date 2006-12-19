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

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.jscript.RhinoScriptService;
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
                spaceRef = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
            }
            
            if (this.scriptLocation != null || (scriptRef != null && nodeService.exists(scriptRef) == true))
            {
                // get the references we need to build the default scripting data-model
                String userName = this.serviceRegistry.getAuthenticationService().getCurrentUserName();
                NodeRef personRef = this.personService.getPerson(userName);
                NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
                
                // the default scripting model provides access to well known objects and searching
                // facilities - it also provides basic create/update/delete/copy/move services
                Map<String, Object> model = RhinoScriptService.buildDefaultModel(
                        this.serviceRegistry,
                        personRef,
                        getCompanyHome(),
                        homeSpaceRef,
                        scriptRef,
                        actionedUponNodeRef,
                        spaceRef);
                
                // Add the action to the default model
                ScriptAction scriptAction = new ScriptAction(this.serviceRegistry, action, this.actionDefinition);
                model.put("action", scriptAction);
                
                if (this.scriptLocation == null)
                {
                    // execute the script against the default model
                    this.serviceRegistry.getScriptService().executeScript(
                        scriptRef,
                        ContentModel.PROP_CONTENT,
                        model);
                }
                else
                {
                    // execute the script at the specified script location
                    this.serviceRegistry.getScriptService().executeScript(this.scriptLocation, model);
                }
            }
        }
    }
    
    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_SCRIPTREF, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_SCRIPTREF)));
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
