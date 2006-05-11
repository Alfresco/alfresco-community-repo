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

import java.util.HashMap;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.config.element.GenericConfigElement;

/**
 * Custom config element that represents the config data for navigation
 * 
 * @author gavinc
 */
public class NavigationConfigElement extends ConfigElementAdapter
{
   private HashMap<String, NavigationResult> viewIds = new HashMap<String, NavigationResult>();
   private HashMap<String, NavigationResult> outcomes = new HashMap<String, NavigationResult>();
   
   private boolean kidsPopulated = false;
   
   /**
    * Default constructor
    */
   public NavigationConfigElement()
   {
      super("navigation");
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public NavigationConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      // lazily build the list of generic config elements representing
      // the navigation overrides as the caller may not even call this method
      
      List<ConfigElement> kids = null;
      
      if (this.viewIds.size() > 0 || this.outcomes.size() > 0)
      {
         if (this.kidsPopulated == false)
         {
            // create generic config elements for the from-view-id items 
            for (String fromViewId : this.viewIds.keySet())
            {
               GenericConfigElement ce = new GenericConfigElement(NavigationElementReader.ELEMENT_OVERRIDE);
               ce.addAttribute(NavigationElementReader.ATTR_FROM_VIEWID, fromViewId);

               NavigationResult navRes = this.viewIds.get(fromViewId);
               String result = navRes.getResult();
               if (navRes.isOutcome())
               {
                  ce.addAttribute(NavigationElementReader.ATTR_TO_OUTCOME, result);
               }
               else
               {
                  ce.addAttribute(NavigationElementReader.ATTR_TO_VIEWID, result);
               }
               
               // add the element
               this.children.add(ce);
            }
            
            // create generic config elements for the from-outcome items 
            for (String fromOutcome : this.outcomes.keySet())
            {
               GenericConfigElement ce = new GenericConfigElement(NavigationElementReader.ELEMENT_OVERRIDE);
               ce.addAttribute(NavigationElementReader.ATTR_FROM_OUTCOME, fromOutcome);

               NavigationResult navRes = this.outcomes.get(fromOutcome);
               String result = navRes.getResult();
               if (navRes.isOutcome())
               {
                  ce.addAttribute(NavigationElementReader.ATTR_TO_OUTCOME, result);
               }
               else
               {
                  ce.addAttribute(NavigationElementReader.ATTR_TO_VIEWID, result);
               }
               
               // add the element
               this.children.add(ce);
            }
            
            this.kidsPopulated = true;
         }
         
         kids = super.getChildren();
      }
      
      return kids;
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      NavigationConfigElement newElement = (NavigationConfigElement)configElement;
      NavigationConfigElement combinedElement = new NavigationConfigElement();

      // add all the existing from view id overrides
      for (String fromViewId : this.viewIds.keySet())
      {
         combinedElement.addOverride(fromViewId, null, this.viewIds.get(fromViewId));
      }
      
      // add all the existing from outcome overrides
      for (String fromOutcome : this.outcomes.keySet())
      {
         combinedElement.addOverride(null, fromOutcome, this.outcomes.get(fromOutcome));
      }
      
      // add all the from view id overrides from the given element
      HashMap<String, NavigationResult> viewIds = newElement.getViewIds(); 
      for (String fromViewId : viewIds.keySet())
      {
         combinedElement.addOverride(fromViewId, null, viewIds.get(fromViewId));
      }
      
      // add all the from outcome overrides from the given element
      HashMap<String, NavigationResult> outcomes = newElement.getOutcomes();
      for (String fromOutcome : outcomes.keySet())
      {
         combinedElement.addOverride(null, fromOutcome, outcomes.get(fromOutcome));
      }
      
      return combinedElement;
   }
   
   /**
    * Returns the list of view ids that have overrides defined
    * 
    * @return Map of view ids and navigation results
    */
   public HashMap<String, NavigationResult> getViewIds()
   {
      return this.viewIds;
   }
   
   /**
    * Returns the list of outcomes that have overrides defined
    * 
    * @return Map of outcomes and navigation results
    */
   public HashMap<String, NavigationResult> getOutcomes()
   {
      return this.outcomes;
   }
   
   /**
    * Adds an override configuration item
    * 
    * @param fromViewId The from-view-id value from the config
    * @param fromOutcome The from-outcome value from the config
    * @param toViewId The to-view-id value from the config
    * @param toOutcome The to-outcome value from the config
    */
   public void addOverride(String fromViewId, String fromOutcome, 
         String toViewId, String toOutcome)
   {
      // NOTE: the constructor will check the validity of the to* parameters
      NavigationResult result = new NavigationResult(toViewId, toOutcome);
      addOverride(fromViewId, fromOutcome, result);
   }
   
   /**
    * Adds an override configuration item
    * 
    * @param fromViewId The from-view-id value from the config
    * @param fromOutcome The from-outcome value from the config
    * @param result The navigation result object to add
    */
   public void addOverride(String fromViewId, String fromOutcome,
         NavigationResult result)
   {
      if (fromViewId != null && fromOutcome != null)
      {
         throw new IllegalStateException("You can not have both a from-view-id and from-outcome");
      }
      
      if (fromViewId != null)
      {
         this.viewIds.put(fromViewId, result);
      }
      else if (fromOutcome != null)
      {
         this.outcomes.put(fromOutcome, result);
      }
   }
   
   /**
    * Returns the best match navigation override configured for the given
    * current view id and/or outcome.
    * 
    * If an outcome is passed it takes precedence, the view id will not be
    * used.
    * 
    * @param fromViewId The current view id 
    * @param fromOutcome The current outcome 
    * @return The navigation result
    */
   public NavigationResult getOverride(String fromViewId, String fromOutcome)
   {
      NavigationResult result = null;
      
      // look for a match for the outcome if one was provided
      if (fromOutcome != null)
      {
         result = this.outcomes.get(fromOutcome);
      }
      else if (fromViewId != null)
      {
         result = this.viewIds.get(fromViewId);
      }
      
      return result;
   }
}
