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
package org.alfresco.web.ui.repo.component.template;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.config.ConfigService;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.apache.log4j.Logger;

/**
 * @author Kevin Roast
 */
public class UITemplate extends SelfRenderingComponent
{
   private final static String ENGINE_DEFAULT = "freemarker";
   private final static String TEMPLATE_KEY = "_template_";
   
   private static Logger logger = Logger.getLogger(UITemplate.class);
   
   /** Template engine name */
   private String engine = null;
   
   /** Template name/path */
   private String template = null;
   
   /** Data model reference */
   private Object model = null;
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Template";
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.engine = (String)values[1];
      this.template = (String)values[2];
      this.model = (Object)values[3];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[4];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.engine;
      values[2] = this.template;
      values[3] = this.model;
      return (values);
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      // get the data model to use - building default if required
      Object model = getModel();
      
      // get the configservice to find the appropriate processor
      ConfigService service = Application.getConfigService(context);
      ClientConfigElement clientConfig = (ClientConfigElement)service.getGlobalConfig().getConfigElement(
            ClientConfigElement.CONFIG_ELEMENT_ID);
      
      // get the template to process
      String template = getTemplate();
      if (template != null && template.length() != 0)
      {
         // get the class name of the processor to instantiate
         String engine = getEngine();
         
         long startTime = 0;
         if (logger.isDebugEnabled())
         {
            logger.debug("Using template processor name: " + engine);
            startTime = System.currentTimeMillis();
         }
         
         // process the template against the model
         try
         {
            TemplateService templateService = Repository.getServiceRegistry(context).getTemplateService();
            templateService.processTemplate(engine, getTemplate(), model, context.getResponseWriter());
         }
         catch (TemplateException err)
         {
            Utils.addErrorMessage(err.getMessage(), err);
         }
         
         if (logger.isDebugEnabled())
         {
            long endTime = System.currentTimeMillis();
            logger.debug("Time to process template: " + (endTime - startTime) + "ms");
         }
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors 

   /**
    * @return the name of the template engine to use.
    */
   public String getEngine()
   {
      ValueBinding vb = getValueBinding("engine");
      if (vb != null)
      {
         this.engine = (String)vb.getValue(getFacesContext());
      }
      
      return this.engine != null ? this.engine : ENGINE_DEFAULT;
   }
   
   /**
    * @param engine      the name of the template engine to use. A default is provided if none is set.
    */
   public void setEngine(String engine)
   {
      this.engine = engine;
   }

   /**
    * Return the data model to bind template against.
    * <p>
    * By default we return a Map model containing root references to the Company Home Space,
    * the users Home Space and the Person Node for the current user.
    * 
    * @return Returns the data model to bind template against.
    */
   public Object getModel()
   {
      if (this.model == null)
      {
         Object model = null;
         ValueBinding vb = getValueBinding("model");
         if (vb != null)
         {
            model = vb.getValue(getFacesContext());
         }
         
         if (getEngine().equals(ENGINE_DEFAULT))
         {
            // create FreeMarker default model and merge
            Map root = new HashMap(11, 1.0f);
            
            FacesContext context = FacesContext.getCurrentInstance();
            ServiceRegistry services = Repository.getServiceRegistry(context);
            
            // supply the CompanyHome space as "companyhome"
            NodeRef companyRootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
            TemplateNode companyRootNode = new TemplateNode(companyRootRef, services, imageResolver);
            root.put("companyhome", companyRootNode);
            
            // supply the users Home Space as "userhome"
            User user = Application.getCurrentUser(context);
            NodeRef userRootRef = new NodeRef(Repository.getStoreRef(), user.getHomeSpaceId());
            TemplateNode userRootNode = new TemplateNode(userRootRef, services, imageResolver);
            root.put("userhome", userRootNode);
            
            // supply the current user Node as "person"
            root.put("person", new TemplateNode(user.getPerson(), services, imageResolver));
            
            // current date/time is useful to have and isn't supplied by FreeMarker by default
            root.put("date", new Date());
            
            // add custom method objects
            root.put("hasAspect", new HasAspectMethod());
            root.put("message", new I18NMessageMethod());
            root.put("dateCompare", new DateCompareMethod());
            
            // merge models
            if (model instanceof Map)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Found valid Map model to merge with FreeMarker: " + model);
               
               root.putAll((Map)model);
            }
            
            model = root;
         }
         
         return model;
      }
      else
      {
         return this.model;
      }
   }

   /**
    * @param model The model to set.
    */
   public void setModel(Object model)
   {
      this.model = model;
   }

   /**
    * @return Returns the template name.
    */
   public String getTemplate()
   {
      ValueBinding vb = getValueBinding("template");
      if (vb != null)
      {
         // convert object to string - then we can handle either a path, NodeRef instance or noderef string
         Object val = vb.getValue(getFacesContext());
         if (val != null)
         {
            this.template = val.toString();
         }
      }
      
      return this.template;
   }

   /**
    * @param template The template name to set.
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /** Template Image resolver helper */
   private TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
       public String resolveImagePathForName(String filename, boolean small)
       {
           return Utils.getFileTypeImage(filename, small);
       }
   };
}
