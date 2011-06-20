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
package org.alfresco.web.ui.common;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.server.config.ServerConfigurationAccessor;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.repo.webdav.WebDAVServlet;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.ExternalAccessServlet;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.ui.common.component.UIStatusMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlFormRendererBase;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.webscripts.ui.common.StringUtils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Class containing misc helper methods used by the JSF components.
 * 
 * @author Kevin Roast
 */
public final class Utils extends StringUtils
{
   public static final String USER_AGENT_FIREFOX = "Firefox";
   public static final String USER_AGENT_MSIE = "MSIE";
   private static final String MSG_TIME_PATTERN = "time_pattern";
   private static final String MSG_DATE_PATTERN = "date_pattern";
   private static final String MSG_DATE_TIME_PATTERN = "date_time_pattern";
   
   private static final Log logger = LogFactory.getLog(Utils.class);
   
   /**
    * Private constructor
    */
   private Utils()
   {
   }
   
   /**
    * Helper to output an attribute to the output stream
    * 
    * @param out        ResponseWriter
    * @param attr       attribute value object (cannot be null)
    * @param mapping    mapping to output as e.g. style="..."
    * 
    * @throws IOException
    */
   public static void outputAttribute(ResponseWriter out, Object attr, String mapping)
      throws IOException
   {
      if (attr != null)
      {
         out.write(' ');
         out.write(mapping);
         out.write("=\"");
         out.write(attr.toString());
         out.write('"');
      }
   }
   
   /**
    * Get the hidden field name for any action component.
    * 
    * All components that wish to simply encode a form value with their client ID can reuse the same
    * hidden field within the parent form. NOTE: components which use this method must only encode
    * their client ID as the value and nothing else!
    * 
    * Build a shared field name from the parent form name and the string "act".
    * 
    * @return hidden field name shared by all action components within the Form.
    */
   public static String getActionHiddenFieldName(FacesContext context, UIComponent component)
   {
      return Utils.getParentForm(context, component).getClientId(context) + NamingContainer.SEPARATOR_CHAR + "act";
   }
   
   /**
    * Helper to recursively render a component and it's child components
    * 
    * @param context    FacesContext
    * @param component  UIComponent
    * 
    * @throws IOException
    */
   public static void encodeRecursive(FacesContext context, UIComponent component)
      throws IOException
   {
      if (component.isRendered() == true)
      {
         component.encodeBegin(context);
         
         // follow the spec for components that render their children
         if (component.getRendersChildren() == true)
         {
            component.encodeChildren(context);
         }
         else
         {
            if (component.getChildCount() != 0)
            {
               for (Iterator i=component.getChildren().iterator(); i.hasNext(); /**/)
               {
                  encodeRecursive(context, (UIComponent)i.next());
               }
            }
         }
         
         component.encodeEnd(context);
      }
   }
   
   /**
    * Generate the JavaScript to submit set the specified hidden Form field to the
    * supplied value and submit the parent Form.
    * 
    * NOTE: the supplied hidden field name is added to the Form Renderer map for output.
    * 
    * @param context       FacesContext
    * @param component     UIComponent to generate JavaScript for
    * @param fieldId       Hidden field id to set value for
    * @param fieldValue    Hidden field value to set hidden field too on submit
    * 
    * @return JavaScript event code
    */
   public static String generateFormSubmit(FacesContext context, UIComponent component, String fieldId, String fieldValue)
   {
      return generateFormSubmit(context, component, fieldId, fieldValue, false, null);
   }
   
   /**
    * Generate the JavaScript to submit set the specified hidden Form field to the
    * supplied value and submit the parent Form.
    * 
    * NOTE: the supplied hidden field name is added to the Form Renderer map for output.
    * 
    * @param context       FacesContext
    * @param component     UIComponent to generate JavaScript for
    * @param fieldId       Hidden field id to set value for
    * @param fieldValue    Hidden field value to set hidden field too on submit
    * @param params        Optional map of param name/values to output
    * 
    * @return JavaScript event code
    */
   public static String generateFormSubmit(FacesContext context, UIComponent component, String fieldId, 
         String fieldValue, Map<String, String> params)
   {
      return generateFormSubmit(context, component, fieldId, fieldValue, false, params);
   }
      
   /**
    * Generate the JavaScript to submit set the specified hidden Form field to the
    * supplied value and submit the parent Form.
    * 
    * NOTE: the supplied hidden field name is added to the Form Renderer map for output.
    * 
    * @param context       FacesContext
    * @param component     UIComponent to generate JavaScript for
    * @param fieldId       Hidden field id to set value for
    * @param fieldValue    Hidden field value to set hidden field too on submit
    * @param valueIsParam  Determines whether the fieldValue parameter should be treated
    *                      as a parameter in the generated JavaScript, false will treat
    *                      the value i.e. surround it with single quotes
    * @param params        Optional map of param name/values to output
    * 
    * @return JavaScript event code
    */
   public static String generateFormSubmit(FacesContext context, UIComponent component, String fieldId, 
         String fieldValue, boolean valueIsParam, Map<String, String> params)
   {
      UIForm form = Utils.getParentForm(context, component);
      if (form == null)
      {
         throw new IllegalStateException("Must nest components inside UIForm to generate form submit!");
      }
      
      String formClientId = form.getClientId(context);
      
      StringBuilder buf = new StringBuilder(200);
      buf.append("document.forms['");
      buf.append(formClientId);
      buf.append("']['");
      buf.append(fieldId);
      buf.append("'].value=");
      if (valueIsParam == false)
      {
         buf.append("'");
      }
      buf.append(Utils.encode(fieldValue));
      if (valueIsParam == false)
      {
         buf.append("'");
      }
      buf.append(";");
      
      if (params != null)
      {
         for (String name : params.keySet())
         {
            buf.append("document.forms['");
            buf.append(formClientId);
            buf.append("']['");
            buf.append(name);
            buf.append("'].value='");
            String val = params.get(name);
            if (val != null)
            {
               val = Utils.encode(val);
            }
            val = replace(val, "\\", "\\\\");   // encode escape character
            val = replace(val, "'", "\\'");     // encode single quote as we wrap string with that
            buf.append(val);
            buf.append("';");
            
            // weak, but this seems to be the way Sun RI do it...
            //FormRenderer.addNeededHiddenField(context, name);
            HtmlFormRendererBase.addHiddenCommandParameter(context, form, name);
         }
      }
      
      buf.append("document.forms['");
      buf.append(formClientId);
      buf.append("'].submit();");
      
      if (valueIsParam == false)
      {
         buf.append("return false;");
      }
      
      // weak, but this seems to be the way Sun RI do it...
      //FormRenderer.addNeededHiddenField(context, fieldId);
      HtmlFormRendererBase.addHiddenCommandParameter(context, form, fieldId);
      
      return buf.toString();
   }
   
   /**
    * Generate the JavaScript to submit the parent Form.
    * 
    * @param context       FacesContext
    * @param component     UIComponent to generate JavaScript for
    * 
    * @return JavaScript event code
    */
   public static String generateFormSubmit(FacesContext context, UIComponent component)
   {
      UIForm form = Utils.getParentForm(context, component);
      if (form == null)
      {
         throw new IllegalStateException("Must nest components inside UIForm to generate form submit!");
      }
      
      String formClientId = form.getClientId(context);
      
      StringBuilder buf = new StringBuilder(48);
      
      buf.append("document.forms['");
      buf.append(formClientId);
      buf.append("'].submit()");
      
      buf.append(";return false;");
      
      return buf.toString();
   }
   
   /**
    * Enum representing the client URL type to generate
    */
   public enum URLMode {HTTP_DOWNLOAD, HTTP_INLINE, WEBDAV, CIFS, SHOW_DETAILS, BROWSE, FTP}
   
   /**
    * Generates a URL for the given usage for the given node.
    * 
    * The supported values for the usage parameter are of URLMode enum type
    * @see URLMode
    * 
    * @param context    Faces context
    * @param node       The node to generate the URL for
    * @param name       Name to use for the download file part of the link if any
    * @param usage      What the URL is going to be used for
    * 
    * @return The URL for the requested usage without the context path
    */
   public static String generateURL(FacesContext context, Node node, String name, URLMode usage)
   {
      String url = null;
      
      switch (usage)
      {
         case WEBDAV:
         {
            // calculate a WebDAV URL for the given node
            FileFolderService fileFolderService = Repository.getServiceRegistry(
                  context).getFileFolderService();
            try
            {
               List<FileInfo> paths = fileFolderService.getNamePath(null, node.getNodeRef());
               
               // build up the webdav url
               StringBuilder path = new StringBuilder("/").append(WebDAVServlet.WEBDAV_PREFIX);
               
               // build up the path skipping the first path as it is the root folder
               for (int x = 1; x < paths.size(); x++)
               {
                  path.append("/").append(WebDAVHelper.encodeURL(paths.get(x).getName(), getUserAgent(context)));
               }
               url = path.toString();
            }
            catch (AccessDeniedException e)
            {
               // cannot build path if user don't have access all the way up
            }
            catch (FileNotFoundException nodeErr)
            {
               // cannot build path if file no longer exists
            }
            break;
         }
         
         case CIFS:
         {
            // calculate a CIFS path for the given node
            
            // get hold of the node service, cifsServer and navigation bean
            ServiceRegistry serviceRegistry = Repository.getServiceRegistry(context); 
            NodeService nodeService = serviceRegistry.getNodeService();
            FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
            NavigationBean navBean = (NavigationBean)context.getExternalContext().
                  getSessionMap().get(NavigationBean.BEAN_NAME);
            ServerConfigurationAccessor serverConfiguration = (ServerConfigurationAccessor)FacesContextUtils.getRequiredWebApplicationContext(
                  context).getBean("fileServerConfiguration");
            
            if (nodeService != null && fileFolderService != null && navBean != null && serverConfiguration != null)
            {
               // Resolve CIFS network folder location for this node
               FilesystemsConfigSection filesysConfig = (FilesystemsConfigSection)serverConfiguration.getConfigSection(FilesystemsConfigSection.SectionName); 
               DiskSharedDevice diskShare = null;
               
               SharedDeviceList shares = filesysConfig.getShares();
               Enumeration<SharedDevice> shareEnum = shares.enumerateShares();
               
               while (shareEnum.hasMoreElements() && diskShare == null) 
               { 
                  SharedDevice curShare = shareEnum.nextElement(); 
                  if (curShare.getContext() instanceof ContentContext) 
                  { 
                     // ALF-6863: Check if the node has a path beneath contentContext.getRootNode() 
                     ContentContext contentContext = (ContentContext) curShare.getContext(); 
                     NodeRef rootNode = contentContext.getRootNode(); 
                     if (node.getNodeRef().equals(rootNode)) 
                     { 
                         diskShare = (DiskSharedDevice) curShare; 
                         break; 
                     } 
                     try 
                     { 
                         fileFolderService.getNamePath(rootNode, node.getNodeRef()); 
                         if (logger.isDebugEnabled()) 
                         { 
                             logger.debug(" Node " + node.getName() + " HAS been found on " + contentContext.getDeviceName()); 
                         } 
                         diskShare = (DiskSharedDevice) curShare; 
                         break; 
                     } 
                     catch (FileNotFoundException ex) 
                     { 
                         if (logger.isDebugEnabled()) 
                         { 
                             logger.debug(" Node " + node.getName() + " HAS NOT been found on " + contentContext.getDeviceName()); 
                         } 
                         // There is no such node on this SharedDevice, continue 
                         continue; 
                     } 
                  } 
               }
               
               if (diskShare != null)
               {
                  ContentContext contentCtx = (ContentContext)diskShare.getContext();
                  NodeRef rootNode = contentCtx.getRootNode();
                  try
                  {
                     url = Repository.getNamePathEx(context, node.getNodePath(), rootNode, "\\", 
                           "file:///" + navBean.getCIFSServerPath(diskShare));
                  }
                  catch (AccessDeniedException e)
                  {
                     // cannot build path if user don't have access all the way up
                  }
                  catch (InvalidNodeRefException nodeErr)
                  {
                     // cannot build path if node no longer exists
                  }
               }
            }
            break;
         }
         
         case HTTP_DOWNLOAD:
         {
            url = DownloadContentServlet.generateDownloadURL(node.getNodeRef(), name);
            break;
         }
         
         case HTTP_INLINE:
         {
            url = DownloadContentServlet.generateBrowserURL(node.getNodeRef(), name);
            break;
         }
         
         case SHOW_DETAILS:
         {
            DictionaryService dd = Repository.getServiceRegistry(context).getDictionaryService();
            
            // default to showing details of content
            String outcome = ExternalAccessServlet.OUTCOME_DOCDETAILS;
            
            // if the node is a type of folder then make the outcome to show space details
            if ((dd.isSubClass(node.getType(), ContentModel.TYPE_FOLDER)) ||
                  (dd.isSubClass(node.getType(), ApplicationModel.TYPE_FOLDERLINK)))
            {
               outcome = ExternalAccessServlet.OUTCOME_SPACEDETAILS;
            }
            
            // build the url
            url = ExternalAccessServlet.generateExternalURL(outcome, 
                  Repository.getStoreRef().getProtocol() + "/" + 
                  Repository.getStoreRef().getIdentifier() + "/" + node.getId());
            break;
         }
         
         case BROWSE:
         {
            url = ExternalAccessServlet.generateExternalURL(ExternalAccessServlet.OUTCOME_BROWSE, 
                  Repository.getStoreRef().getProtocol() + "/" + 
                  Repository.getStoreRef().getIdentifier() + "/" + node.getId());
         }
         
         case FTP:
         {
            // not implemented yet!
            break;
         }
      }
      
      return url;
   }
   
   /**
    * Generates a URL for the given usage for the given node.
    * 
    * The supported values for the usage parameter are of URLMode enum type
    * @see URLMode
    * 
    * @param context    Faces context
    * @param node       The node to generate the URL for
    * @param usage      What the URL is going to be used for
    * 
    * @return The URL for the requested usage without the context path
    */
   public static String generateURL(FacesContext context, Node node, URLMode usage)
   {
      return generateURL(context, node, node.getName(), usage);
   }
   
   /**
    * Build a context path safe image tag for the supplied image path.
    * Image path should be supplied with a leading slash '/'.
    * 
    * @param context       FacesContext
    * @param image         The local image path from the web folder with leading slash '/'
    * @param width         Width in pixels
    * @param height        Height in pixels
    * @param alt           Optional alt/title text
    * @param onclick       JavaScript onclick event handler code
    * 
    * @return Populated <code>img</code> tag
    */
   public static String buildImageTag(FacesContext context, String image, int width, int height,
         String alt, String onclick)
   {
      return buildImageTag(context, image, width, height, alt, onclick, null);
   }

   /**
    * Build a context path safe image tag for the supplied image path.
    * Image path should be supplied with a leading slash '/'.
    * 
    * @param context       FacesContext
    * @param image         The local image path from the web folder with leading slash '/'
    * @param width         Width in pixels
    * @param height        Height in pixels
    * @param alt           Optional alt/title text
    * @param onclick       JavaScript onclick event handler code
    * @param verticalAlign Optional HTML alignment value
    * 
    * @return Populated <code>img</code> tag
    */
   public static String buildImageTag(FacesContext context, String image, int width, int height,
                                      String alt, String onclick, String verticalAlign)
   {
       return buildImageTag(context, image, width, height, alt, onclick, verticalAlign, null);
   }
   
   /**
    * Build a context path safe image tag for the supplied image path.
    * Image path should be supplied with a leading slash '/'.
    * 
    * @param context       FacesContext
    * @param image         The local image path from the web folder with leading slash '/'
    * @param width         Width in pixels
    * @param height        Height in pixels
    * @param alt           Optional alt/title text
    * @param onclick       JavaScript onclick event handler code
    * @param verticalAlign Optional HTML alignment value
    * @param style         Optional inline CSS styling
    * 
    * @return Populated <code>img</code> tag
    */
   public static String buildImageTag(FacesContext context, String image, int width, int height,
                                      String alt, String onclick, String verticalAlign, String style)
   {
      StringBuilder buf = new StringBuilder(200);
      
      style = style != null ? "border-width:0px; " + style : "border-width:0px;";
      buf.append("<img src='")
         .append(context.getExternalContext().getRequestContextPath())
         .append(image)
         .append("' width='")
         .append(width)
         .append("' height='")
         .append(height)
         .append("'");
      
      if (alt != null)
      {
         alt = Utils.encode(alt);
         buf.append(" alt=\"")
            .append(alt)
            .append("\" title=\"")
            .append(alt)
            .append("\"");
      }
      else
      {
         buf.append(" alt=''");
      }

      if (verticalAlign != null)
      {
         StringBuilder styleBuf = new StringBuilder(40);
         styleBuf.append(style).append("vertical-align:").append(verticalAlign).append(";");
         style = styleBuf.toString();
      }
      
      if (onclick != null)
      {
         buf.append(" onclick=\"").append(onclick).append('"');
         StringBuilder styleBuf = new StringBuilder(style.length() + 16);
         styleBuf.append(style).append("cursor:pointer;");
         style = styleBuf.toString();
      }
      buf.append(" style='").append(style).append("'/>");
      
      return buf.toString();
   }
   
   /**
    * Build a context path safe image tag for the supplied image path.
    * Image path should be supplied with a leading slash '/'.
    * 
    * @param context       FacesContext
    * @param image         The local image path from the web folder with leading slash '/'
    * @param width         Width in pixels
    * @param height        Height in pixels
    * @param alt           Optional alt/title text
    * 
    * @return Populated <code>img</code> tag
    */
   public static String buildImageTag(FacesContext context, String image, int width, int height, String alt)
   {
      return buildImageTag(context, image, width, height, alt, null);
   }
   
   /**
    * Build a context path safe image tag for the supplied image path.
    * Image path should be supplied with a leading slash '/'.
    * 
    * @param context       FacesContext
    * @param image         The local image path from the web folder with leading slash '/'
    * @param alt           Optional alt/title text
    * 
    * @return Populated <code>img</code> tag
    */
   public static String buildImageTag(FacesContext context, String image, String alt)
   {
      return buildImageTag(context, image, alt, null);
   }
   
   /**
    * Build a context path safe image tag for the supplied image path.
    * Image path should be supplied with a leading slash '/'.
    * 
    * @param context       FacesContext
    * @param image         The local image path from the web folder with leading slash '/'
    * @param alt           Optional alt/title text
    * @param verticalAlign         Optional HTML alignment value
    * 
    * @return Populated <code>img</code> tag
    */
   public static String buildImageTag(FacesContext context, String image, String alt, String verticalAlign)
   {
      StringBuilder buf = new StringBuilder(128);
      buf.append("<img src='")
         .append(context.getExternalContext().getRequestContextPath())
         .append(image)
         .append("' ");

      String style = "border-width:0px;";
      if (alt != null)
      {
         alt = Utils.encode(alt);
         buf.append(" alt=\"")
            .append(alt)
            .append("\" title=\"")
            .append(alt)
            .append('"');
      }
      else
      {
         buf.append(" alt=''");
      }

      if (verticalAlign != null)
      {
         StringBuilder styleBuf = new StringBuilder(40);
         styleBuf.append(style).append("vertical-align:").append(verticalAlign).append(";");
         style = styleBuf.toString();
      }
      
      buf.append(" style='").append(style).append("'/>");
      
      return buf.toString();
   }
   
   /**
    * Return the parent UIForm component for the specified UIComponent
    * 
    * @param context       FaceContext
    * @param component     The UIComponent to find parent Form for
    * 
    * @return UIForm parent or null if none found in hiearachy
    */
   public static UIForm getParentForm(FacesContext context, UIComponent component)
   {
      UIComponent parent = component.getParent();
      while (parent != null)
      {
         if (parent instanceof UIForm)
         {
            break;
         }
         parent = parent.getParent();
      }
      return (UIForm)parent;
   }
   
   /**
    * Return the parent UIComponent implementing the NamingContainer interface for
    * the specified UIComponent.
    * 
    * @param context       FaceContext
    * @param component     The UIComponent to find parent Form for
    * 
    * @return NamingContainer parent or null if none found in hiearachy
    */
   public static UIComponent getParentNamingContainer(FacesContext context, UIComponent component)
   {
      UIComponent parent = component.getParent();
      while (parent != null)
      {
         if (parent instanceof NamingContainer)
         {
            break;
         }
         parent = parent.getParent();
      }
      return (UIComponent)parent;
   }
   
   /**
    * Return the parent UIComponent implementing the IDataContainer interface for
    * the specified UIComponent.
    * 
    * @param context       FaceContext
    * @param component     The UIComponent to find parent IDataContainer for
    * 
    * @return IDataContainer parent or null if none found in hiearachy
    */
   public static IDataContainer getParentDataContainer(FacesContext context, UIComponent component)
   {
      UIComponent parent = component.getParent();
      while (parent != null)
      {
         if (parent instanceof IDataContainer)
         {
            break;
         }
         parent = parent.getParent();
      }
      return (IDataContainer)parent;
   }
   
   /**
    * Determines whether the given component is disabled or readonly
    * 
    * @param component The component to test
    * @return true if the component is either disabled or set to readonly
    */
   public static boolean isComponentDisabledOrReadOnly(UIComponent component)
   {
      boolean disabled = false;
      boolean readOnly = false;
      
      Object disabledAttr = component.getAttributes().get("disabled");
      if (disabledAttr != null)
      {
         disabled = disabledAttr.equals(Boolean.TRUE);
      }
      
      if (disabled == false)
      {
         Object readOnlyAttr = component.getAttributes().get("readonly");
         if (readOnlyAttr != null)
         {
            readOnly = readOnlyAttr.equals(Boolean.TRUE);
         }
      }

      return disabled || readOnly;
   }
   
   /**
    * Invoke the method encapsulated by the supplied MethodBinding
    * 
    * @param context    FacesContext
    * @param method     MethodBinding to invoke
    * @param event      ActionEvent to pass to the method of signature:
    *                   public void myMethodName(ActionEvent event)
    */
   public static void processActionMethod(FacesContext context, MethodBinding method, ActionEvent event)
   {
      try
      {
         method.invoke(context, new Object[] {event});
      }
      catch (EvaluationException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof AbortProcessingException)
         {
            throw (AbortProcessingException)cause;
         }
         else
         {
            throw e;
         }
      }   
   }
   
   /**
    * Adds a global error message
    * 
    * @param msg        The error message
    */
   public static void addErrorMessage(String msg)
   {
      addErrorMessage(msg, null);
   }
   
   /**
    * Adds a global error message and logs exception details
    * 
    * @param msg        The error message
    * @param err        The exception to log
    */
   public static void addErrorMessage(String msg, Throwable err)
   {
      FacesContext context = FacesContext.getCurrentInstance( );
      FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
      context.addMessage(null, facesMsg);
      if (err != null)
      {
         if ((err instanceof InvalidNodeRefException == false &&
              err instanceof AccessDeniedException == false &&
              err instanceof NoTransformerException == false) || logger.isDebugEnabled())
         {
            logger.error(msg, err);
         }
      }
   }
   
   /**
    * Adds a global status message that will be displayed by a Status Message UI component
    * 
    * @param severity   Severity of the message
    * @param msg        Text of the message
    */
   public static void addStatusMessage(FacesMessage.Severity severity, String msg)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      String time = getTimeFormat(fc).format(new Date(System.currentTimeMillis()));
      FacesMessage fm = new FacesMessage(severity, time, msg);
      fc.addMessage(UIStatusMessage.STATUS_MESSAGE, fm);
   }
   
   /**
    * @return the formatter for locale sensitive Time formatting
    */
   public static DateFormat getTimeFormat(FacesContext fc)
   {
      return getDateFormatFromPattern(fc, Application.getMessage(fc, MSG_TIME_PATTERN));
   }
   
   /**
    * @return the formatter for locale sensitive Date formatting
    */
   public static DateFormat getDateFormat(FacesContext fc)
   {
      return getDateFormatFromPattern(fc, Application.getMessage(fc, MSG_DATE_PATTERN));
   }
   
   /**
    * @return the formatter for locale sensitive Date & Time formatting
    */
   public static DateFormat getDateTimeFormat(FacesContext fc)
   {
      return getDateFormatFromPattern(fc, Application.getMessage(fc, MSG_DATE_TIME_PATTERN));
   }
   
   /**
    * @return DataFormat object for the specified pattern
    */
   private static DateFormat getDateFormatFromPattern(FacesContext fc, String pattern)
   {
      if (pattern == null)
      {
         throw new IllegalArgumentException("DateTime pattern is mandatory.");
      }
      try
      {
         return new SimpleDateFormat(pattern, Application.getLanguage(fc));
      }
      catch (IllegalArgumentException err)
      {
         throw new AlfrescoRuntimeException("Invalid DateTime pattern", err);
      }
   }
   
   /**
    * Parse XML format date YYYY-MM-DDTHH:MM:SS
    * @param isoDate
    * @return Date or null if failed to parse
    */
   public static Date parseXMLDateFormat(String isoDate)
   {
      Date parsed = null;
      
      try
      {
         int offset = 0;
         
         // extract year
         int year = Integer.parseInt(isoDate.substring(offset, offset += 4));
         if (isoDate.charAt(offset) != '-')
         {
            throw new IndexOutOfBoundsException("Expected - character but found " + isoDate.charAt(offset));
         }
         
         // extract month
         int month = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
         if (isoDate.charAt(offset) != '-')
         {
            throw new IndexOutOfBoundsException("Expected - character but found " + isoDate.charAt(offset));
         }
         
         // extract day
         int day = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
         if (isoDate.charAt(offset) != 'T')
         {
            throw new IndexOutOfBoundsException("Expected T character but found " + isoDate.charAt(offset));
         }
         
         // extract hours, minutes, seconds and milliseconds
         int hour = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
         if (isoDate.charAt(offset) != ':')
         {
            throw new IndexOutOfBoundsException("Expected : character but found " + isoDate.charAt(offset));
         }
         int minutes = Integer.parseInt(isoDate.substring(offset += 1, offset += 2));
         if (isoDate.charAt(offset) != ':')
         {
            throw new IndexOutOfBoundsException("Expected : character but found " + isoDate.charAt(offset));
         }
         int seconds = Integer.parseInt(isoDate.substring(offset += 1 , offset += 2));
         
         // initialize Calendar object
         Calendar calendar = Calendar.getInstance();
         calendar.setLenient(false);
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.MONTH, month - 1);
         calendar.set(Calendar.DAY_OF_MONTH, day);
         calendar.set(Calendar.HOUR_OF_DAY, hour);
         calendar.set(Calendar.MINUTE, minutes);
         calendar.set(Calendar.SECOND, seconds);
         
         // extract the date
         parsed = calendar.getTime();
      }
      catch(IndexOutOfBoundsException e)
      {
      }
      catch(NumberFormatException e)
      {
      }
      catch(IllegalArgumentException e)
      {
      }
      
      return parsed;
   }

   
   
   /**
    * Given a ConfigElement instance retrieve the display label, this could be
    * dervied from a message bundle key or a literal string
    * 
    * @param context FacesContext
    * @param configElement The ConfigElement to test
    * @return The resolved display label
    */
   public static String getDisplayLabel(FacesContext context, ConfigElement configElement)
   {
      String label = null;
 
      // look for a localized string
      String msgId = configElement.getAttribute("display-label-id");
      if (msgId != null)
      {
         label = Application.getMessage(context, msgId);
      }
      
      // if there wasn't an externalized string look for a literal string
      if (label == null)
      {
         label = configElement.getAttribute("display-label");
      }
      
      return label;
   }
   
   /**
    * Given a ConfigElement instance retrieve the description, this could be
    * dervied from a message bundle key or a literal string
    * 
    * @param context FacesContext
    * @param configElement The ConfigElement to test
    * @return The resolved description
    */
   public static String getDescription(FacesContext context, ConfigElement configElement)
   {
      String description = null;
      
      // look for a localized string
      String msgId = configElement.getAttribute("description-id");
      if (msgId != null)
      {
         description = Application.getMessage(context, msgId);
      }
      
      // if there wasn't an externalized string look for a literal string
      if (description == null)
      {
         description = configElement.getAttribute("description");
      }
      
      return description;
   }

   /**
    * @return the browser User-Agent header value trimmed to either "Firefox" or "MSIE" as appropriate.
    */
   public static String getUserAgent(FacesContext context)
   {
      Object userAgent = context.getExternalContext().getRequestHeaderMap().get("User-Agent");
      if (userAgent != null)
      {
         if (userAgent.toString().indexOf("Firefox/") != -1)
         {
            return USER_AGENT_FIREFOX;
         }
         else if (userAgent.toString().indexOf("MSIE") != -1)
         {
            return USER_AGENT_MSIE;
         }
         else
         {
            return userAgent.toString();
         }
      }
      return "";
   }
   
   /**
    * Generate the QName sort for a standard Person lookup. The filter is
    * standardised across multiple JSF components and beans, and used with
    * {@link PersonService#getPeople(List, boolean, List, org.alfresco.query.PagingRequest)}
    */
   public static List<Pair<QName,Boolean>> generatePersonSort()
   {
      List<Pair<QName,Boolean>> sort = new ArrayList<Pair<QName,Boolean>>();
      sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, true));
      sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, true));
      sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, true));
      return sort;
   }
   
   /**
    * Generate the QName filter for a standard Person lookup. The filter is
    * standardised across multiple JSF components and beans, and used with
    * {@link PersonService#getPeople(List, boolean, List, org.alfresco.query.PagingRequest)}
    *  
    * @param term    Search term
    */
   public static List<Pair<QName,String>> generatePersonFilter(String term)
   {
      List<Pair<QName,String>> filter = new ArrayList<Pair<QName,String>>();
      filter.add(new Pair<QName, String>(ContentModel.PROP_FIRSTNAME, term));
      filter.add(new Pair<QName, String>(ContentModel.PROP_LASTNAME, term));
      filter.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, term));
      return filter;
   }
   
   /**
    * How many results should a person search return up to? This is needed
    * because the JSF components do paging differently.
    * For now, hard coded at 1000, may be configurable later
    */
   public static int getPersonMaxResults()
   {
      return 1000;
   }
   
   /**
    * Generate the Lucene query for a standard Person search. The query used is standardised
    * across multiple JSF components and beans.
    * 
    * @param query   Buffer for the query
    * @param term    Search term
    * @deprecated Use {@link #generatePersonFilter(String)} and {@link PersonService#getPeople(List, boolean, List, org.alfresco.query.PagingRequest)} instead
    */
   public static void generatePersonSearch(StringBuilder query, String term)
   {
      // define the query to find people by their first or last name
      for (StringTokenizer t = new StringTokenizer(term.trim(), " "); t.hasMoreTokens(); /**/)
      {
         String token = AbstractLuceneQueryParser.escape(t.nextToken());
         query.append("+TYPE:\"").append(ContentModel.TYPE_PERSON).append("\" ");
         query.append("+(@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:firstName:\"*");
         query.append(token);
         query.append("*\" @").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:lastName:\"*");
         query.append(token);
         query.append("*\" @").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:userName:");
         query.append(token);
         query.append("*) ");
      }
   }
}
