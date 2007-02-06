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
package org.alfresco.web.forms.xforms;

import java.io.*;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.forms.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

   private final static String[][] JS_NAMESPACES = 
   {
      { "xforms", NamespaceConstants.XFORMS_NS, NamespaceConstants.XFORMS_PREFIX },
      { "xhtml", NamespaceConstants.XHTML_NS, NamespaceConstants.XHTML_PREFIX },
      { "chiba", NamespaceConstants.CHIBA_NS, NamespaceConstants.CHIBA_PREFIX },
      { "alfresco", NamespaceService.ALFRESCO_URI, NamespaceService.ALFRESCO_PREFIX }
   };
   
   private final static String[] JS_SCRIPTS = 
   {
      "/scripts/tiny_mce/" + (LOGGER.isDebugEnabled() 
                              ? "tiny_mce_src.js" 
                              : "tiny_mce.js"),
      "/scripts/ajax/dojo/" + (LOGGER.isDebugEnabled() 
                               ? "dojo.js.uncompressed.js" 
                               : "dojo.js"),
      "/scripts/ajax/xforms.js",
      "/scripts/upload_helper.js",
   };


   private final static String[] BUNDLE_KEYS =
   {
      "validation_provide_values_for_required_fields",
      "idle",
      "loading",
      "add_content",
      "go_up",
      "cancel",
      "upload",
      "path"
   };

   public XFormsProcessor()
   {
   }

   public Session process(final Document instanceDataDocument,
                          final Form form,
                          final Writer out)
      throws FormProcessor.ProcessingException
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      //make the XFormsBean available for this session
      final XFormsBean xforms = (XFormsBean)
         FacesHelper.getManagedBean(fc, "XFormsBean");
      final Session result = 
         xforms.createSession(instanceDataDocument, form);
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
      js.append("var alfresco_xforms_constants = {};\n");
      js.append("alfresco_xforms_constants.WEBAPP_CONTEXT = '").
         append(contextPath).
         append("';\n");
      js.append("alfresco_xforms_constants.XFORMS_UI_DIV_ID = '").
         append(xformsUIDivId).
         append("';\n");
      for (String[] ns : JS_NAMESPACES)
      {
         js.append("alfresco_xforms_constants.").
            append(ns[0].toUpperCase()).
            append("_NS = '").append(ns[1]).append("';\n");
         js.append("alfresco_xforms_constants.").
            append(ns[0].toUpperCase()). 
            append("_PREFIX = '").append(ns[2]).append("';\n");
      }

      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      js.append("alfresco_xforms_constants.resources = {\n");
      for (String k : BUNDLE_KEYS)
      {
         js.append(k).
            append(": '").
            append(bundle.getString(k)).
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
