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

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.PageContext;
import java.io.*;
import java.util.*;
import org.alfresco.jndi.*;
import org.alfresco.repo.avm.AVMRemote;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class Util
{

   public static Map<String, Document> loadXMLDocuments(final PageContext pageContext,
                                                        final String path,
                                                        final String documentElementNodeName)
	throws Exception
    {
	final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);
	dbf.setValidating(false);
	final DocumentBuilder db = dbf.newDocumentBuilder();

	// The real_path will look somethign like this:
	//   /alfresco.avm/avm.alfresco.localhost/$-1$alfreco-guest-main:/appBase/avm_webapps/my_webapp
	final String realPath = pageContext.getServletContext().getRealPath(path);

	// The avm_path to the root of the context will look something like this:
	//    alfreco-guest-main:/appBase/avm_webapps/my_webapp
	String avmPath = realPath.substring(realPath.indexOf('$', realPath.indexOf('$') + 1)  + 1);
	avmPath = avmPath.replace('\\','/');

	final AVMRemote avm_remote = AVMFileDirContext.getAVMRemote();
	final Map<String, AVMNodeDescriptor> entries = avm_remote.getDirectoryListing(-1, avmPath);

	Map<String, Document> result = new HashMap<String, Document>();
	for (Map.Entry<String, AVMNodeDescriptor> entry : entries.entrySet() )
	{
	    final String entryName = entry.getKey();
	    AVMNodeDescriptor entryNode = entry.getValue();
	    if (entryNode.isFile())
	    {
		final InputStream istream = 
		    new AVMRemoteInputStream(avm_remote.getInputHandle(-1, avmPath + '/' + entryName), 
					     avm_remote );
		try
		{
		    final Document d = db.parse(istream);
		    if (documentElementNodeName.equals(d.getDocumentElement().getNodeName()))
			result.put(entryName, d);
		}
		catch (Throwable t)
		{
		    t.printStackTrace();
		}
		finally
		{
		    istream.close();
		}
	    }
	}
	return result;
    }
}