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

import java.io.File;
import java.io.IOException;
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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
   private static Log logger = LogFactory.getLog(UploadFileServlet.class); 
   
   /**
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String returnPage = null;
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      
      try
      {
         AuthenticationStatus status = servletAuthenticate(request, response);
         if (status == AuthenticationStatus.Failure)
         {
            return;
         }
         
         if (isMultipart == false)
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
         
         Iterator<FileItem> iter = fileItems.iterator();
         FileUploadBean bean = new FileUploadBean();
         while(iter.hasNext())
         {
            FileItem item = iter.next();
            if(item.isFormField())
            {
               if (item.getFieldName().equalsIgnoreCase("return-page"))
               {
                  returnPage = item.getString();
               }
            }
            else
            {
               String filename = item.getName();
               if (filename != null && filename.length() != 0)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Processing uploaded file: " + filename);
                  
                  // workaround a bug in IE where the full path is returned
                  // IE is only available for Windows so only check for the Windows path separator
                  int idx = filename.lastIndexOf('\\');
                  
                  if (idx == -1)
                  {
                     // if there is no windows path separator check for *nix
                     idx = filename.lastIndexOf('/');
                  }
                  
                  if (idx != -1)
                  {
                     filename = filename.substring(idx + File.separator.length());
                  }
                  
                  File tempFile = TempFileProvider.createTempFile("alfresco", ".upload");
                  item.write(tempFile);
                  bean.setFile(tempFile);
                  bean.setFileName(filename);
                  bean.setFilePath(tempFile.getAbsolutePath());
                  session.setAttribute(FileUploadBean.FILE_UPLOAD_BEAN_NAME, bean);
                  if (logger.isDebugEnabled())
                     logger.debug("Temp file: " + tempFile.getAbsolutePath() + " created from upload filename: " + filename);
               }
            }
         }
         
         if (returnPage == null || returnPage.length() == 0)
         {
            throw new AlfrescoRuntimeException("return-page parameter has not been supplied");
         }

         // finally redirect
         if (logger.isDebugEnabled())
            logger.debug("Upload servicing complete, redirecting to: " + returnPage);

         response.sendRedirect(returnPage);
      }
      catch (Throwable error)
      {
         Application.handleServletError(getServletContext(), (HttpServletRequest)request,
               (HttpServletResponse)response, error, logger, returnPage);
      }
   }
}
