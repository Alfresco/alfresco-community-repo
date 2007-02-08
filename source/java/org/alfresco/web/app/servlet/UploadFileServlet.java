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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet that takes a file uploaded via a browser and represents it as an
 * UploadFileBean in the session
 * 
 * @author gavinc
 */
public class UploadFileServlet extends BaseServlet
{
   private static final long serialVersionUID = -5482538466491052873L;
   private static final Log logger = LogFactory.getLog(UploadFileServlet.class); 
   
   /**
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String uploadId = null;
      String returnPage = null;
      final RequestContext requestContext = new ServletRequestContext(request);
      boolean isMultipart = ServletFileUpload.isMultipartContent(requestContext);
      
      try
      {
         AuthenticationStatus status = servletAuthenticate(request, response);
         if (status == AuthenticationStatus.Failure)
         {
            return;
         }
         
         if (!isMultipart)
         {
            throw new AlfrescoRuntimeException("This servlet can only be used to handle file upload requests, make" +
                                        "sure you have set the enctype attribute on your form to multipart/form-data");
         }

         if (logger.isDebugEnabled())
            logger.debug("Uploading servlet servicing...");
         
         HttpSession session = request.getSession();
         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
         
         // ensure that the encoding is handled correctly
         upload.setHeaderEncoding("UTF-8");
         
         List<FileItem> fileItems = upload.parseRequest(request);
         
         FileUploadBean bean = new FileUploadBean();
         for (FileItem item : fileItems)
         {
            if(item.isFormField())
            {
               if (item.getFieldName().equalsIgnoreCase("return-page"))
               {
                  returnPage = item.getString();
               }
	       else if (item.getFieldName().equalsIgnoreCase("upload-id"))
               {
                  uploadId = item.getString();
               }
            }
            else
            {
               String filename = item.getName();
               if (filename != null && filename.length() != 0)
               {
                  if (logger.isDebugEnabled())
                  {
                     logger.debug("Processing uploaded file: " + filename);
                  }
                  // workaround a bug in IE where the full path is returned
                  // IE is only available for Windows so only check for the Windows path separator
                  filename = FilenameUtils.getName(filename);
                  final File tempFile = TempFileProvider.createTempFile("alfresco", ".upload");
                  item.write(tempFile);
                  bean.setFile(tempFile);
                  bean.setFileName(filename);
                  bean.setFilePath(tempFile.getAbsolutePath());
                  if (logger.isDebugEnabled())
                  {
                     logger.debug("Temp file: " + tempFile.getAbsolutePath() + 
				  " created from upload filename: " + filename);
                  }
               }
            }
         }

         session.setAttribute(FileUploadBean.getKey(uploadId), bean);         

         if (bean.getFile() == null)
         {
            logger.warn("no file uploaded for upload " + uploadId);
         }
         if (returnPage == null || returnPage.length() == 0)
         {
            throw new AlfrescoRuntimeException("return-page parameter has not been supplied");
         }
         
         if (returnPage.startsWith("javascript:"))
         {
            returnPage = returnPage.substring("javascript:".length());
            // finally redirect
            if (logger.isDebugEnabled())
            {
               logger.debug("Sending back javascript response " + returnPage);
            }

            final PrintWriter out = response.getWriter();
            out.println("<html><body><script type=\"text/javascript\">");
            out.println(returnPage);
            out.println("</script></body></html>");
            out.close();
         }
         else
         {
            // finally redirect
            if (logger.isDebugEnabled())
            {
               logger.debug("Upload servicing complete, redirecting to: " + returnPage);
            }
            
            response.sendRedirect(returnPage);
         }
      }
      catch (Throwable error)
      {
         Application.handleServletError(getServletContext(), (HttpServletRequest)request,
               (HttpServletResponse)response, error, logger, returnPage);
      }
   }
}
