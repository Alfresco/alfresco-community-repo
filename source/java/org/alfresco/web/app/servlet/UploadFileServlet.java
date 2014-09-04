/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.web.app.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.ErrorBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.config.ConfigService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet that takes a file uploaded via a browser and represents it as an
 * UploadFileBean in the session
 * 
 * @author gavinc
 * @deprecated 5.0 not exposed in web-client web.xml
 */
public class UploadFileServlet extends BaseServlet
{
   private static final long serialVersionUID = -5482538466491052875L;
   private static final Log logger = LogFactory.getLog(UploadFileServlet.class); 
   
   private ConfigService configService;
   
   
   /**
    * @see javax.servlet.GenericServlet#init()
    */
   @Override
   public void init(ServletConfig sc) throws ServletException
   {
      super.init(sc);
      
      WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(sc.getServletContext());
      this.configService = (ConfigService)ctx.getBean("webClientConfigService");
   }

   /**
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @SuppressWarnings("unchecked")
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
         
         FacesContext context = FacesContext.getCurrentInstance();
         Map<Object, Object> session = context.getExternalContext().getSessionMap();
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
                  
                  // ADB-41: Ignore non-existent files i.e. 0 byte streams.
                  if (allowZeroByteFiles() == true || item.getSize() > 0)
                  {
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
                                     " size " + tempFile.length() +  
                                     " bytes created from upload filename: " + filename);
                     }
                  }
                  else
                  {
                     if (logger.isWarnEnabled())
                        logger.warn("Ignored file '" + filename + "' as there was no content, this is either " +
                              "caused by uploading an empty file or a file path that does not exist on the client.");
                  }
               }
            }
         }

         session.put(FileUploadBean.getKey(uploadId), bean);
         
         if (bean.getFile() == null && uploadId != null && logger.isWarnEnabled())
         {
            logger.warn("no file uploaded for upload id: " + uploadId);
         }
         
         if (returnPage == null || returnPage.length() == 0)
         {
            throw new AlfrescoRuntimeException("return-page parameter has not been supplied");
         }
         
         JSONObject json;
         try
         {
             json = new JSONObject(returnPage);
             
             if (json.has("id") && json.has("args"))
             {
                // finally redirect
                if (logger.isDebugEnabled())
                {
                    logger.debug("Sending back javascript response " + returnPage);
                }
                response.setContentType(MimetypeMap.MIMETYPE_HTML);
                response.setCharacterEncoding("utf-8");
                // work-around for WebKit protection against embedded javascript on POST body response
                response.setHeader("X-XSS-Protection", "0");
                final PrintWriter out = response.getWriter();
                out.println("<html><body><script type=\"text/javascript\">");
                
                out.println("window.parent.upload_complete_helper(");
                out.println("'" + json.getString("id") + "'");
                out.println(", ");
                out.println(json.getJSONObject("args"));
                out.println(");");
                
                out.println("</script></body></html>");
                out.close();
             }
         }
         catch (JSONException e)
         {
            // finally redirect
            if (logger.isDebugEnabled())
               logger.debug("redirecting to: " + returnPage);
            
            response.sendRedirect(returnPage);
         }
         
         if (logger.isDebugEnabled())
            logger.debug("upload complete");
      }
      catch (Throwable error)
      {
         handleUploadException(request, response, error, returnPage);
      }
   }

   private void handleUploadException(HttpServletRequest request, HttpServletResponse response, Throwable error, String returnPage)
   {
      try
      {
         HttpSession session = request.getSession(true);
         ErrorBean errorBean = (ErrorBean) session.getAttribute(ErrorBean.ERROR_BEAN_NAME);
         if (errorBean == null)
         {
            errorBean = new ErrorBean();
            session.setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean);
         }
         errorBean.setLastError(error);
         errorBean.setReturnPage(returnPage);
      }
      catch (Throwable e)
      {
         logger.error("Error while handling upload Exception", e);
      }
      try
      {
         String errorPage = Application.getErrorPage(getServletContext());
            
         if (logger.isDebugEnabled())
         {
                logger.debug("An error has occurred. Sending back response for redirecting to error page: " + errorPage);
         }
          
         response.setContentType(MimetypeMap.MIMETYPE_HTML);
         response.setCharacterEncoding("utf-8");
         final PrintWriter out = response.getWriter();
         out.println("<html><body><script type=\"text/javascript\">");
         out.println("window.parent.location.replace(\" " + request.getContextPath() + errorPage + "\")");
         out.println("</script></body></html> ");
         out.close();
      }
      catch (Exception e)
      {
          logger.error("Error while handling upload Exception", e);
      }
  }

   
   private boolean allowZeroByteFiles()
   {
      ClientConfigElement clientConfig = (ClientConfigElement)configService.getGlobalConfig().getConfigElement(
            ClientConfigElement.CONFIG_ELEMENT_ID);
      return clientConfig.isZeroByteFileUploads();
   }
}
