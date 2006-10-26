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
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;

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

   public XSLFORenderingEngine(final NodeRef nodeRef,
                               final NodeService nodeService,
                               final ContentService contentService)
   {
      super(nodeRef, nodeService, contentService);
   }

   public void generate(final Document xmlContent,
                        final Map<String, String> parameters,
                        final OutputStream out)
      throws IOException,
      RenderingEngine.RenderingException
   {
      try
      {
         final FopFactory fopFactory = FopFactory.newInstance();
         final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
         final Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
         
         // Resulting SAX events (the generated FO) must be piped through to FOP
         final Result result = new SAXResult(fop.getDefaultHandler());
         
         super.render(new DOMSource(xmlContent), parameters, result);
         
         // Result processing
         FormattingResults foResults = fop.getResults();
         java.util.List pageSequences = foResults.getPageSequences();
         for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) 
         {
            PageSequenceResults pageSequenceResults = (PageSequenceResults)it.next();
            System.out.println("PageSequence " 
                               + (String.valueOf(pageSequenceResults.getID()).length() > 0 
                                  ? pageSequenceResults.getID() : "<no id>") 
                               + " generated " + pageSequenceResults.getPageCount() + " pages.");
         }
         System.out.println("Generated " + foResults.getPageCount() + " pages in total.");
      }
      catch (FOPException fope)
      {
      }
   }
}