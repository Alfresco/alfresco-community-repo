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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.web.action.ActionEvaluator;

/**
 * Action config element.
 * 
 * @author Kevin Roast
 */
public class ActionsConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "actions";
   
   private Map<String, ActionDefinition> actionDefs = new HashMap<String, ActionDefinition>(32, 1.0f);
   private Map<String, ActionGroup> actionGroups = new HashMap<String, ActionGroup>(16, 1.0f);
   
   /**
    * Default constructor
    */
   public ActionsConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * @param name
    */
   public ActionsConfigElement(String name)
   {
      super(name);
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the Actions config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      ActionsConfigElement existingElement = (ActionsConfigElement)configElement;
      ActionsConfigElement combinedElement = new ActionsConfigElement();
      
      //
      // TODO: implement to allow override of config elements
      //
      
      return null;
   }
   
   /*package*/ void addActionDefinition(ActionDefinition actionDef)
   {
      actionDefs.put(actionDef.getId(), actionDef);
   }
   
   public ActionDefinition getActionDefinition(String id)
   {
      return actionDefs.get(id);
   }
   
   /*package*/ void addActionGroup(ActionGroup group)
   {
      actionGroups.put(group.getId(), group);
   }
   
   public ActionGroup getActionGroup(String id)
   {
      return actionGroups.get(id);
   }
   
   
   /**
    * Simple class representing the definition of a UI action.
    * 
    * @author Kevin Roast
    */
   public static class ActionDefinition
   {
      public ActionDefinition(String id)
      {
         if (id == null || id.length() == 0)
         {
            throw new IllegalArgumentException("ActionDefinition ID is mandatory.");
         }
         this.id = id;
      }
      
      public String getId()
      {
         return id;
      }
      
      public void addAllowPermission(String permission)
      {
         if (permissionAllow == null)
         {
            permissionAllow = new ArrayList<String>(2);
         }
         permissionAllow.add(permission);
      }
      
      public void addDenyPermission(String permission)
      {
         if (permissionDeny == null)
         {
            permissionDeny = new ArrayList<String>(1);
         }
         permissionDeny.add(permission);
      }
      
      public List<String> getAllowPermissions()
      {
         return permissionAllow;
      }
      
      public List<String> getDenyPermissions()
      {
         return permissionDeny;
      }
      
      public void addParam(String name, String value)
      {
         if (params == null)
         {
            params = new HashMap<String, String>(1, 1.0f);
         }
         params.put(name, value);
      }
      
      public Map<String, String> getParams()
      {
         return params;
      }
      
      private String id;
      private List<String> permissionAllow = null;
      private List<String> permissionDeny = null;
      private Map<String, String> params = null;
      
      public ActionEvaluator Evaluator = null;
      public String Label;
      public String LabelMsg;
      public String Tooltip;
      public String TooltipMsg;
      public boolean ShowLink;
      public String Style;
      public String StyleClass;
      public String Image;
      public String ActionListener;
      public String Action;
      public String Href;
      public String Target;
   }
   
   
   /**
    * Simple class representing a group of UI actions.
    * 
    * @author Kevin Roast
    */
   public static class ActionGroup implements Iterable<ActionDefinition>
   {
      public ActionGroup(String id)
      {
         if (id == null || id.length() == 0)
         {
            throw new IllegalArgumentException("ActionGroup ID is mandatory.");
         }
         this.id = id;
      }
      
      public String getId()
      {
         return id;
      }
      
      public void addAction(ActionDefinition actionDef)
      {
         actions.put(actionDef.getId(), actionDef);
      }
      
      /**
       * @return Iterator to the ActionDefinition objects referenced by this group 
       */
      public Iterator<ActionDefinition> iterator()
      {
         return actions.values().iterator();
      }
      
      private String id;
      
      /** the action definitions, we use a linked hashmap to ensure we do not have more 
          than one action with the same Id and that the insertion order is preserved */
      private Map<String, ActionDefinition> actions = new LinkedHashMap(8, 1.0f);
      
      public boolean ShowLink;
      public String Style;
      public String StyleClass;
   }
}
