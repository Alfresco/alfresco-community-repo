/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean which proxies requests to online presence servers.
 * 
 * @author Mike Hatfield
 */
public class PresenceProxyBean
{
   private static Log logger = LogFactory.getLog(PresenceProxyBean.class);
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void proxyRequest() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String url = (String)requestMap.get("url");
      
      if (logger.isDebugEnabled())
         logger.debug("PresenceProxyBean.proxyRequest() url=" + url);
      
      if (url != null)
      {
         String response = getUrlResponse(url);
         out.write(response);
      }
   }

   /**
    * Perform request
    *  
    * @throws IOException
    */
   public String getUrlResponse(String requestUrl)
   {
      String response = "";
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(requestUrl);
      method.setRequestHeader("Accept", "*/*");
      client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
      try
      {
         int statusCode = client.executeMethod(method);
         if (statusCode == HttpStatus.SC_OK)
         {
            response = method.getResponseBodyAsString();
         }
         else
         {
            response = method.getStatusText();
         }
      }
      catch (HttpException e)
      {
         response = e.getMessage();
      }
      catch (IOException e)
      {
         response = e.getMessage();
      }
      finally
      {
         // Release the connection.
         method.releaseConnection();
      }
      
      return response;
   }
}
