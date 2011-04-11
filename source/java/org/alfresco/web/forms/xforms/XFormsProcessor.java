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
package org.alfresco.web.forms.xforms;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.faces.context.FacesContext;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.xml.ns.NamespaceConstants;
import org.chiba.xml.xforms.exception.XFormsException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.JavaScriptUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XFormsProcessor implements FormProcessor
{

   private static final Log LOGGER = LogFactory.getLog(XFormsProcessor.class); 

   /**
    * A triple of js variable name, namespace uri, and namespace prefix which
    * will form javascript variables within alfresco.constants.
    */
   private final static String[][] JS_NAMESPACES = 
   {
      { "xforms", NamespaceConstants.XFORMS_NS, NamespaceConstants.XFORMS_PREFIX },
      { "xhtml", NamespaceConstants.XHTML_NS, NamespaceConstants.XHTML_PREFIX },
      { "chiba", NamespaceConstants.CHIBA_NS, NamespaceConstants.CHIBA_PREFIX },
      { "alfresco", NamespaceService.ALFRESCO_URI, NamespaceService.ALFRESCO_PREFIX }
   };
   
   /** Scripts needed to initialize the xforms client. */
   private final static String[] JS_SCRIPTS = 
   {
      "/scripts/tiny_mce/" + (LOGGER.isDebugEnabled() 
                              ? "tiny_mce_src.js" 
                              : "tiny_mce.js"),
      "/scripts/ajax/dojo/" + (LOGGER.isDebugEnabled() 
                               ? "dojo.js.uncompressed.js" 
                               : "dojo.js"),
      "/scripts/ajax/mootools.v1.11.js",
      "/scripts/ajax/common.js",
      "/scripts/ajax/ajax_helper.js",
      "/scripts/ajax/tiny_mce_wcm_extensions.js",
      "/scripts/ajax/xforms.js",
      "/scripts/ajax/file_picker_widget.js",
      "/scripts/upload_helper.js",
      "/scripts/ajax/dojo/src/date/serialize.js"
   };

   /** Localized strings needed by the xforms client. */
   private final static String[] BUNDLE_KEYS =
   {
      "add_content",
      "cancel",
      "change",
      "click_to_edit",
      "eg",
      "go_up",
      "idle",
      "loading",
      "path",
      "select",
      "upload",
      "validation_provide_values_for_required_fields"
   };

   private static JSONObject widgetConfig = null;

   public XFormsProcessor()
   {
      if (XFormsProcessor.widgetConfig == null)
      {
         XFormsProcessor.widgetConfig = XFormsProcessor.loadConfig();
      }
   }

   public Session process(final Document instanceDataDocument,
                          final String formInstanceDataName,
                          final Form form,
                          final Writer out)
      throws FormProcessor.ProcessingException
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      //make the XFormsBean available for this session
      final XFormsBean xforms = (XFormsBean)
         FacesHelper.getManagedBean(fc, XFormsBean.BEAN_NAME);
      final Session result = 
         xforms.createSession(instanceDataDocument, formInstanceDataName, form);
      this.process(result, out);
      return result;
   }

   /**
    * Generates html text which bootstraps the JavaScript code that will
    * call back into the XFormsBean and get the xform and build the ui.
    */
   public void process(final Session session, final Writer out)
      throws FormProcessor.ProcessingException
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      //make the XFormsBean available for this session
      final XFormsBean xforms = (XFormsBean)
         FacesHelper.getManagedBean(fc, XFormsBean.BEAN_NAME);
      final AVMBrowseBean avmBrowseBean = (AVMBrowseBean)
         FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);
      try
      {
         xforms.setXFormsSession((XFormsBean.XFormsSession)session);
      }
      catch (FormBuilderException fbe)
      {
         LOGGER.error(fbe);
         throw new ProcessingException(fbe);
      }
      catch (XFormsException xfe)
      {
         LOGGER.error(xfe);
         throw new ProcessingException(xfe);
      }
 
      final String contextPath = fc.getExternalContext().getRequestContextPath();
      final Document result = XMLUtil.newDocument();
      final String xformsUIDivId = "alfresco-xforms-ui";

      // this div is where the ui will write to
      final Element div = result.createElement("div");
      div.setAttribute("id", xformsUIDivId);
      result.appendChild(div);
      
      Element e = result.createElement("link");
      e.setAttribute("rel", "stylesheet");
      e.setAttribute("type", "text/css");
      e.setAttribute("href", contextPath + "/css/xforms.css");
      div.appendChild(e);      

      // a script with config information and globals.
      e = result.createElement("script");
      e.setAttribute("type", "text/javascript");
      final StringBuilder js = new StringBuilder("\n");
      final String[] jsNamespacesObjects = 
      {
         "alfresco", 
         "alfresco.constants", 
         "alfresco.xforms", 
         "alfresco.xforms.constants" 
      };
      for (final String jsNamespace : jsNamespacesObjects)
      {
         js.append(jsNamespace).
            append(" = typeof ").
            append(jsNamespace).
            append(" == 'undefined' ? {} : ").
            append(jsNamespace).
            append(";\n");
      }
      js.append("alfresco.constants.DEBUG = ").
         append(LOGGER.isDebugEnabled()).
         append(";\n");
      js.append("alfresco.constants.WEBAPP_CONTEXT = '").
         append(JavaScriptUtils.javaScriptEscape(contextPath)).
         append("';\n");
      
      String avmWebApp = avmBrowseBean.getWebapp();
      
      // TODO - need better way to determine WCM vs ECM context
      js.append("alfresco.constants.AVM_WEBAPP_CONTEXT = '");
      if (avmWebApp != null)
      {
         js.append(JavaScriptUtils.javaScriptEscape(avmWebApp));
      }
      js.append("';\n");
      
      // TODO - need better way to determine WCM vs ECM context
      js.append("alfresco.constants.AVM_WEBAPP_URL = '");
      if (avmWebApp != null)
      {
         //Use preview store because when user upload image it appears in preview, not in main store.
         String storeName = AVMUtil.getCorrespondingPreviewStoreName(avmBrowseBean.getSandbox());
         if (storeName != null)
         {
            js.append(JavaScriptUtils.javaScriptEscape(fc.getExternalContext().getRequestContextPath() + "/wcs/api/path/content/avm/" +
                      AVMUtil.buildStoreWebappPath(storeName, avmWebApp).replace(":","")));
         }
      }
	  
      js.append("';\n");

      js.append("alfresco.constants.AVM_WEBAPP_PREFIX = '");
      if (avmWebApp != null)
      {
         String storeName = AVMUtil.getCorrespondingPreviewStoreName(avmBrowseBean.getSandbox());
         if (storeName != null)
         {
            js.append(JavaScriptUtils.javaScriptEscape(fc.getExternalContext().getRequestContextPath() + "/wcs/api/path/content/avm/" +
                      AVMUtil.buildSandboxRootPath(storeName).replace(":","")));
         }
      }
         
      js.append("';\n");
      js.append("alfresco.xforms.constants.XFORMS_UI_DIV_ID = '").
         append(xformsUIDivId).
         append("';\n");
      js.append("alfresco.xforms.constants.FORM_INSTANCE_DATA_NAME = '").
         append(JavaScriptUtils.javaScriptEscape(session.getFormInstanceDataName())).
         append("';\n");
      SimpleDateFormat sdf = (SimpleDateFormat)
         SimpleDateFormat.getDateInstance(DateFormat.SHORT, 
                                          Application.getLanguage(fc));
      js.append("alfresco.xforms.constants.DATE_FORMAT = '").
         append(sdf.toPattern()).
         append("';\n");
      sdf = (SimpleDateFormat)
         SimpleDateFormat.getTimeInstance(DateFormat.SHORT, 
                                          Application.getLanguage(fc));
      js.append("alfresco.xforms.constants.TIME_FORMAT = '").
         append(sdf.toPattern()).
         append("';\n");
      sdf = (SimpleDateFormat)
         SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, 
                                              DateFormat.SHORT, 
                                              Application.getLanguage(fc));
      js.append("alfresco.xforms.constants.DATE_TIME_FORMAT = '").
         append(sdf.toPattern()).
         append("';\n");
      for (String[] ns : JS_NAMESPACES)
      {
         js.append("alfresco.xforms.constants.").
            append(ns[0].toUpperCase()).
            append("_NS = '").append(ns[1]).append("';\n");
         js.append("alfresco.xforms.constants.").
            append(ns[0].toUpperCase()). 
            append("_PREFIX = '").append(ns[2]).append("';\n");
      }

      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      js.append("alfresco.resources = {\n");
      for (String k : BUNDLE_KEYS)
      {
         js.append(k).
            append(": '").
            append(JavaScriptUtils.javaScriptEscape(bundle.getString(k))).
            append("'").
            append(k.equals(BUNDLE_KEYS[BUNDLE_KEYS.length - 1]) ? "\n};" : ",").
            append("\n");
      }

      try
      {
         js.append("alfresco.xforms.widgetConfig = \n").
            append(LOGGER.isDebugEnabled() 
                   ? XFormsProcessor.widgetConfig.toString(0) 
                   : XFormsProcessor.widgetConfig).
            append("\n");
      }
      catch (JSONException jsone)
      {
         LOGGER.error(jsone);
      }
      e.appendChild(result.createTextNode(js.toString()));

      div.appendChild(e);
	    
      // include all our scripts, order is significant
      for (final String script : JS_SCRIPTS)
      {
         if (script == null)
         {
            continue;
         }
         e = result.createElement("script");
         e.setAttribute("type", "text/javascript");
         e.setAttribute("src", contextPath + script);
         e.appendChild(result.createTextNode("\n"));
         div.appendChild(e);
      }
      
      // output any custom scripts
      ConfigElement config = Application.getConfigService(fc).getGlobalConfig().getConfigElement("wcm");
      if (config != null)
      {
         // get the custom scripts to include
         ConfigElement xformsScriptsConfig = config.getChild("xforms-scripts");
         if (xformsScriptsConfig != null)
         {
            StringTokenizer t = new StringTokenizer(xformsScriptsConfig.getValue().trim(), ", ");
            while (t.hasMoreTokens())
            {
               e = result.createElement("script");
               e.setAttribute("type", "text/javascript");
               e.setAttribute("src", contextPath + t.nextToken());
               e.appendChild(result.createTextNode("\n"));
               div.appendChild(e);
            }
         }
      }
 
      XMLUtil.print(result, out);
   }
   
   private static JSONObject loadConfig()
   {
      final ConfigService cfgService = Application.getConfigService(FacesContext.getCurrentInstance());
      final ConfigElement xformsConfig = cfgService.getGlobalConfig().getConfigElement("wcm").getChild("xforms");
      final List<ConfigElement> widgetConfig = xformsConfig.getChildren("widget");

      class WidgetConfigElement
         implements Comparable<WidgetConfigElement>
      {
         public final String xformsType;
         public final String xmlSchemaType;
         public final String appearance;
         public final String javascriptClassName;
         private final Map<String, String> params = new HashMap<String, String>();

         public WidgetConfigElement(final String xformsType,
                                    final String xmlSchemaType,
                                    final String appearance,
                                    final String javascriptClassName)
         {
            if (xformsType == null)
            {
               throw new NullPointerException();
            }
            this.xformsType = xformsType;
            this.xmlSchemaType = xmlSchemaType;
            this.appearance = appearance;
            this.javascriptClassName = javascriptClassName;
         }

         public void addParam(final String k, final String v)
         {
            this.params.put(k, v);
         }

         public Map<String, String> getParams()
         {
            return (this.params == null 
                    ? (Map<String, String>)Collections.EMPTY_MAP
                    : Collections.unmodifiableMap(this.params));
         }

         public int compareTo(final WidgetConfigElement other)
         {
            int result = this.xformsType.compareTo(other.xformsType);
            if (result != 0)
            {
               return result;
            }
            
            result = this.compareAttribute(this.xmlSchemaType, other.xmlSchemaType);
            if (result != 0)
            {
               return result;
            }
            result = this.compareAttribute(this.appearance, other.appearance);
            if (result != 0)
            {
               return result;
            }
            throw new RuntimeException("widget definitions " + this + 
                                       " and " + other + " collide");
         }

         public String toString()
         {
            return (this.getClass().getName() + "{" +
                    "xformsType: "+ this.xformsType +
                    ", xmlSchemaType: " + this.xmlSchemaType +
                    ", appearance: " + this.appearance +
                    ", javascriptClassName: " + this.javascriptClassName +
                    ", numParams: " + this.getParams().size() +
                    "}");
         }

         private int compareAttribute(final String s1, final String s2)
         {
            return (s1 != null && s2 == null
                    ? 1
                    : (s1 == null && s2 != null
                       ? -1
                       : (s1 != null && s2 != null
                          ? s1.compareTo(s2)
                          : 0)));
         }
      }

      final TreeSet<WidgetConfigElement> widgetConfigs = new TreeSet<WidgetConfigElement>();
      for (final ConfigElement ce : widgetConfig)
      {
         final WidgetConfigElement wce = new WidgetConfigElement(ce.getAttribute("xforms-type"),
                                                                 ce.getAttribute("xml-schema-type"),
                                                                 ce.getAttribute("appearance"),
                                                                 ce.getAttribute("javascript-class-name"));

         final List<ConfigElement> params = ce.getChildren("param");
         for (final ConfigElement p : params)
         {
            wce.addParam(p.getAttribute("name"), p.getValue());
         }
         widgetConfigs.add(wce);
      }
      try
      {
         final JSONObject result = new JSONObject();
         for (final WidgetConfigElement wce : widgetConfigs)
         {
            if (!result.has(wce.xformsType))
            {
               result.put(wce.xformsType, new JSONObject());
            }
            final JSONObject xformsTypeObject = result.getJSONObject(wce.xformsType);
            String s = wce.xmlSchemaType == null ? "*" : wce.xmlSchemaType;
            if (!xformsTypeObject.has(s))
            {
               xformsTypeObject.put(s, new JSONObject());
            }
            final JSONObject schemaTypeObject = xformsTypeObject.getJSONObject(s);
            s = wce.appearance == null ? "*" : wce.appearance;
            final JSONObject o = new JSONObject();
            schemaTypeObject.put(s, o);
            o.put("className", wce.javascriptClassName);
            if (wce.getParams().size() != 0)
            {
               o.put("params", new JSONObject(wce.getParams()));
            }
         }
         return result;
      }
      catch (JSONException jsone)
      {
         LOGGER.error(jsone, jsone);
         return null;
      }
   }
}
