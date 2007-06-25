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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.scripts.jsf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.web.scripts.DeclarativeWebScriptRegistry;
import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptRegistry;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.WebScriptRuntime;
import org.alfresco.web.scripts.WebScriptURLRequest;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * JSF Component implementation for the WebScript component.
 * <p>
 * Responsible for generating a JSF Component specific WebScriptRuntime instance and
 * executing the specified WebScript against the runtime. 
 * 
 * @author Kevin Roast
 */
public class UIWebScript extends SelfRenderingComponent
{
   private static Log logger = LogFactory.getLog(UIWebScript.class);
   
   /** WebScript URL to execute */
   private String scriptUrl = null;
   
   /** User defined script context value */
   private Object context = null;
   
   private boolean scriptUrlModified = false;
   
   private WebScriptRegistry registry;
   private ServiceRegistry serviceRegistry;;
   
   /**
    * Default constructor
    */
   public UIWebScript()
   {
      WebApplicationContext ctx = FacesContextUtils.getRequiredWebApplicationContext(
            FacesContext.getCurrentInstance());
      this.registry = (DeclarativeWebScriptRegistry)ctx.getBean("webscripts.registry");
      this.serviceRegistry = (ServiceRegistry)ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.Controls";
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.scriptUrl = (String)values[1];
      this.scriptUrlModified = (Boolean)values[2];
      this.context = values[3];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[] {
         super.saveState(context), this.scriptUrl, this.scriptUrlModified, this.context};
      return values;
   }

   /* (non-Javadoc)
    * @see javax.faces.component.UIComponentBase#broadcast(javax.faces.event.FacesEvent)
    */
   @Override
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof WebScriptEvent)
      {
         this.scriptUrlModified = true;
         this.scriptUrl = ((WebScriptEvent)event).Url;
      }
      else
      {
         super.broadcast(event);
      }
   }

   /* (non-Javadoc)
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   @Override
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = this.getClientId(context);
      String value = (String)requestMap.get(fieldId);
      if (value != null && value.length() != 0)
      {
         // found web-script URL for this component
         try
         {
            String url = URLDecoder.decode(value, "UTF-8");
            queueEvent(new WebScriptEvent(this, url));
         }
         catch (UnsupportedEncodingException e)
         {
            throw new AlfrescoRuntimeException("Unable to decode utf-8 script url.");
         }
      }
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      String scriptUrl = getScriptUrl();
      
      Object scriptContext = getContext();
      if (scriptContext != null)
      {
         // context object supplied, perform simple variable substitution
         if (scriptContext instanceof Map)
         {
            Map<String, Object> scriptContextMap = (Map<String, Object>)scriptContext;
            for (String key : scriptContextMap.keySet())
            {
               scriptUrl = scriptUrl.replace(key, scriptContextMap.get(key).toString());
            }
         }
         else
         {
            // currently we only support {noderef} replacement directly
            // TODO: move the variable substitution into the WebScript engine - pass in
            //       a bag of context objects i.e. name/value pairs of well known keys
            //       allow common values such as noderef, nodeid, path, user etc.
            scriptUrl = scriptUrl.replace("{noderef}", scriptContext.toString());
         }
      }
      
      // execute WebScript
      if (logger.isDebugEnabled())
         logger.debug("Processing UIWebScript encodeBegin(): " + scriptUrl);
      
      WebScriptRuntime runtime = new WebScriptJSFRuntime(context, scriptUrl);
      runtime.executeScript();
   }
   
   /**
    * Set the scriptUrl
    *
    * @param scriptUrl     the scriptUrl
    */
   public void setScriptUrl(String scriptUrl)
   {
      this.scriptUrl = getFacesContext().getExternalContext().getRequestContextPath() + scriptUrl;
   }

   /**
    * @return the scriptUrl
    */
   public String getScriptUrl()
   {
      if (this.scriptUrlModified == false)
      {
         ValueBinding vb = getValueBinding("scriptUrl");
         if (vb != null)
         {
            this.scriptUrl = getFacesContext().getExternalContext().getRequestContextPath() +
                             (String)vb.getValue(getFacesContext());
         }
      }
      return this.scriptUrl;
   }
   
   /**
    * @return the user defined script context object
    */
   public Object getContext()
   {
      ValueBinding vb = getValueBinding("context");
      if (vb != null)
      {
         this.context = vb.getValue(getFacesContext());
      }
      return this.context;
   }

   /**
    * @param context the user defined script context to set
    */
   public void setContext(Object context)
   {
      this.context = context;
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the clicking of a webscript url action.
    */
   public static class WebScriptEvent extends ActionEvent
   {
      public WebScriptEvent(UIComponent component, String url)
      {
         super(component);
         this.Url = url;
      }
      
      public String Url = null;
   }
   
   /**
    * Implementation of a WebScriptRuntime for the JSF environment
    * 
    * @author Kevin Roast
    */
   private class WebScriptJSFRuntime extends WebScriptRuntime
   {
      private FacesContext fc;
      private String scriptUrl;
      private String script;
      
      WebScriptJSFRuntime(FacesContext fc, String scriptUrl)
      {
         super(registry, serviceRegistry);
         this.fc = fc;
         this.scriptUrl = scriptUrl;
         this.script = WebScriptURLRequest.splitURL(scriptUrl)[2];
      }

      /**
       * @see org.alfresco.web.scripts.WebScriptRuntime#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean)
       */
      @Override
      protected boolean authenticate(RequiredAuthentication required, boolean isGuest)
      {
         // JSF component already in an authenticated environment as the
         // /faces servlet filter (or JSF portlet wrapper) is called first
         return true;
      }

      /**
       * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
       */
      @Override
      protected WebScriptRequest createRequest(WebScriptMatch match)
      {
         return new WebScriptJSFRequest(this.scriptUrl, match);
      }

      /**
       * @see org.alfresco.web.scripts.WebScriptRuntime#createResponse()
       */
      @Override
      protected WebScriptResponse createResponse()
      {
         return new WebScriptJSFResponse(fc, UIWebScript.this);
      }

      /**
       * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptMethod()
       */
      @Override
      protected String getScriptMethod()
      {
         return "GET";
      }

      /**
       * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptUrl()
       */
      @Override
      protected String getScriptUrl()
      {
         return this.script;
      }
   }
}
