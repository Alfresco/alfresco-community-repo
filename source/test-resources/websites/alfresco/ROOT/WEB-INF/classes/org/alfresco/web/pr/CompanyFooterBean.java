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
import javax.servlet.jsp.PageContext;
import org.w3c.dom.*;

public class CompanyFooterBean
{

    public static List<CompanyFooterBean> getCompanyFooters(final PageContext pageContext)
	throws Exception
    {
	final Map<String, Document> entries = Util.loadXMLDocuments(pageContext,
								    "/media/releases/content/company_footers",
								    "alfresco:company-footer");
	final List<CompanyFooterBean> result = new ArrayList<CompanyFooterBean>(entries.size());
	for (Map.Entry<String, Document> entry : entries.entrySet())
	{
	    String fileName = entry.getKey();
	    Document d = entry.getValue();
	    Element n = (Element)d.getElementsByTagName("alfresco:name").item(0);
	    String href = "/media/releases/content/company_footers/" + fileName;
	    result.add(new CompanyFooterBean(n.getFirstChild().getNodeValue(),
					     href));
	}
	return result;
    }

    private final String name;
    private final String href;

    public CompanyFooterBean(final String name, 
			     final String href)
    {
        this.name = name;
        this.href = href;
    }

    public String getName() 
    { 
	return this.name; 
    }

    public String getHref()
    {
	return this.href;
    }
}