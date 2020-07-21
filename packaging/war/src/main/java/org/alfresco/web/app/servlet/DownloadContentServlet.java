/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.web.app.servlet;

import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Servlet responsible for streaming node content from the repo directly to the response stream.
 * The appropriate mimetype is calculated based on filename extension.
 * <p>
 * The URL to the servlet should be generated thus:
 * <pre>/alfresco/download/attach/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf</pre>
 * or
 * <pre>/alfresco/download/direct/workspace/SpacesStore/0000-0000-0000-0000/myfile.pdf</pre></p>
 * <p>
 * The 'attach' or 'direct' element is used to indicate whether to display the stream directly
 * in the browser or download it as a file attachment.</p>
 * <p>
 * Since ACS 6.X, this Servlet redirects to GET /nodes/{nodeId}/content V1 REST API.
 *
 * 
 * @author Kevin Roast
 * @author gavinc
 */
public class DownloadContentServlet extends HttpServlet
{
   private static final long serialVersionUID = -576405943603122206L;

   private static Log logger = LogFactory.getLog(DownloadContentServlet.class);

   private static final String URL_ATTACH = "a";
   private static final String URL_ATTACH_LONG = "attach";
   private static final String URL_DIRECT = "d";
   private static final String URL_DIRECT_LONG = "direct";

   /**
    * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
    */
   protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException
   {
      if (logger.isDebugEnabled())
      {
         String queryString = req.getQueryString();
         logger.debug("Authenticating (GET) request to URL: " + req.getRequestURI() +
               ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
      }

      // remove request context.
      String requestURI = req.getRequestURI();
      requestURI = requestURI.substring(req.getContextPath().length());

      StringTokenizer t = new StringTokenizer(requestURI, "/");
      int tokenCount = t.countTokens();
      t.nextToken();     // skip servlet name

      // expect a minimum of 6 URL tokens.
      // /d/{attach|direct}/{storeType}/{storeId}/{nodeId}/{nodeName}
      if(tokenCount < 6)
      {
         throw new IllegalArgumentException("Download URL did not contain all required args: " + requestURI);
      }

      // attachment mode (either 'attach' or 'direct')
      String attachToken = t.nextToken();
      boolean isAttachment = URL_ATTACH.equalsIgnoreCase(attachToken) || URL_ATTACH_LONG.equalsIgnoreCase(attachToken);
      boolean isDirect = URL_DIRECT.equalsIgnoreCase(attachToken) || URL_DIRECT_LONG.equalsIgnoreCase(attachToken);
      if (!(isAttachment || isDirect))
      {
         throw new IllegalArgumentException("Attachment mode is not properly specified: " + requestURI);
      }

      // allow only nodes from workspace://SpaceStore/ storeRef
      StoreRef storeRef = new StoreRef(URLDecoder.decode(t.nextToken()), URLDecoder.decode(t.nextToken()));
      boolean isWorkspaceStoreType = storeRef.getProtocol().equalsIgnoreCase("workspace");
      boolean isSpacesStoreStoreId = storeRef.getIdentifier().equalsIgnoreCase("SpacesStore");

      if (!isWorkspaceStoreType || !isSpacesStoreStoreId)
      {
         throw new IllegalArgumentException("Servlet accepts only nodes from workspace://SpaceStore/ storeRef: " + requestURI);
      }

      String nodeId = URLDecoder.decode(t.nextToken());

      // build redirect URL to V1 GET /nodes/{nodeId}/content
      String redirectUrl = String
          .format("%s/api/-default-/public/alfresco/versions/1/nodes/%s/content?attachment=%b",
              req.getContextPath(), nodeId, isAttachment);

      if (logger.isDebugEnabled())
      {
         logger.debug("Request redirected to URL: " + redirectUrl);
      }
      res.sendRedirect(redirectUrl);
   }
}
