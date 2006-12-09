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
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.ui.common.Utils;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.chiba.xml.xforms.ui.BoundElement;
import org.chiba.xml.xforms.ui.Upload;

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

   private Form form;
   private FormProcessor.InstanceData instanceData = null;
   private ChibaBean chibaBean;
   private SchemaFormBuilder schemaFormBuilder = null;
   private final LinkedList<XFormsEvent> eventLog = new LinkedList<XFormsEvent>();

   /** @return the form */
   public Form getForm()
   {
      return this.form;
   }

   /** @param tt the template type */
   public void setForm(final Form form)
   {
      this.form = form;
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
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("initializing " + this + " with form " + this.form.getName());
      }
      this.chibaBean = new ChibaBean();
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();
      XFormsBean.storeCookies(request.getCookies(), this.chibaBean);

      final HttpSession session = (HttpSession)externalContext.getSession(true);
      final AVMBrowseBean browseBean = (AVMBrowseBean)
         session.getAttribute("AVMBrowseBean");
      final String cwdAVMPath = browseBean.getCurrentPath();

      final String baseUrl = (request.getScheme() + "://" + 
                              request.getServerName() + ':' + 
                              request.getServerPort() + 
                              request.getContextPath());
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("building xform for schema " + form.getName() +
                      " with baseUrl " + baseUrl +
                      " root element " + form.getSchemaRootElementName() +
                      " avm cwd " + cwdAVMPath);
      }
      this.schemaFormBuilder = 
         new SchemaFormBuilder("/ajax/invoke/XFormsBean.handleAction",
                               SchemaFormBuilder.SUBMIT_METHOD_POST,
                               new XHTMLWrapperElementsBuilder(),
                               baseUrl);
                               
      try
      {
         final Document schemaDocument = this.form.getSchema();
         this.rewriteInlineURIs(schemaDocument, cwdAVMPath);
         final Document xformsDocument = 
            this.schemaFormBuilder.buildXForm(instanceData.getContent(), 
                                              schemaDocument,
                                              this.form.getSchemaRootElementName());

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("generated xform: " + 
                         FormsService.getInstance().writeXMLToString(xformsDocument));
         }

         this.chibaBean.setXMLContainer(xformsDocument);

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
      LOGGER.debug(this + ".getXForm()");
      final FacesContext context = FacesContext.getCurrentInstance();
      final ResponseWriter out = context.getResponseWriter();
      final Node xformsDocument = this.chibaBean.getXMLContainer();
      FormsService.getInstance().writeXML(xformsDocument, out);
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

      LOGGER.debug(this + ".setXFormsValue(" + id + ", " + value + ")");
      if (this.chibaBean.lookup(id) instanceof Upload)
      {
         this.chibaBean.updateControlValue(id, null, value, value.getBytes());
      }
      else
      {
         this.chibaBean.updateControlValue(id, value);
      }

      final ResponseWriter out = context.getResponseWriter();
      FormsService.getInstance().writeXML(this.getEventLog(), out);
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

      LOGGER.debug(this + ".setRepeatIndex(" + id + ", " + index + ")");
      this.chibaBean.updateRepeatIndex(id, index);

      final ResponseWriter out = context.getResponseWriter();
      FormsService.getInstance().writeXML(this.getEventLog(), out);
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

      LOGGER.debug(this + ".fireAction(" + id + ")");
      this.chibaBean.dispatch(id, XFormsEventFactory.DOM_ACTIVATE);

      final ResponseWriter out = context.getResponseWriter();
      FormsService.getInstance().writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * handles submits and sets the instance data.
    */
   public void handleAction() 
      throws Exception
   {
      LOGGER.debug(this + ".handleAction");
      final FacesContext context = FacesContext.getCurrentInstance();
      final HttpServletRequest request = (HttpServletRequest)
         context.getExternalContext().getRequest();
      final FormsService formsService = FormsService.getInstance();
      final Document result = formsService.parseXML(request.getInputStream());
      this.schemaFormBuilder.removePrototypeNodes(result.getDocumentElement());
      this.instanceData.setContent(result);

      final ResponseWriter out = context.getResponseWriter();
      formsService.writeXML(result, out);
      out.close();
   }

   /**
    * Swaps model nodes to implement reordering within repeats.
    */
   public void swapRepeatItems() 
      throws Exception
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();

      final String fromItemId = (String)requestParameters.get("fromItemId");
      final String toItemId = (String)requestParameters.get("toItemId");
      LOGGER.debug(this + ".swapRepeatItems(" + fromItemId + ", " + toItemId + ")");
      this.swapRepeatItems(this.chibaBean.lookup(fromItemId), 
                           this.chibaBean.lookup(toItemId));

      final ResponseWriter out = context.getResponseWriter();
      FormsService.getInstance().writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * Provides data for a file picker widget.
    */
   public void getFilePickerData()
      throws Exception
   {
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpSession session = (HttpSession)
         externalContext.getSession(true);
      final AVMBrowseBean browseBean = (AVMBrowseBean)
         session.getAttribute("AVMBrowseBean");

      final Map requestParameters = externalContext.getRequestParameterMap();
      String currentPath = (String)requestParameters.get("currentPath");
      if (currentPath == null)
      {
         currentPath = browseBean.getCurrentPath();
      }
      else
      {
         currentPath = AVMConstants.buildAbsoluteAVMPath(browseBean.getCurrentPath(),
                                                         currentPath);
      }
      LOGGER.debug(this + ".getFilePickerData(" + currentPath + ")");

      final ServiceRegistry serviceRegistry = 
         Repository.getServiceRegistry(facesContext);
      final AVMService avmService = serviceRegistry.getAVMService();

      final FormsService formsService = FormsService.getInstance();
      final Document result = formsService.newDocument();
      final Element filePickerDataElement = result.createElement("file-picker-data");
      result.appendChild(filePickerDataElement);


      final AVMNodeDescriptor currentNode = avmService.lookup(-1, currentPath);
      if (currentNode == null)
      {
         final Element errorElement = result.createElement("error");
         errorElement.appendChild(result.createTextNode("Path " + currentPath + " not found"));
         filePickerDataElement.appendChild(errorElement);
         currentPath = browseBean.getCurrentPath();
      }
      else if (! currentNode.isDirectory())
      {
         currentPath = AVMNodeConverter.SplitBase(currentPath)[0];
      }
      
      Element e = result.createElement("current-node");
      e.setAttribute("avmPath", currentPath);
      e.setAttribute("webappRelativePath", 
                     AVMConstants.getWebappRelativePath(currentPath));
      e.setAttribute("type", "directory");
      e.setAttribute("image", "/images/icons/space_small.gif");
      filePickerDataElement.appendChild(e);

      for (Map.Entry<String, AVMNodeDescriptor> entry : 
              avmService.getDirectoryListing(-1, currentPath).entrySet())
      {
         e = result.createElement("child-node");
         e.setAttribute("avmPath", entry.getValue().getPath());
         e.setAttribute("webappRelativePath", 
                        AVMConstants.getWebappRelativePath(entry.getValue().getPath()));
         e.setAttribute("type", entry.getValue().isDirectory() ? "directory" : "file");
         e.setAttribute("image", (entry.getValue().isDirectory()
                                  ? "/images/icons/space_small.gif"
                                  : Utils.getFileTypeImage(facesContext, 
                                                           entry.getValue().getName(),
                                                           true)));
         filePickerDataElement.appendChild(e);
      }

      final ResponseWriter out = facesContext.getResponseWriter();
      FormsService.getInstance().writeXML(result, out);
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

   private Node getEventLog()
   {
      final FormsService formsService = FormsService.getInstance();
      final Document result = formsService.newDocument();
      final Element eventsElement = result.createElement("events");
      result.appendChild(eventsElement);
      for (XFormsEvent xfe : this.eventLog)
      {
         final String type = xfe.getType();
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("adding event " + type + " to the event log");
         }
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
               if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug("adding property {" + name + 
                               ":" + value + "} to event " + type);
               }

               final Element propertyElement = result.createElement("property");
               eventElement.appendChild(propertyElement);
               propertyElement.setAttribute("name", name.toString());
               propertyElement.setAttribute("value", 
                                            value != null ? value.toString() : null);
            }
         }
      }
      this.eventLog.clear();

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("generated event log:\n" + 
                      formsService.writeXMLToString(result));
      }
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
