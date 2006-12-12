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
package org.alfresco.web.forms;

import java.io.*;
import java.util.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

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

      MIME_TYPES.put(MimeConstants.MIME_GIF, MimeConstants.MIME_GIF);
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

   public void render(final Document xmlContent,
                      final RenderingEngineTemplate ret,
                      final Map<String, String> parameters,
                      final OutputStream out)
      throws IOException,
      RenderingEngine.RenderingException
   {
      Result result = null;
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
         final Fop fop = fopFactory.newFop(mimetype, 
                                           foUserAgent, 
                                           out);
         // Resulting SAX events (the generated FO) must be piped through to FOP
         result = new SAXResult(fop.getDefaultHandler());
      }
      catch (FOPException fope)
      {
         throw new RenderingEngine.RenderingException(fope);
      }
         
      super.render(new DOMSource(xmlContent), ret, parameters, result);
   }
}