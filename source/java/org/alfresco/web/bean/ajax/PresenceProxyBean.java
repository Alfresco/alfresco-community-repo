package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.io.Serializable;
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
public class PresenceProxyBean implements Serializable
{
   private static final long serialVersionUID = -3041576848188629589L;
   
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
