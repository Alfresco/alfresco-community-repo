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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.XSModel;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.DOMEventNames;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.ns.NamespaceConstants;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Instance;
import org.chiba.xml.xforms.core.Model;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.core.impl.DefaultValidatorMode;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.RepeatItem;
import org.chiba.xml.xforms.ui.Upload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.xml.sax.SAXException;


/**
 * Bean for interacting with the chiba processor from the ui using ajax requests.
 * Manages the chiba bean lifecycle.
 */
public class XFormsBean implements Serializable
{
   private static final long serialVersionUID = -7979077333882370784L;

   /////////////////////////////////////////////////////////////////////////////

   public static class AlfrescoSubmissionHandler
      extends AbstractConnector
      implements SubmissionHandler
   {

      public Map submit(final Submission submission, 
                        final Node instance)
         throws XFormsException
      {
         if (XFormsBean.LOGGER.isDebugEnabled())
         {
            XFormsBean.LOGGER.debug(this.getClass().getName() + 
                                    " recieved submission " + XMLUtil.toString(instance, true));
         }
         final FacesContext fc = FacesContext.getCurrentInstance();
         //make the XFormsBean available for this session
         final XFormsBean xforms = (XFormsBean)FacesHelper.getManagedBean(fc, BEAN_NAME);
         xforms.handleSubmit(instance);
         return Collections.EMPTY_MAP;
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   
   /**
    */
   class XFormsSession implements FormProcessor.Session
   {
      private static final long serialVersionUID = 1L;
      private final Document formInstanceData;
      private final String formInstanceDataName;
      private final Form form;

      private ChibaBean chibaBean;
      private final Schema2XForms schema2XForms;
      private final List<XMLEvent> eventLog = new LinkedList<XMLEvent>();

      public XFormsSession(final Document formInstanceData,
                           final String formInstanceDataName,
                           final Form form,
                           final String baseUrl, 
                           final boolean formatCaption)
      {
         this.formInstanceData = formInstanceData;
         this.formInstanceDataName = formInstanceDataName;
         this.form = form;
         this.schema2XForms = new Schema2XForms(/* "/ajax/invoke/XFormsBean.handleAction" */ null,
                                                Schema2XForms.SubmitMethod.POST,
                                                /* baseUrl */ "alfresco:" + XFormsBean.class.getName(), formatCaption);
      }

      public void destroy()
      {
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

      public String getFormInstanceDataName()
      {
         return this.formInstanceDataName;
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private static final Log LOGGER = LogFactory.getLog(XFormsBean.class);

   private XFormsSession xformsSession;
   private transient Schema2XFormsProperties schema2XFormsProperties;
   private AVMBrowseBean avmBrowseBean;
   private NavigationBean navigator;
   // lock for XFormSession.eventLog
   private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
   private Lock writeLock = rwLock.writeLock();
   private Lock readLock = rwLock.readLock();
   
   public static String BEAN_NAME = "XFormsBean";
   
   /**
    * @param schema2XFormsProperties the schema2XFormsProperties to set.
    */
   public void setSchema2XFormsProperties(final Schema2XFormsProperties schema2XFormsProperties)
   {
      this.schema2XFormsProperties = schema2XFormsProperties;
   }
   
   protected Schema2XFormsProperties getSchema2XFormsProperties()
   {
      if (schema2XFormsProperties == null)
      {
         schema2XFormsProperties = (Schema2XFormsProperties)FacesHelper.getManagedBean(
               FacesContext.getCurrentInstance(), "Schema2XFormsProperties");
      }
      return schema2XFormsProperties;
   }

   /**
    * @param avmBrowseBean the avmBrowseBean to set.
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   public void setNavigator(final NavigationBean navigator)
   {
      this.navigator = navigator;
   }

   /** @param xformsSession the current session */
   public void setXFormsSession(final XFormsSession xformsSession)
      throws FormBuilderException,
      XFormsException
   {
      this.xformsSession = xformsSession;

      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();
      final ServletContext servletContext = (ServletContext)
         externalContext.getContext();
      
      writeLock.lock();
      try
      {
         final ChibaBean chibaBean = new ChibaBean();
         chibaBean.setConfig(servletContext.getRealPath("/WEB-INF/chiba.xml"));
         Pair<Document, XSModel> chibaPair = this.getXFormsDocument();
         chibaBean.setXMLContainer(chibaPair.getFirst(), chibaPair.getSecond());
          
         final EventTarget et = (EventTarget)
         chibaBean.getXMLContainer().getDocumentElement();
         final EventListener el = new EventListener()
         {
            public void handleEvent(final Event e)
            {
               final XMLEvent xmle = (XMLEvent)e;
               if (XFormsBean.LOGGER.isDebugEnabled())
                  XFormsBean.LOGGER.debug("received event " + xmle.getType() + ": " + xmle);

               XFormsBean.this.xformsSession.eventLog.add(xmle);
            }
         };
          
         // interaction events my occur during init so we have to register before
         et.addEventListener(ChibaEventNames.LOAD_URI, el, true);
         et.addEventListener(ChibaEventNames.RENDER_MESSAGE, el, true);
         et.addEventListener(ChibaEventNames.REPLACE_ALL, el, true);
    
         et.addEventListener(XFormsEventNames.ENABLED, el, true);
         et.addEventListener(XFormsEventNames.DISABLED, el, true);
         et.addEventListener(XFormsEventNames.REQUIRED, el, true);
         et.addEventListener(XFormsEventNames.OPTIONAL, el, true);
         et.addEventListener(XFormsEventNames.READONLY, el, true);
         et.addEventListener(XFormsEventNames.READWRITE, el, true);
         et.addEventListener(XFormsEventNames.VALID, el, true);
         et.addEventListener(XFormsEventNames.INVALID, el, true);
         et.addEventListener(XFormsEventNames.IN_RANGE, el, true);
         et.addEventListener(XFormsEventNames.OUT_OF_RANGE, el, true);
         et.addEventListener(XFormsEventNames.SELECT, el, true);
         et.addEventListener(XFormsEventNames.DESELECT, el, true);
         et.addEventListener(XFormsEventNames.INSERT, el, true);
         et.addEventListener(XFormsEventNames.DELETE, el, true);
    
         chibaBean.init();
    
         // register for notification events
         et.addEventListener(XFormsEventNames.SUBMIT, el, true);
         et.addEventListener(XFormsEventNames.SUBMIT_DONE, el, true);
         et.addEventListener(XFormsEventNames.SUBMIT_ERROR, el, true);
         et.addEventListener(ChibaEventNames.STATE_CHANGED, el, true);
         et.addEventListener(ChibaEventNames.PROTOTYPE_CLONED, el, true);
         et.addEventListener(ChibaEventNames.ID_GENERATED, el, true);
         et.addEventListener(ChibaEventNames.ITEM_INSERTED, el, true);
         et.addEventListener(ChibaEventNames.ITEM_DELETED, el, true);
         et.addEventListener(ChibaEventNames.INDEX_CHANGED, el, true);
         et.addEventListener(ChibaEventNames.SWITCH_TOGGLED, el, true);
         this.xformsSession.chibaBean = chibaBean;
      }
      finally
      {
         writeLock.unlock();
      }
   }

   /**
    * Initializes the chiba process with the xform and registers any necessary
    * event listeners.
    */
   public XFormsSession createSession(final Document formInstanceData,
                                      final String formInstanceDataName,
                                      final Form form)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("initializing xforms session with form " + form.getName() +
                      " and instance data " + formInstanceDataName + 
                      " (" + formInstanceData + ")");
      }

      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final ExternalContext externalContext = facesContext.getExternalContext();
      final HttpServletRequest request = (HttpServletRequest)
         externalContext.getRequest();

      final String baseUrl = (request.getScheme() + "://" + 
                              request.getServerName() + ':' + 
                              request.getServerPort() + 
                              request.getContextPath());
      return this.new XFormsSession(formInstanceData, formInstanceDataName, form, baseUrl, getSchema2XFormsProperties().isFormatCaption());
   }

   /**
    * Writes the xform out to the http servlet response.  This allows
    * us to use the browser to parse the xform using XMLHttpRequest.
    */
   public void getXForm() throws IOException, XFormsException
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug(this + ".getXForm()");
      final FacesContext context = FacesContext.getCurrentInstance();
      final ResponseWriter out = context.getResponseWriter();

      readLock.lock();
      try
      {
         final ChibaBean chibaBean = this.xformsSession.chibaBean;
         final Node xformsDocument = chibaBean.getXMLContainer();
         XMLUtil.print(xformsDocument, out);
      }
      finally
      {
         readLock.unlock();
      }
   }

   /**
    * sets the value of a control in the processor.
    */
   public void setXFormsValue() throws XFormsException, IOException
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      final String id = (String)requestParameters.get("id");
      final String value = (String)requestParameters.get("value");

      if (LOGGER.isDebugEnabled())
         LOGGER.debug(this + ".setXFormsValue(" + id + ", " + value + ")");

      writeLock.lock();
      try
      {
         final ChibaBean chibaBean = this.xformsSession.chibaBean;

         if (chibaBean.getContainer().lookup(id) instanceof Upload)
         {
            chibaBean.updateControlValue(id, null, value, value.getBytes("UTF-8"));
         }
         else
         {
            chibaBean.updateControlValue(id, value);
         }
         final ResponseWriter out = context.getResponseWriter();
         XMLUtil.print(this.getEventLog(), out);
         out.close();
      }
      finally
      {
         writeLock.unlock();
      }
   }

   /**
    * sets the value of a control in the processor.
    */
   public void setRepeatIndeces() throws XFormsException, IOException
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      final String repeatIds = (String)requestParameters.get("repeatIds");
      if (LOGGER.isDebugEnabled())
         LOGGER.debug(this + ".setRepeatIndeces(" + repeatIds + ")");

      writeLock.lock();
      try
      {
         for (String id : repeatIds.split(","))
         {
            final int index = Integer.parseInt((String)requestParameters.get(id));
            if (LOGGER.isDebugEnabled())
               LOGGER.debug(this + ".setRepeatIndex(" + id + ", " + index + ")");
            final ChibaBean chibaBean = this.xformsSession.chibaBean;
            chibaBean.updateRepeatIndex(id, index);
         }
         final ResponseWriter out = context.getResponseWriter();
         XMLUtil.print(this.getEventLog(), out);
         out.close();
      }
      finally
      {
         writeLock.unlock();
      }
   }

   /**
    * fires an action associated with a trigger.
    */
   public void fireAction() throws XFormsException, IOException
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();
      final String id = (String)requestParameters.get("id");

      if (LOGGER.isDebugEnabled())
         LOGGER.debug(this + ".fireAction(" + id + ")");

      writeLock.lock();
      try
      {
         final ChibaBean chibaBean = this.xformsSession.chibaBean;
         chibaBean.dispatch(id, DOMEventNames.ACTIVATE);
         
         final ResponseWriter out = context.getResponseWriter();
         XMLUtil.print(this.getEventLog(), out);
         out.close();
      }
      finally
      {
         writeLock.unlock();
      }
   }

   /**
    * handles submits and sets the instance data.
    */
   public void handleAction() 
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug(this + ".handleAction");
      try
      {
         final FacesContext context = FacesContext.getCurrentInstance();
         final HttpServletRequest request = (HttpServletRequest)
            context.getExternalContext().getRequest();
         final Document result = XMLUtil.parse(request.getInputStream());
         this.handleSubmit(result);
         final Document instanceData = this.xformsSession.getFormInstanceData();
         final ResponseWriter out = context.getResponseWriter();
         XMLUtil.print(instanceData, out, false);
         out.close();
      }
      catch (Throwable t)
      {
         LOGGER.error(t.getMessage(), t);
      }
   }

   public void handleSubmit(Node result)
   {
      final Document instanceData = this.xformsSession.getFormInstanceData();
      Element documentElement = instanceData.getDocumentElement();
      if (documentElement != null)
      {
         instanceData.removeChild(documentElement);
      }
      if (result instanceof Document)
      {
         result = ((Document)result).getDocumentElement();
      }
      documentElement = (Element)instanceData.importNode(result.cloneNode(true), true);
      Schema2XForms.removePrototypeNodes(documentElement);
      instanceData.appendChild(documentElement);
      instanceData.normalizeDocument();
   }

   /**
    * Swaps model nodes to implement reordering within repeats.
    */
   public void swapRepeatItems() throws Exception
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final Map requestParameters = context.getExternalContext().getRequestParameterMap();

      final String fromItemId = (String)requestParameters.get("fromItemId");
      final String toItemId = (String)requestParameters.get("toItemId");
      if (LOGGER.isDebugEnabled())
         LOGGER.debug(this + ".swapRepeatItems(" + fromItemId + ", " + toItemId + ")");

      writeLock.lock();
      try
      {
         final ChibaBean chibaBean = this.xformsSession.chibaBean;
         final RepeatItem from = (RepeatItem)chibaBean.getContainer().lookup(fromItemId);
         if (from == null)
         {
            throw new NullPointerException("unable to find source repeat item " + fromItemId);
         }
         final RepeatItem to = (RepeatItem)chibaBean.getContainer().lookup(toItemId);
         if (to == null)
         {
            throw new NullPointerException("unable to find destination repeat item " + toItemId);
         }

         this.swapRepeatItems(from, to);
         
         final ResponseWriter out = context.getResponseWriter();
         XMLUtil.print(this.getEventLog(), out);
         out.close();
      }
      finally
      {
         writeLock.unlock();
      }
   }

   private void swapRepeatItems(final RepeatItem from,
                                final RepeatItem to)
      throws XFormsException
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("swapping repeat item " + from + " with " + to);
         LOGGER.debug("from instance id  " + from.getInstanceId());
      }
      final Model model = from.getModel();
      final Instance instance = model.getInstance(from.getInstanceId());
      assert instance == to.getModel().getInstance(to.getInstanceId());
      final String fromLocationPath = from.getLocationPath();
      final String toLocationPath = to.getLocationPath();
      
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("from {id: " + from.getId() + ",position: " + from.getPosition() +
                      "} " + fromLocationPath + 
                      "=" + instance.getModelItem(fromLocationPath).getValue());
         LOGGER.debug("to {id:" + to.getId() + ",position: " + to.getPosition() +
                      "} " + toLocationPath + 
                      "=" + instance.getModelItem(toLocationPath).getValue());
      }

      String beforeLocation = toLocationPath;
      final int toPosition = to.getPosition();
      if (from.getPosition() < toPosition)
      {
         final RepeatItem beforeItem = to.getRepeat().getRepeatItem(toPosition + 1);
         beforeLocation = (beforeItem != null
                           ? beforeItem.getLocationPath()
                           : to.getRepeat().getLocationPath().replaceAll("\\[position\\(\\)[\\s]*!=[\\s]*last\\(\\)]$",
                                                                         "[position()=last()]"));
      }
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("inserting node before " + beforeLocation);
      instance.insertNode(fromLocationPath, beforeLocation);

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("deleting from " + from.getLocationPath());
      // need to reload from location path since it has moved
      instance.deleteNode(from.getLocationPath());

      to.getRepeat().setIndex(toPosition);

      model.getContainer().dispatch(model.getTarget(), XFormsEventNames.REBUILD, null);
      model.getContainer().dispatch(model.getTarget(), XFormsEventNames.RECALCULATE, null);
      model.getContainer().dispatch(model.getTarget(), XFormsEventNames.REVALIDATE, null);
      model.getContainer().dispatch(model.getTarget(), XFormsEventNames.REFRESH, null);

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("swapped model data, instance data after manipulation:\n " + 
                      XMLUtil.toString(instance.getInstanceDocument()));
      }
   }

   private static void rewriteInlineURIs(final Document schemaDocument,
                                         final String cwdAvmPath)
   {
      final NodeList nl =
         XMLUtil.combine(schemaDocument.getElementsByTagNameNS(NamespaceConstants.XMLSCHEMA_NS, "include"),
                         schemaDocument.getElementsByTagNameNS(NamespaceConstants.XMLSCHEMA_NS, "import"));
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("rewriting " + nl.getLength() + " includes");
      
      for (int i = 0; i < nl.getLength(); i++)
      {
         final Element includeEl = (Element)nl.item(i);
         if (includeEl.hasAttribute("schemaLocation"))
         {
            String uri      = includeEl.getAttribute("schemaLocation");
            String finalURI = null;

            if (uri == null || uri.startsWith("http://") || uri.startsWith("https://"))
            {
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("not rewriting " + uri);
               
               continue;
            }

            if (uri.startsWith("webscript://"))
            {
               // It's a web script include / import
               final FacesContext       facesContext    = FacesContext.getCurrentInstance();
               final ExternalContext    externalContext = facesContext.getExternalContext();
               final HttpServletRequest request         = (HttpServletRequest)externalContext.getRequest();
               
               final String baseURI = (request.getScheme() + "://" +
                                       request.getServerName() + ':' +
                                       request.getServerPort() +
                                       request.getContextPath() + "/wcservice");
               String rewrittenURI = uri;
               
               if (uri.contains("${storeid}"))
               {
                  final String storeId = AVMUtil.getStoreName(cwdAvmPath);
                  rewrittenURI         = uri.replace("${storeid}", storeId);
               }
               else if (uri.contains("{storeid}"))
               {
                  final String storeId = AVMUtil.getStoreName(cwdAvmPath);
                  rewrittenURI         = uri.replace("{storeid}", storeId);
               }
               else
               {
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug("no store id specified in webscript URI " + uri);
               }
               
               if (uri.contains("${ticket}"))
               {
                  AuthenticationService authenticationService = Repository.getServiceRegistry(facesContext).getAuthenticationService();
                  final String ticket = authenticationService.getCurrentTicket();
                  rewrittenURI        = rewrittenURI.replace("${ticket}", ticket);
               }
               else if (uri.contains("{ticket}"))
               {
                  AuthenticationService authenticationService = Repository.getServiceRegistry(facesContext).getAuthenticationService();
                  final String ticket = authenticationService.getCurrentTicket();
                  rewrittenURI        = rewrittenURI.replace("{ticket}", ticket);
               }
               else
               {
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug("no ticket specified in webscript URI " + uri);
               }
               
               rewrittenURI = rewrittenURI.replaceAll("%26","&");
               
               finalURI = baseURI + rewrittenURI.replace("webscript://", "/");
               
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("Final URI " + finalURI);
            }
            else
            {
               // It's a web project asset include / import
               final String baseURI = (uri.charAt(0) == '/'
                                       ? AVMUtil.getPreviewURI(AVMUtil.getStoreName(cwdAvmPath))
                                       : AVMUtil.getPreviewURI(cwdAvmPath));

               finalURI = baseURI + uri;
            }

            if (LOGGER.isDebugEnabled())
               LOGGER.debug("rewriting " + uri + " as " + finalURI);
            
            includeEl.setAttribute("schemaLocation", finalURI);
         }
      }
   }

   private Node getEventLog()
   {
      final Document result = XMLUtil.newDocument();
      final Element eventsElement = result.createElement("events");
      result.appendChild(eventsElement);
      
      for (XMLEvent xfe : this.xformsSession.eventLog)
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

         if (LOGGER.isDebugEnabled() && XFormsEventNames.SUBMIT_ERROR.equals(type))
         {
			// debug for figuring out which elements aren't valid for submit
			LOGGER.debug("performing full revalidate");
			try
			{
			   final Model model = this.xformsSession.chibaBean.getContainer().getDefaultModel();
			   final Instance instance = model.getDefaultInstance();
			   model.getValidator().validate(instance, "/", new DefaultValidatorMode());
			   final Iterator<ModelItem> it = instance.iterateModelItems("/");
			   while (it.hasNext())
			   {
				  final ModelItem modelItem = it.next();
				  if (!modelItem.isValid())
				  {
					 LOGGER.debug("model node " + modelItem.getNode() + " is invalid");
				  }
				  if (modelItem.isRequired() && modelItem.getValue().length() == 0)
				  {
					 LOGGER.debug("model node " + modelItem.getNode() + " is empty and required");
				  }
			   }
			}
			catch (final XFormsException xfe2)
			{
			   LOGGER.debug("error performing revaliation", xfe2);
			}
		 }
	  }
      this.xformsSession.eventLog.clear();

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("generated event log:\n" + XMLUtil.toString(result));
      }
      return result;
   }

   /**
    * stores cookies that may exist in request and passes them on to processor for usage in
    * HTTPConnectors. Instance loading and submission then uses these cookies. Important for
    * applications using auth.
    */
//   @SuppressWarnings("unchecked")
//   private static void storeCookies(final javax.servlet.http.Cookie[] cookiesIn,
//                                    final ChibaBean chibaBean){
//      if (cookiesIn != null) {
//         org.apache.commons.httpclient.Cookie[] commonsCookies = 
//            new org.apache.commons.httpclient.Cookie[cookiesIn.length];
//         for (int i = 0; i < cookiesIn.length; i += 1) {
//            commonsCookies[i] =
//               new org.apache.commons.httpclient.Cookie(cookiesIn[i].getDomain(),
//                                                        cookiesIn[i].getName(),
//                                                        cookiesIn[i].getValue(),
//                                                        cookiesIn[i].getPath(),
//                                                        cookiesIn[i].getMaxAge(),
//                                                        cookiesIn[i].getSecure());
//         }
//         chibaBean.getContext().put(AbstractHTTPConnector.REQUEST_COOKIE,
//                                    commonsCookies);
//      }
//   }
   
   private Pair<Document, XSModel> getXFormsDocument()
      throws FormBuilderException
   {
      String path = null;
      if (this.xformsSession.form.isWebForm())
      {
         path = this.getCurrentAVMPath();
      }
      else
      {
         path = this.getCurrentPath();
      }
      
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("building xform for schema " + this.xformsSession.form.getName() +
                      " root element " + this.xformsSession.form.getSchemaRootElementName() +
                      " cwd " + path);
      }

      final Locale locale = 
         Application.getLanguage(FacesContext.getCurrentInstance());
      final ResourceBundle resourceBundle = 
         getSchema2XFormsProperties().getResourceBundle(this.xformsSession.form, 
                                                        locale);
      try
      {
         final Document schemaDocument = this.xformsSession.form.getSchema();
         XFormsBean.rewriteInlineURIs(schemaDocument, path);
         final String rootElementName = this.xformsSession.form.getSchemaRootElementName();
         final Pair<Document, XSModel> result = 
            this.xformsSession.schema2XForms.buildXForm(this.xformsSession.formInstanceData, 
                                                        schemaDocument,
                                                        rootElementName,
                                                        resourceBundle);
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("generated xform: " + XMLUtil.toString(result.getFirst()));
         }
         return result;
      }
      catch (IOException ioe)
      {
         throw new FormBuilderException(ioe);
      }
      catch (SAXException saxe)
      {
         throw new FormBuilderException(saxe);
      }
   }

   private String getCurrentAVMPath()
   {
      AVMNode node = this.avmBrowseBean.getAvmActionNode();
      if (node == null)
      {
         return this.avmBrowseBean.getCurrentPath();
      }

      final String result = node.getPath();
      return node.isDirectory() ? result : AVMNodeConverter.SplitBase(result)[0];
   }
   
   private String getCurrentPath()
   {
      org.alfresco.web.bean.repository.Node node = this.navigator.getCurrentNode();
      if (node == null)
      {
         return null;
      }

      final String result = node.getPath();
      return result;
   }
}
