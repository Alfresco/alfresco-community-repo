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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet responsible for streaming node content from the repo directly to the response stream.
 * The appropriate mimetype is calculated based on filename extension.
 * <p>
 * The URL to the servlet should be generated thus:
 * <pre>/alfresco/download/attach/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf</pre>
 * or
 * <pre>/alfresco/download/direct/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf</pre>
 * <p>
 * The store protocol, followed by the store ID, followed by the content Node Id
 * the last element is used for mimetype calculation and browser default filename.
 * <p>
 * The 'attach' or 'direct' element is used to indicate whether to display the stream directly
 * in the browser or download it as a file attachment.
 * <p>
 * By default, the download assumes that the content is on the
 * {@link org.alfresco.model.ContentModel#PROP_CONTENT content property}.<br>
 * To retrieve the content of a specific model property, use a 'property' arg, providing the workspace,
 * node ID AND the qualified name of the property.
 * <p>
 * Like most Alfresco servlets, the URL may be followed by a valid 'ticket' argument for authentication:
 * ?ticket=1234567890
 * <p>
 * And/or also followed by the "?guest=true" argument to force guest access login for the URL. 
 * 
 * @author Kevin Roast
 */
public class DownloadContentServlet extends BaseServlet
{
   private static final long serialVersionUID = -4558907921887235966L;
   
   private static Log logger = LogFactory.getLog(DownloadContentServlet.class);
   
   private static final String DOWNLOAD_URL  = "/download/attach/{0}/{1}/{2}/{3}";
   private static final String BROWSER_URL   = "/download/direct/{0}/{1}/{2}/{3}";
   
   private static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
   
   private static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
   
   private static final String ARG_PROPERTY = "property";
   private static final String ARG_ATTACH   = "attach";
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      // The URL contains multiple parts
      // /alfresco/download/attach/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf
      // the protocol, followed by the store, followed by the Id
      // the last part is only used for mimetype and browser use
      // may be followed by valid ticket for pre-authenticated usage: ?ticket=1234567890 
      String uri = req.getRequestURI();
      
      if (logger.isDebugEnabled())
         logger.debug("Processing URL: " + uri + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
      
      AuthenticationStatus status = servletAuthenticate(req, res);
      if (status == AuthenticationStatus.Failure)
      {
         return;
      }
      
      // TODO: add compression here?
      //       see http://servlets.com/jservlet2/examples/ch06/ViewResourceCompress.java for example
      //       only really needed if we don't use the built in compression of the servlet container
      uri = uri.substring(req.getContextPath().length());
      StringTokenizer t = new StringTokenizer(uri, "/");
      if (t.countTokens() < 6)
      {
         throw new IllegalArgumentException("Download URL did not contain all required args: " + uri); 
      }
      
      t.nextToken();    // skip servlet name
      
      String attachToken = t.nextToken();
      boolean attachment = attachToken.equals(ARG_ATTACH);
      
      StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
      String id = t.nextToken();
      String filename = t.nextToken();
      
      // get property qualified name
      QName propertyQName = null;
      String property = req.getParameter(ARG_PROPERTY);
      if (property == null || property.length() == 0)
      {
          propertyQName = ContentModel.PROP_CONTENT;
      }
      else
      {
          propertyQName = QName.createQName(property);
      }
      
      // build noderef from the appropriate URL elements
      NodeRef nodeRef = new NodeRef(storeRef, id);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Found NodeRef: " + nodeRef.toString());
         logger.debug("Will use filename: " + filename);
         logger.debug("For property: " + propertyQName);
         logger.debug("With attachment mode: " + attachment);
      }
      
      // get the services we need to retrieve the content
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      ContentService contentService = serviceRegistry.getContentService();
      PermissionService permissionService = serviceRegistry.getPermissionService();
      
      try
      {
         // check that the user has at least READ_CONTENT access - else redirect to the login page
         if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
         {
            if (logger.isDebugEnabled())
               logger.debug("User does not have permissions to read content for NodeRef: " + nodeRef.toString());
            redirectToLoginPage(req, res, getServletContext());
            return;
         }
         
         if (attachment == true)
         {
            // set header based on filename - will force a Save As from the browse if it doesn't recognise it
            // this is better than the default response of the browser trying to display the contents
            String encname = filename.replace('%', '=');
            res.setHeader("Content-Disposition", "attachment;filename=\"=?ISO-8859-1?Q?" + encname + "?=\"");
         }
         
         // get the content reader
         ContentReader reader = contentService.getReader(nodeRef, propertyQName);
         // ensure that it is safe to use
         reader = FileContentReader.getSafeContentReader(
                    reader,
                    Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                    nodeRef, reader);
         
         String mimetype = reader.getMimetype();
         // fall back if unable to resolve mimetype property
         if (mimetype == null || mimetype.length() == 0)
         {
            MimetypeService mimetypeMap = serviceRegistry.getMimetypeService();
            mimetype = MIMETYPE_OCTET_STREAM;
            int extIndex = filename.lastIndexOf('.');
            if (extIndex != -1)
            {
               String ext = filename.substring(extIndex + 1);
               String mt = mimetypeMap.getMimetypesByExtension().get(ext);
               if (mt != null)
               {
                  mimetype = mt;
               }
            }
         }
         res.setContentType(mimetype);
         
         // get the content and stream directly to the response output stream
         // assuming the repo is capable of streaming in chunks, this should allow large files
         // to be streamed directly to the browser response stream.
         try
         {
            reader.getContent( res.getOutputStream() );
         }
         catch (SocketException e)
         {
            if (e.getMessage().contains("ClientAbortException"))
            {
               // the client cut the connection - our mission was accomplished apart from a little error message
               logger.error("Client aborted stream read:\n   node: " + nodeRef + "\n   content: " + reader);
            }
            else
            {
               throw e;
            }
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error during download content servlet processing: " + err.getMessage(), err);
      }
   }
   
   /**
    * Helper to generate a URL to a content node for downloading content from the server.
    * The content is supplied as an HTTP1.1 attachment to the response. This generally means
    * a browser should prompt the user to save the content to specified location.
    * 
    * @param ref     NodeRef of the content node to generate URL for (cannot be null)
    * @param name    File name to return in the URL (cannot be null)
    * 
    * @return URL to download the content from the specified node
    */
   public final static String generateDownloadURL(NodeRef ref, String name)
   {
      String url = null;
      
      try
      {
         url = MessageFormat.format(DOWNLOAD_URL, new Object[] {
                  ref.getStoreRef().getProtocol(),
                  ref.getStoreRef().getIdentifier(),
                  ref.getId(),
                  Utils.replace(URLEncoder.encode(name, "UTF-8"), "+", "%20") } );
      }
      catch (UnsupportedEncodingException uee)
      {
         throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + ref, uee);
      }
      
      return url;
   }
   
   /**
    * Helper to generate a URL to a content node for downloading content from the server.
    * The content is supplied directly in the reponse. This generally means a browser will
    * attempt to open the content directly if possible, else it will prompt to save the file.
    * 
    * @param ref     NodeRef of the content node to generate URL for (cannot be null)
    * @param name    File name to return in the URL (cannot be null)
    * 
    * @return URL to download the content from the specified node
    */
   public final static String generateBrowserURL(NodeRef ref, String name)
   {
      String url = null;
      
      try
      {
         url = MessageFormat.format(BROWSER_URL, new Object[] {
                  ref.getStoreRef().getProtocol(),
                  ref.getStoreRef().getIdentifier(),
                  ref.getId(),
                  Utils.replace(URLEncoder.encode(name, "UTF-8"), "+", "%20") } );
      }
      catch (UnsupportedEncodingException uee)
      {
         throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + ref, uee);
      }
      
      return url;
   }
}
