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

import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper around standard faces servlet to provide error handling
 * 
 * @author gavinc
 */
public class AlfrescoFacesServlet extends FacesServlet
{
   private static Log logger = LogFactory.getLog(AlfrescoFacesServlet.class);
   
   /**
    * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
    */
   public void service(ServletRequest request, ServletResponse response) 
      throws IOException, ServletException
   {
      try
      {
         super.service(request, response);
      }
      catch (Throwable error)
      {
         String returnPage = ((HttpServletRequest)request).getRequestURI();
         
         Application.handleServletError(getServletConfig().getServletContext(), (HttpServletRequest)request,
               (HttpServletResponse)response, error, logger, returnPage);
      }
   }
}
