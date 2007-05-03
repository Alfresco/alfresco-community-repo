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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.forms.xforms;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.JavaScriptUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.chiba.xml.ns.NamespaceConstants;
import org.chiba.xml.xforms.exception.XFormsException;

public class XFormsProcessor
   implements FormProcessor
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
      "/scripts/ajax/ajax_helper.js",
      "/scripts/ajax/tiny_mce_wcm_extensions.js",
      "/scripts/ajax/xforms.js",
      "/scripts/ajax/file_picker_widget.js",
      "/scripts/upload_helper.js",
   };

   /** Localized strings needed by the xforms client. */
   private final static String[] BUNDLE_KEYS =
   {
      "add_content",
      "cancel",
      "click_to_edit",
      "eg",
      "go_up",
      "idle",
      "loading",
      "path",
      "upload",
      "validation_provide_values_for_required_fields"
   };

   public XFormsProcessor()
   {
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
         FacesHelper.getManagedBean(fc, "XFormsBean");
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
         FacesHelper.getManagedBean(fc, "XFormsBean");
      final AVMBrowseBean avmBrowseBean = (AVMBrowseBean)
         FacesHelper.getManagedBean(fc, "AVMBrowseBean");
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
      final StringBuilder js = new StringBuilder("\ndjConfig = {isDebug:" + LOGGER.isDebugEnabled() + "};\n");
      final String[] jsNamespacesObjects = { "alfresco", "alfresco.constants", "alfresco.xforms", "alfresco.xforms.constants" };
      for (final String jsNamespace : jsNamespacesObjects)
      {
         js.append(jsNamespace).
            append(" = typeof ").
            append(jsNamespace).
            append(" == 'undefined' ? {} : ").
            append(jsNamespace).
            append(";\n");
      }
      js.append("alfresco.constants.WEBAPP_CONTEXT = '").
         append(JavaScriptUtils.javaScriptEscape(contextPath)).
         append("';\n");
      js.append("alfresco.constants.AVM_WEBAPP_CONTEXT = '").
         append(JavaScriptUtils.javaScriptEscape(avmBrowseBean.getWebapp())).
         append("';\n");
      js.append("alfresco.constants.AVM_WEBAPP_URL = '").
         append(JavaScriptUtils.javaScriptEscape(AVMConstants.buildWebappUrl(AVMConstants.getCorrespondingPreviewStoreName(avmBrowseBean.getSandbox()), 
                                                                             avmBrowseBean.getWebapp()))).
         append("';\n");
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
         append(sdf.toLocalizedPattern()).
         append("';\n");
      sdf = (SimpleDateFormat)
         SimpleDateFormat.getTimeInstance(DateFormat.SHORT, 
                                          Application.getLanguage(fc));
      js.append("alfresco.xforms.constants.TIME_FORMAT = '").
         append(sdf.toLocalizedPattern()).
         append("';\n");
      sdf = (SimpleDateFormat)
         SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, 
                                              DateFormat.SHORT, 
                                              Application.getLanguage(fc));
      js.append("alfresco.xforms.constants.DATE_TIME_FORMAT = '").
         append(sdf.toLocalizedPattern()).
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
      e.appendChild(result.createTextNode(js.toString()));

      div.appendChild(e);
	    
      // include all our scripts, order is significant
      for (final String script : JS_SCRIPTS)
      {
         e = result.createElement("script");
         e.setAttribute("type", "text/javascript");
         e.setAttribute("src", contextPath + script);
         e.appendChild(result.createTextNode("\n"));
         div.appendChild(e);
      }
 
      XMLUtil.print(result, out);
   }
}
