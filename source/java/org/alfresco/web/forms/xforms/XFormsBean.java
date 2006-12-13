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
import java.util.*;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.*;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.events.XFormsEventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.BoundElement;
import org.chiba.xml.xforms.ui.Upload;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.ls.*;
import org.xml.sax.SAXException;

/**
 * Bean for interacting with the chiba processor from the ui using ajax requests.
 * Manages the chiba bean lifecycle.
 */
public class XFormsBean
{

   /////////////////////////////////////////////////////////////////////////////
   
   /**
    */
   static class XFormsSession implements FormProcessor.Session
   {

      private final Document formInstanceData;
      private final Form form;

      private ChibaBean chibaBean;
      private final SchemaFormBuilder schemaFormBuilder;
      private final HashMap<String, NodeRef> uploads = new HashMap<String, NodeRef>();
      private final List<XFormsEvent> eventLog = new LinkedList<XFormsEvent>();

      public XFormsSession(final Document formInstanceData,
                           final Form form,
                           final String baseUrl)
      {
         this.formInstanceData = formInstanceData;
         this.form = form;
         this.schemaFormBuilder = 
            new SchemaFormBuilder("/ajax/invoke/XFormsBean.handleAction",
                                  SchemaFormBuilder.SUBMIT_METHOD_POST,
                                  new XHTMLWrapperElementsBuilder(),
                                  baseUrl);
      }

      public NodeRef[] getUploadedFiles()
      {
         return (NodeRef[])this.uploads.values().toArray(new NodeRef[0]);
      }

      public void destroy()
      {
         this.uploads.clear();
         try
         {
            this.chibaBean.shutdown();
         }
         catch (XFormsException xfe)
         {
            LOGGER.debug(xfe);
         }
      }

      public Form getForm()
      {
         return this.form;
      }

      public Document getFormInstanceData()
      {
         return this.formInstanceData;
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private static final Log LOGGER = LogFactory.getLog(XFormsBean.class);

   private XFormsSession xformsSession;

   /** @param xformsSession the current session */
   public void setXFormsSession(final XFormsSession xformsSession)
      throws XFormsException
   {
      this.xformsSession = xformsSession;

      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();

      final ChibaBean chibaBean = new ChibaBean();
      XFormsBean.storeCookies(request.getCookies(), chibaBean);

      final HttpSession session = (HttpSession)externalContext.getSession(true);
      final AVMBrowseBean browseBean = (AVMBrowseBean)
         session.getAttribute("AVMBrowseBean");
      final String cwdAVMPath = browseBean.getCurrentPath();

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("building xform for schema " + this.xformsSession.form.getName() +
                      " root element " + this.xformsSession.form.getSchemaRootElementName() +
                      " avm cwd " + cwdAVMPath);
      }
                               
      try
      {
         final Document schemaDocument = this.xformsSession.form.getSchema();
         XFormsBean.rewriteInlineURIs(schemaDocument, cwdAVMPath);
         final Document xformsDocument = 
            this.xformsSession.schemaFormBuilder.buildXForm(this.xformsSession.formInstanceData, 
                                                            schemaDocument,
                                                            this.xformsSession.form.getSchemaRootElementName());

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("generated xform: " + 
                         FormsService.getInstance().writeXMLToString(xformsDocument));
         }

         chibaBean.setXMLContainer(xformsDocument);

         final EventTarget et = (EventTarget)
            chibaBean.getXMLContainer().getDocumentElement();
         final EventListener el = new EventListener()
         {
            public void handleEvent(final Event e)
            {
               XFormsBean.LOGGER.debug("received event " + e);
               XFormsBean.this.xformsSession.eventLog.add((XFormsEvent)e);
            }
         };
         // interaction events my occur during init so we have to register before
         et.addEventListener(XFormsEventFactory.CHIBA_LOAD_URI, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_RENDER_MESSAGE, el, true);
         et.addEventListener(XFormsEventFactory.CHIBA_REPLACE_ALL, el, true);

         chibaBean.init();

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
      this.xformsSession.chibaBean = chibaBean;
   }

   /**
    * Initializes the chiba process with the xform and registers any necessary
    * event listeners.
    */
   public static XFormsSession createSession(final Document formInstanceData,
                                             final Form form)
      throws XFormsException
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("initializing xforms session with form " + form.getName() +
                      " and instance data " + formInstanceData);
      }

      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();

      final String baseUrl = (request.getScheme() + "://" + 
                              request.getServerName() + ':' + 
                              request.getServerPort() + 
                              request.getContextPath());
      return new XFormsSession(formInstanceData, form, baseUrl);
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
      final ChibaBean chibaBean = this.xformsSession.chibaBean;
      final Node xformsDocument = chibaBean.getXMLContainer();
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
      final ChibaBean chibaBean = this.xformsSession.chibaBean;
      if (chibaBean.lookup(id) instanceof Upload)
      {
         chibaBean.updateControlValue(id, null, value, value.getBytes());
      }
      else
      {
         chibaBean.updateControlValue(id, value);
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
      final ChibaBean chibaBean = this.xformsSession.chibaBean;
      chibaBean.updateRepeatIndex(id, index);

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
      final ChibaBean chibaBean = this.xformsSession.chibaBean;
      chibaBean.dispatch(id, XFormsEventFactory.DOM_ACTIVATE);

      final ResponseWriter out = context.getResponseWriter();
      FormsService.getInstance().writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * handles submits and sets the instance data.
    */
   public void handleAction() 
   {
      LOGGER.debug(this + ".handleAction");
      try
      {
         final FacesContext context = FacesContext.getCurrentInstance();
         final HttpServletRequest request = (HttpServletRequest)
            context.getExternalContext().getRequest();
         final FormsService formsService = FormsService.getInstance();
         final Document result = formsService.parseXML(request.getInputStream());
         final Document instanceData = this.xformsSession.getFormInstanceData();
         Element documentElement = instanceData.getDocumentElement();
         if (documentElement != null)
         {
            instanceData.removeChild(documentElement);
         }

         documentElement = result.getDocumentElement();
         this.xformsSession.schemaFormBuilder.removePrototypeNodes(documentElement);
         documentElement = (Element)instanceData.importNode(documentElement, true);
         instanceData.appendChild(documentElement);

         final ResponseWriter out = context.getResponseWriter();
         formsService.writeXML(instanceData, out);
         out.close();
      }
      catch (Throwable t)
      {
         LOGGER.error(t.getMessage(), t);
      }
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
      final ChibaBean chibaBean = this.xformsSession.chibaBean;
      this.swapRepeatItems(chibaBean.lookup(fromItemId), 
                           chibaBean.lookup(toItemId));

      final ResponseWriter out = context.getResponseWriter();
      FormsService.getInstance().writeXML(this.getEventLog(), out);
      out.close();
   }

   /**
    * Provides data for a file picker widget.
    */
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_XML)
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
         final String previewStorePath = 
            browseBean.getCurrentPath().replaceFirst(AVMConstants.STORE_MAIN,
                                                     AVMConstants.STORE_PREVIEW);
         currentPath = AVMConstants.buildAbsoluteAVMPath(previewStorePath,
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
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void uploadFile()
      throws Exception
   {
      LOGGER.debug(this + ".uploadFile()");
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();
      final HttpSession session = (HttpSession)
         externalContext.getSession(true);
      final AVMBrowseBean browseBean = (AVMBrowseBean)
         session.getAttribute("AVMBrowseBean");

      final ServletFileUpload upload = 
         new ServletFileUpload(new DiskFileItemFactory());
      upload.setHeaderEncoding("UTF-8");
      final List<FileItem> fileItems = upload.parseRequest(request);
      final FileUploadBean bean = new FileUploadBean();
      String uploadId = null;
      String currentPath = null;
      String filename = null;
      InputStream fileInputStream = null;
      for (FileItem item : fileItems)
      {
         LOGGER.debug("item = " + item);
         if (item.isFormField() && item.getFieldName().equals("id"))
         {
            uploadId = item.getString();
            LOGGER.debug("uploadId is " + uploadId);
         }
         else if (item.isFormField() && item.getFieldName().equals("currentPath"))
         {
            final String previewStorePath = 
               browseBean.getCurrentPath().replaceFirst(AVMConstants.STORE_MAIN,
                                                        AVMConstants.STORE_PREVIEW);
            currentPath = AVMConstants.buildAbsoluteAVMPath(previewStorePath,
                                                            item.getString());
            LOGGER.debug("currentPath is " + currentPath);
         }
         else
         {
            filename = item.getName();
            int idx = filename.lastIndexOf('\\');
            if (idx == -1)
            {
               idx = filename.lastIndexOf('/');
            }
            if (idx != -1)
            {
               filename = filename.substring(idx + File.separator.length());
            }
            fileInputStream = item.getInputStream();
            LOGGER.debug("parsed file " + filename);
         }
      }

      final ServiceRegistry serviceRegistry = 
         Repository.getServiceRegistry(facesContext);
      final AVMService avmService = serviceRegistry.getAVMService();
      LOGGER.debug("saving file " + filename + " to " + currentPath);
      
      FileCopyUtils.copy(fileInputStream, 
                         avmService.createFile(currentPath, filename));

      final NodeRef uploadNodeRef = 
         AVMNodeConverter.ToNodeRef(-1, currentPath + "/" + filename);
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(ContentModel.PROP_TITLE, filename);
      props.put(ContentModel.PROP_DESCRIPTION, 
                "Uploaded for form " + this.xformsSession.getForm().getName());
      final NodeService nodeService = serviceRegistry.getNodeService();
      nodeService.addAspect(uploadNodeRef, ContentModel.ASPECT_TITLED, props);

      this.xformsSession.uploads.put(uploadId, uploadNodeRef);

      LOGGER.debug("upload complete.  sending response");
      final FormsService formsService = FormsService.getInstance();
      final Document result = formsService.newDocument();
      final Element htmlEl = result.createElement("html");
      result.appendChild(htmlEl);
      final Element bodyEl = result.createElement("body");
      htmlEl.appendChild(bodyEl);

      final Element scriptEl = result.createElement("script");
      bodyEl.appendChild(scriptEl);
      scriptEl.setAttribute("type", "text/javascript");
      final Node scriptText = 
         result.createTextNode("window.parent.FilePickerWidget." +
                               "_upload_completeHandler('" + uploadId + "');");
      scriptEl.appendChild(scriptText);

      final ResponseWriter out = facesContext.getResponseWriter();
      formsService.writeXML(result, out);
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

   private static void rewriteInlineURIs(final Document schemaDocument,
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
      for (XFormsEvent xfe : this.xformsSession.eventLog)
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
      this.xformsSession.eventLog.clear();

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
