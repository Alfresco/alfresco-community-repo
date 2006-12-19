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
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.RhinoScriptService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;

/**
 * Execute Script command implementation.
 * <p>
 * Executes the supplied script against the default data-model.
 * 
 * @author Kevin Roast
 */
public final class ExecuteScriptCommand implements Command
{
   public static final String PROP_SCRIPT = "script";
   public static final String PROP_DOCUMENT = "document";
   public static final String PROP_USERPERSON = "person";
   public static final String PROP_ARGS = "args";
   
   private static final String[] PROPERTIES = new String[] {PROP_SCRIPT, PROP_DOCUMENT, PROP_USERPERSON, PROP_ARGS};
   
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#getPropertyNames()
    */
   public String[] getPropertyNames()
   {
      return PROPERTIES;
   }

   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      // get the target Script node for the command
      NodeRef scriptRef = (NodeRef)properties.get(PROP_SCRIPT);
      if (scriptRef == null)
      {
         throw new IllegalArgumentException(
               "Unable to execute ExecuteScriptCommand - mandatory parameter not supplied: " + PROP_SCRIPT);
      }
      
      NodeRef personRef = (NodeRef)properties.get(PROP_USERPERSON);
      if (personRef == null)
      {
         throw new IllegalArgumentException(
               "Unable to execute ExecuteScriptCommand - mandatory parameter not supplied: " + PROP_USERPERSON);
      }
      
      // get the optional document and space context ref
      NodeService nodeService = serviceRegistry.getNodeService();
      NodeRef docRef = (NodeRef)properties.get(PROP_DOCUMENT);
      NodeRef spaceRef = null;
      if (docRef != null)
      {
         spaceRef = nodeService.getPrimaryParent(docRef).getParentRef();
      }
      
      // build the model needed to execute the script
      Map<String, Object> model = RhinoScriptService.buildDefaultModel(
            serviceRegistry,
            personRef,
            new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId()),
            (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER),
            scriptRef,
            docRef,
            spaceRef);
      
      // add the url arguments map
      model.put("args", properties.get(PROP_ARGS));
      
      // execute the script and return the result
      return serviceRegistry.getScriptService().executeScript(scriptRef, null, model);
   }
}
