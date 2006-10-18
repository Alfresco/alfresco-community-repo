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
import org.alfresco.web.templating.extension.*;

public class PressReleaseBean
{
    public static List<PressReleaseBean> getPressReleases(final PageContext pageContext)
	throws Exception
    {
       final ExtensionFunctions ef = 
          new ServletContextExtensionFunctionsAdapter(pageContext.getServletContext());

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
          href = href.replaceAll(".xml$", ".shtml");
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

    public String getTitle() 
    { 
	return this.title; 
    }

    public String getAbstract()
    {
	return this.theAbstract;
    }

    public Date getLaunchDate()
    {
	return this.launchDate;
    }

    public String getHref()
    {
	return this.href;
    }
}