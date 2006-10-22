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
package org.alfresco.web.pr;

import java.util.*;
import java.text.*;
import javax.servlet.jsp.PageContext;
import org.w3c.dom.*;
import org.alfresco.web.forms.*;

/**
 * Bean for getting data for press releases.
 * It's used by /media/releases/index.jsp to aggregate all forms created by press-release.xsd in 
 * /media/releases/content and generate an index page for them.
 */
public class PressReleaseBean
{
   /**
    * Loads all xml assets created by press-release.xsd in /media/releases/content and populates
    * PressReleaseBeans with their contents.
    *
    * @param pageContext the page context from the jsp, needed for accessing the 
    * servlet context for the ServletContextFormDataFunctionsAdapter class.
    *
    * @return a list of populated PressReleaseBeans.
    */
    public static List<PressReleaseBean> getPressReleases(final PageContext pageContext)
	throws Exception
    {
       final FormDataFunctions ef = 
          new ServletContextFormDataFunctionsAdapter(pageContext.getServletContext());

       final Map<String, Document> entries = ef.getXMLDocuments("press-release", "/media/releases/content");
       final List<PressReleaseBean> result = new ArrayList<PressReleaseBean>(entries.size());
       for (Map.Entry<String, Document> entry : entries.entrySet() )
       {
          final String fileName = entry.getKey();
          final Document d = entry.getValue();
          final Element t = (Element)d.getElementsByTagName("alfresco:title").item(0);
          final Element a = (Element)d.getElementsByTagName("alfresco:abstract").item(0);
          final Element dateEl = (Element)d.getElementsByTagName("alfresco:launch_date").item(0);
          final Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateEl.getFirstChild().getNodeValue());
          String href = "/media/releases/content/" + fileName;
          href = href.replaceAll(".xml$", ".html");
          result.add(new PressReleaseBean(t.getFirstChild().getNodeValue(),
                                          a.getFirstChild().getNodeValue(),
                                          date,
                                          href));
       }
       return result;
    }

    private final String title;
    private final String theAbstract;
    private final Date launchDate;
    private final String href;

    public PressReleaseBean(final String title, 
			    final String theAbstract, 
			    final Date launchDate, 
			    final String href)
    {
        this.title = title;
        this.theAbstract = theAbstract;
        this.launchDate = launchDate;
        this.href = href;
    }

   /** 
    * The title of the press release as defined in the xml asset. 
    */
    public String getTitle() 
    { 
	return this.title; 
    }

   /** 
    * The abstract of the press release as defined in the xml asset. 
    */
    public String getAbstract()
    {
	return this.theAbstract;
    }

   /** 
    * The launch date of the press release as defined in the xml asset. 
    */
    public Date getLaunchDate()
    {
	return this.launchDate;
    }

  /**
    * Returns the url within the webapp to the xml file describing this press release
    *
    * @return the url to the xml file which will be something like /media/releases/content/[filename].xml
    */
   public String getHref()
   {
      return this.href;
   }
}