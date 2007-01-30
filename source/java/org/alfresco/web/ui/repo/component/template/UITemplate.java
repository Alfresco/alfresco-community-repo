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
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.apache.log4j.Logger;

/**
 * Component responsible for rendering the output of a FreeMarker template directly to the page.
 * <p>
 * FreeMarker templates can be specified as a NodeRef or classpath location. The template output
 * will be processed against the default model merged with any custom model reference supplied to
 * the component as a value binding attribute. The output of the template is the output of the
 * component tag.
 * 
 * @author Kevin Roast
 */
public class UITemplate extends SelfRenderingComponent
{
   private final static String ENGINE_DEFAULT = "freemarker";
   
   private static Logger logger = Logger.getLogger(UITemplate.class);
   
   /** Template engine name */
   private String engine = null;
   
   /** Template name/classpath */
   private String template = null;
   
   /** Template cm:name based path */
   private String templatePath = null;
   
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
      this.templatePath = (String)values[3];
      this.model = (Object)values[4];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[] {
         super.saveState(context), this.engine, this.template, this.templatePath, this.model};
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
      
      // get the template to process
      String templateRef = getTemplate();
      if (templateRef == null || templateRef.length() == 0)
      {
         // no noderef/classpath template found - try a name based path
         String path = getTemplatePath();
         if (path != null && path.length() != 0)
         {
            // convert cm:name based path to a NodeRef
            StringTokenizer t = new StringTokenizer(path, "/");
            int tokenCount = t.countTokens();
            String[] elements = new String[tokenCount];
            for (int i=0; i<tokenCount; i++)
            {
               elements[i] = t.nextToken();
            }
            NodeRef pathRef = BaseServlet.resolveWebDAVPath(context, elements, false);
            if (pathRef != null)
            {
               templateRef = pathRef.toString();
            }
         }
      }
      
      if (templateRef != null && templateRef.length() != 0)
      {
         // get the class name of the processor to instantiate
         String engine = getEngine();
         
         long startTime = 0;
         if (logger.isDebugEnabled())
         {
            logger.debug("Using template processor name: " + engine);
            startTime = System.currentTimeMillis();
         }
         
         // get the data model to use - building default FreeMarker model as required
         Object model = getFreeMarkerModel(getModel(), templateRef);
         
         // process the template against the model
         try
         {
            TemplateService templateService = Repository.getServiceRegistry(context).getTemplateService();
            templateService.processTemplate(engine, templateRef, model, context.getResponseWriter());
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
   
   /**
    * By default we return a Map model containing root references to the Company Home Space,
    * the users Home Space and the Person Node for the current user.
    * 
    * @param model      Custom model to merge into default model
    * @param template   Optional reference to the template to add to model
    * 
    * @return Returns the data model to bind template against.
    */
   private Object getFreeMarkerModel(Object model, String template)
   {
      if (getEngine().equals(ENGINE_DEFAULT))
      {
         // create an instance of the default FreeMarker template object model
         FacesContext fc = FacesContext.getCurrentInstance();
         ServiceRegistry services = Repository.getServiceRegistry(fc);
         User user = Application.getCurrentUser(fc);
         
         // add the template itself to the model
         NodeRef templateRef = null;
         if (template.indexOf(StoreRef.URI_FILLER) != -1)
         {
            // found a noderef template
            templateRef = new NodeRef(template);
         }
         
         Map root = DefaultModelHelper.buildDefaultModel(services, user, templateRef);
         
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
    * Return the custom data model to bind template against.
    * 
    * @return Returns the custom data model to bind template against.
    */
   public Object getModel()
   {
      if (this.model == null)
      {
         ValueBinding vb = getValueBinding("model");
         if (vb != null)
         {
            this.model = vb.getValue(getFacesContext());
         }
      }
      return this.model;
   }
   
   /**
    * @param model The model to set.
    */
   public void setModel(Object model)
   {
      this.model = model;
   }

   /**
    * @return Returns the template NodeRef/classpath.
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
    * @param template   The template NodeRef/classpath to set.
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /**
    * @return Returns the template path.
    */
   public String getTemplatePath()
   {
      ValueBinding vb = getValueBinding("templatePath");
      if (vb != null)
      {
         String val = (String)vb.getValue(getFacesContext());
         if (val != null)
         {
            this.templatePath = val.toString();
         }
      }
      
      return this.templatePath;
   }

   /**
    * @param templatePath  The template cm:name based path to set.
    */
   public void setTemplatePath(String templatePath)
   {
      this.templatePath = templatePath;
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
