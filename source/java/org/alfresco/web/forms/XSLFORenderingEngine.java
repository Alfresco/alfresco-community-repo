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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms;

import java.io.*;
import java.util.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A rendering engine which uses xsl-fo templates to generate renditions of
 * form instance data.
 *
 * @author Ariel Backenroth
 */
public class XSLFORenderingEngine
   extends XSLTRenderingEngine
{

   private static final Log LOGGER = LogFactory.getLog(XSLFORenderingEngine.class);

   private static final Map<String, String> MIME_TYPES = 
      new HashMap<String, String>();
   static
   {
      MIME_TYPES.put(MimeConstants.MIME_PDF, MimeConstants.MIME_PDF);

      MIME_TYPES.put(MimeConstants.MIME_POSTSCRIPT, MimeConstants.MIME_POSTSCRIPT);
      MIME_TYPES.put(MimeConstants.MIME_EPS, MimeConstants.MIME_POSTSCRIPT);

      MIME_TYPES.put(MimeConstants.MIME_PLAIN_TEXT, MimeConstants.MIME_PLAIN_TEXT);

      MIME_TYPES.put(MimeConstants.MIME_RTF, MimeConstants.MIME_RTF);
      MIME_TYPES.put(MimeConstants.MIME_RTF_ALT1, MimeConstants.MIME_RTF);
      MIME_TYPES.put(MimeConstants.MIME_RTF_ALT2, MimeConstants.MIME_RTF);

      MIME_TYPES.put(MimeConstants.MIME_MIF, MimeConstants.MIME_MIF);
      MIME_TYPES.put("application/x-mif", MimeConstants.MIME_MIF);

      MIME_TYPES.put(MimeConstants.MIME_SVG, MimeConstants.MIME_SVG);
      MIME_TYPES.put("image/svg", MimeConstants.MIME_SVG);

      //looks like a dependency is missing - removing for now
      //MIME_TYPES.put(MimeConstants.MIME_GIF, MimeConstants.MIME_GIF);
      MIME_TYPES.put(MimeConstants.MIME_PNG, MimeConstants.MIME_PNG);
      MIME_TYPES.put(MimeConstants.MIME_JPEG, MimeConstants.MIME_JPEG);
      MIME_TYPES.put(MimeConstants.MIME_TIFF, MimeConstants.MIME_TIFF);
   };

   public XSLFORenderingEngine()
   {
      super();
   }

   public String getName()
   {
      return "XSL-FO";
   }

   public String getDefaultTemplateFileExtension() 
   {
      return "fo";
   }

   @Override
   public void render(final Map<QName, Object> model,
                      final RenderingEngineTemplate ret,
                      final OutputStream out)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {

      String mimetype = MIME_TYPES.get(ret.getMimetypeForRendition());
      if (mimetype == null)
      {
         throw new RenderingEngine.RenderingException("mimetype " + ret.getMimetypeForRendition() +
                                                      " is not supported by " + this.getName());
      }
      try
      {
         final FopFactory fopFactory = FopFactory.newInstance();
         final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
         final Fop fop = fopFactory.newFop(mimetype, foUserAgent, out);
         // Resulting SAX events (the generated FO) must be piped through to FOP
         super.render(model,
                      ret, 
                      new SAXResult(fop.getDefaultHandler()));

      }
      catch (FOPException fope)
      {
         throw new RenderingEngine.RenderingException(fope);
      }
      finally
      {
         out.close();
      }
   }
}