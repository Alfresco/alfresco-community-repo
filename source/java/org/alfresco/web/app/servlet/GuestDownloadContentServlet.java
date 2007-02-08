/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet responsible for streaming node content from the repo directly to the response stream.
 * The appropriate mimetype is calculated based on filename extension.
 * <p>
 * The URL to the servlet should be generated thus:
 * <pre>/alfresco/guestDownload/attach/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf</pre>
 * or
 * <pre>/alfresco/guestDownload/direct/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf</pre>
 * or
 * <pre>/alfresco/guestDownload/[direct|attach]?path=/Company%20Home/MyFolder/myfile.pdf</pre>
 * The protocol, followed by either the store and Id (NodeRef) or instead specify a name based
 * encoded Path to the content, note that the filename element is used for mimetype lookup and
 * as the returning filename for the response stream.
 * <p>
 * The 'attach' or 'direct' element is used to indicate whether to display the stream directly
 * in the browser or download it as a file attachment.
 * <p>
 * By default, the download assumes that the content is on the
 * {@link org.alfresco.model.ContentModel#PROP_CONTENT content property}.<br>
 * To retrieve the content of a specific model property, use a 'property' arg, providing the workspace,
 * node ID AND the qualified name of the property.
 * <p>
 * This servlet only accesses content available to the guest user. If the guest user does not
 * have access to the requested a 401 Forbidden response is returned to the caller.
 * <p>
 * This servlet does not effect the current session, therefore if guest access is required to a 
 * resource this servlet can be used without logging out the current user.
 * 
 * @author gavinc
 */
public class GuestDownloadContentServlet extends BaseDownloadContentServlet
{
   private static final long serialVersionUID = -5258137503339817457L;
   
   private static Log logger = LogFactory.getLog(GuestDownloadContentServlet.class);
   
   private static final String DOWNLOAD_URL  = "/guestDownload/attach/{0}/{1}/{2}/{3}";
   private static final String BROWSER_URL   = "/guestDownload/direct/{0}/{1}/{2}/{3}";
   
   @Override
   protected Log getLogger()
   {
      return logger;
   }
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      if (logger.isDebugEnabled())
      {
         String queryString = req.getQueryString();
         logger.debug("Setting up guest access to URL: " + req.getRequestURI() + 
               ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
      }
      
      DownloadContentWork dcw = new DownloadContentWork(req, res);
      AuthenticationUtil.runAs(dcw, PermissionService.GUEST_AUTHORITY);
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
      return generateUrl(DOWNLOAD_URL, ref, name);
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
      return generateUrl(BROWSER_URL, ref, name);
   }
   
   /**
    * Class to wrap the call to processDownloadRequest.
    * 
    * @author gavinc
    */
   public class DownloadContentWork implements RunAsWork<Object>
   {
      private HttpServletRequest req = null;
      private HttpServletResponse res = null;
      
      public DownloadContentWork(HttpServletRequest req, HttpServletResponse res)
      {
         this.req = req;
         this.res = res;
      }
      
      public Object doWork() throws Exception
      {
         processDownloadRequest(this.req, this.res, false);
         
         return null;
      }
   }   
}
