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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;

/**
 * @author Kevin Roast
 */
public class ScriptActionExecutor extends ActionExecuterAbstractBase
{
    public static final String NAME = "script";
    public static final String PARAM_SCRIPTREF = "script-ref";
    public static final String PARAM_SPACEREF = "space-ref";
    
    private ServiceRegistry serviceRegistry;
    private PersonService personService;
    private String companyHomePath;
    private StoreRef storeRef;
    
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
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        NodeService nodeService = this.serviceRegistry.getNodeService();
        if (nodeService.exists(actionedUponNodeRef))
        {
            NodeRef scriptRef = (NodeRef)action.getParameterValue(PARAM_SCRIPTREF);
            NodeRef spaceRef = (NodeRef)action.getParameterValue(PARAM_SPACEREF);
            if (spaceRef == null)
            {
                // get primary parent of the doc as no space has been specified
                spaceRef = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
            }
            
            if (nodeService.exists(scriptRef))
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
                        actionedUponNodeRef,
                        spaceRef);
                
                // execute the script against the default model
                this.serviceRegistry.getScriptService().executeScript(
                        scriptRef,
                        ContentModel.PROP_CONTENT,
                        model);
            }
        }
    }
    
    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_SCRIPTREF, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_SCRIPTREF)));
        paramList.add(new ParameterDefinitionImpl(PARAM_SPACEREF, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_SPACEREF)));
    }
    
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
