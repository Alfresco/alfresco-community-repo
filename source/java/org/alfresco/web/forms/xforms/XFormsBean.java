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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.app.servlet.FacesHelper;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.chiba.xml.xforms.ui.BoundElement;

import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.xml.sax.SAXException;

/**
 * Bean for interacting with the chiba processor from the ui using ajax requests.
 * Manages the chiba bean lifecycle.
 */
public class XFormsBean
{
   private static final Log LOGGER = LogFactory.getLog(XFormsBean.class);

   private Form tt;
   private FormProcessor.InstanceData instanceData = null;
   private ChibaBean chibaBean;
   private final LinkedList<XFormsEvent> eventLog = new LinkedList<XFormsEvent>();

   /** @return the form */
   public Form getForm()
   {
      return this.tt;
   }

   /** @param tt the template type */
   public void setForm(final Form tt)
   {
      this.tt = tt;
   }

   /** @param instanceData the instance data being modified. */
   public void setInstanceData(final FormProcessor.InstanceData instanceData)
   {
      this.instanceData = instanceData;
   }

   /**
    * Initializes the chiba process with the xform and registers any necessary
    * event listeners.
    */
   public void init()
      throws XFormsException
   {
      LOGGER.debug("initializing " + this + " with tt " + tt.getName());
      this.chibaBean = new ChibaBean();
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();
      final HttpSession session = (HttpSession)
         externalContext.getSession(true);
      final AVMBrowseBean browseBean = (AVMBrowseBean)
         session.getAttribute("AVMBrowseBean");
      LOGGER.debug("avm cwd is " + browseBean.getCurrentPath());
	
      XFormsBean.storeCookies(request.getCookies(), this.chibaBean);

      try
      {
         final Document form = this.buildXForm(instanceData.getContent(), 
                                               tt,
                                               browseBean.getCurrentPath(),
                                               request);
         this.chibaBean.setXMLContainer(form);

         final EventTarget et = (EventTarget)
            this.chibaBean.getXMLContainer().getDocumentElement();
         final EventListener el = new EventListener()
         {
            public void handleEvent(Event e)
            {
               XFormsBean.LOGGER.debug("received event " + e);
               XFormsBean.this.eventLog.add((XFormsEvent)e);
            }
         };
         // interaction events my occur during init so we have to register before
         et.addEventListener(XFormsEventFactory.CHIBA_LOAD_URI, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_RENDER_MESSAGE, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_REPLACE_ALL, el, true);

         this.chibaBean.init();

         // register for notification events
         et.addEventListener(XFormsEventFactory.SUBMIT_DONE, el, true);
         et.addEventListener(XFormsEventFactory.SUBMIT_ERROR, el, true);
         et.addEventListener(XFormsEventFactory.REQUIRED, el, true);
         et.addEventListener(XFormsEventFactory.OPTIONAL, el, true);
         et.addEventListener(XFormsEventFactory.VALID, el, true);
         et.addEventListener(XFormsEventFactory.INVALID, el, true);
         et.addEventListener(XFormsEventFactory.OUT_OF_RANGE, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_STATE_CHANGED, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_PROTOTYPE_CLONED, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_ID_GENERATED, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_ITEM_INSERTED, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_ITEM_DELETED, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_INDEX_CHANGED, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_SWITCH_TOGGLED, el, true);
      }
      catch (FormBuilderException fbe)
      {
         LOGGER.error(fbe);
      }
      catch (IOException ioe)
      {
         LOGGER.error(ioe);
      }
      catch (SAXException saxe)
      {
         LOGGER.error(saxe);
      }
   }

   /**
    * Writes the xform out to the http servlet response.  This allows
    * us to use the browser to parse the xform using XMLHttpRequest.
    */
   public void getXForm() 
      throws IOException,
      XFormsException
   {
      LOGGER.debug(this + " building xform");
      final FacesContext context = FacesContext.getCurrentInstance();
      final ResponseWriter out = context.getResponseWriter();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      LOGGER.debug("building xform for " + this.tt.getName());
      final Node form = this.chibaBean.getXMLContainer();
      final FormsService ts = FormsService.getInstance();
      ts.writeXML(form, out);
   }

   /**
    * sets the value of a control in the processor.
    *
    * @param id the id of the control in the host document
    * @param value the new value
    * @return the list of events that may result through this action
    */
   public void setXFormsValue() 
      throws XFormsException, IOException
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      final String id = (String)requestParameters.get("id");
      final String value = (String)requestParameters.get("value");

      LOGGER.debug(this + " setXFormsValue(" + id + ", " + value + ")");
      this.chibaBean.updateControlValue(id, value);

      final FormsService ts = FormsService.getInstance();
      final ResponseWriter out = context.getResponseWriter();
      ts.writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * sets the value of a control in the processor.
    *
    * @param id the id of the control in the host document
    * @param value the new value
    * @return the list of events that may result through this action
    */
   public void setRepeatIndex() 
      throws XFormsException, IOException
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      final String id = (String)requestParameters.get("id");
      final int index = Integer.parseInt((String)requestParameters.get("index"));

      LOGGER.debug(this + " setRepeatIndex(" + id + ", " + index + ")");
      this.chibaBean.updateRepeatIndex(id, index);

      final FormsService ts = FormsService.getInstance();
      final ResponseWriter out = context.getResponseWriter();
      ts.writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * fires an action associated with a trigger.
    *
    * @param id the id of the control in the host document
    */
   public void fireAction() 
      throws XFormsException, IOException
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      final String id = (String)requestParameters.get("id");

      LOGGER.debug(this + " fireAction(" + id + ")");
      this.chibaBean.dispatch(id, XFormsEventFactory.DOM_ACTIVATE);

      final FormsService ts = FormsService.getInstance();
      final ResponseWriter out = context.getResponseWriter();
      ts.writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * handles submits and sets the instance data.
    */
   public void handleAction() 
      throws Exception
   {
      LOGGER.debug(this + " handleAction");
      final FacesContext context = FacesContext.getCurrentInstance();
      final HttpServletRequest request = (HttpServletRequest)
         context.getExternalContext().getRequest();
      final FormsService ts = FormsService.getInstance();
      final Document result = ts.parseXML(request.getInputStream());
      this.instanceData.setContent(result);

      final ResponseWriter out = context.getResponseWriter();
      ts.writeXML(result, out);
      out.close();
   }

   /**
    * Swaps model nodes to implement reordering within repeats.
    */
   public void swapRepeatItems() 
      throws Exception
   {
      LOGGER.debug(this + " handleAction");
      final FacesContext context = FacesContext.getCurrentInstance();
      final HttpServletRequest request = (HttpServletRequest)
         context.getExternalContext().getRequest();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();

      final String fromItemId = (String)requestParameters.get("fromItemId");
      final String toItemId = (String)requestParameters.get("toItemId");
      LOGGER.debug("swapping from " + fromItemId + " to " + toItemId);
      this.swapRepeatItems(this.chibaBean.lookup(fromItemId), 
                           this.chibaBean.lookup(toItemId));

      final FormsService ts = FormsService.getInstance();
      final ResponseWriter out = context.getResponseWriter();
      ts.writeXML(this.getEventLog(), out);
      out.close();
   }

   private void swapRepeatItems(final XFormsElement from,
                                final XFormsElement to)
   {
      LOGGER.debug("swapping repeat item " + from + " with " + to);

      if (from instanceof BoundElement && to instanceof BoundElement)
      {
         LOGGER.debug("from instance id  " + ((BoundElement)from).getInstanceId());
         final Instance instance = from.getModel().getInstance(((BoundElement)from).getInstanceId());
         assert instance == to.getModel().getInstance(((BoundElement)to).getInstanceId());

         final String fromLocationPath = ((BoundElement)from).getLocationPath();
         final ModelItem fromModelItem = instance.getModelItem(fromLocationPath);

         final String toLocationPath = ((BoundElement)to).getLocationPath();
         final ModelItem toModelItem = instance.getModelItem(toLocationPath);

         LOGGER.debug("from[" + from.getId() + "] " + fromLocationPath + "=" + fromModelItem.getValue());
         LOGGER.debug("to[" + to.getId() + "] " + toLocationPath + "=" + toModelItem.getValue());

         final Node fromNode = (Node)fromModelItem.getNode();
         final Node toNode = (Node)toModelItem.getNode();
         Node swapNode = fromNode;
         fromModelItem.setNode(toNode);
         toModelItem.setNode(swapNode);

         final Node parentNode = fromNode.getParentNode();
         assert parentNode.equals(toNode.getParentNode());

         swapNode = parentNode.getOwnerDocument().createTextNode("swap");
         parentNode.replaceChild(swapNode, fromNode);
         parentNode.replaceChild(fromNode, toNode);
         parentNode.replaceChild(toNode, swapNode);
      }
   }

   private void rewriteInlineURIs(final Document schemaDocument,
                                  final String cwdAvmPath)
   {
      final NodeList includes = 
         schemaDocument.getElementsByTagNameNS(SchemaFormBuilder.XMLSCHEMA_NS, "include");
      LOGGER.debug("rewriting " + includes.getLength() + " includes");
      for (int i = 0; i < includes.getLength(); i++)
      {
         final Element includeEl = (Element)includes.item(i);
         if (includeEl.hasAttribute("schemaLocation"))
         {
            String uri = includeEl.getAttribute("schemaLocation");
            final String baseURI = (uri.charAt(0) == '/'
                                    ? AVMConstants.buildAVMStoreUrl(cwdAvmPath)
                                    : AVMConstants.buildAVMAssetUrl(cwdAvmPath));

            LOGGER.debug("rewriting " + uri + " as " + (baseURI + uri));
            includeEl.setAttribute("schemaLocation", baseURI + uri);
         }
      }
   }

   /**
    * Generates the xforms based on the schema.
    */
   private Document buildXForm(Document xmlContent, 
                               final Form tt,
                               final String cwdAvmPath,
                               final HttpServletRequest request) 
      throws FormBuilderException,
      IOException,
      SAXException
   {
      final Document schemaDocument = tt.getSchema();
      this.rewriteInlineURIs(schemaDocument, cwdAvmPath);
      final String baseUrl = (request.getScheme() + "://" + 
                              request.getServerName() + ':' + 
                              request.getServerPort() + 
                              request.getContextPath());
      LOGGER.debug("using baseUrl " + baseUrl + " for schemaformbuilder");
      final SchemaFormBuilder builder = 
         new SchemaFormBuilder("/ajax/invoke/XFormsBean.handleAction",
                               SchemaFormBuilder.SUBMIT_METHOD_POST,
                               new XHTMLWrapperElementsBuilder(),
                               baseUrl);
      LOGGER.debug("building xform for schema " + tt.getName());
      final Document result = builder.buildForm(xmlContent, 
                                                schemaDocument, 
                                                tt.getSchemaRootElementName());
      LOGGER.debug("generated xform: " + result);
      //	LOGGER.debug(ts.writeXMLToString(result));
      return result;
   }

   private Node getEventLog()
   {
      final FormsService ts = FormsService.getInstance();
      final Document result = ts.newDocument();
      final Element eventsElement = result.createElement("events");
      result.appendChild(eventsElement);
      for (XFormsEvent xfe : this.eventLog)
      {
         final String type = xfe.getType();
         LOGGER.debug("adding event " + type + " to the event log");
         final Element target = (Element)xfe.getTarget();

         final Element eventElement = result.createElement(type);
         eventsElement.appendChild(eventElement);
         eventElement.setAttribute("targetId", target.getAttributeNS(null, "id"));
         eventElement.setAttribute("targetName", target.getLocalName());

         final Collection properties = xfe.getPropertyNames();
         if (properties != null)
         {
            for (Object name : properties)
            {
               final Object value = xfe.getContextInfo((String)name);
               LOGGER.debug("adding property {" + name + 
                            ":" + value + "} to event " + type);
               final Element propertyElement = result.createElement("property");
               eventElement.appendChild(propertyElement);
               propertyElement.setAttribute("name", name.toString());
               propertyElement.setAttribute("value", 
                                            value != null ? value.toString() : null);
            }
         }
      }
      this.eventLog.clear();
      return result;
   }

   /**
    * stores cookies that may exist in request and passes them on to processor for usage in
    * HTTPConnectors. Instance loading and submission then uses these cookies. Important for
    * applications using auth.
    */
   @SuppressWarnings("unchecked")
   private static void storeCookies(final javax.servlet.http.Cookie[] cookiesIn,
                                    final ChibaBean chibaBean){
      if (cookiesIn != null) {
         org.apache.commons.httpclient.Cookie[] commonsCookies = 
            new org.apache.commons.httpclient.Cookie[cookiesIn.length];
         for (int i = 0; i < cookiesIn.length; i += 1) {
            commonsCookies[i] =
               new org.apache.commons.httpclient.Cookie(cookiesIn[i].getDomain(),
                                                        cookiesIn[i].getName(),
                                                        cookiesIn[i].getValue(),
                                                        cookiesIn[i].getPath(),
                                                        cookiesIn[i].getMaxAge(),
                                                        cookiesIn[i].getSecure());
         }
         chibaBean.getContext().put(AbstractHTTPConnector.REQUEST_COOKIE,
                                    commonsCookies);
      }
   }
}
