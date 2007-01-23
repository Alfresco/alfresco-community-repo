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
package org.alfresco.web.api;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * API Service Response
 * 
 * @author davidc
 */
public class APIResponse extends HttpServletResponseWrapper
{
    // API Formats
    public static final String HTML_FORMAT = "html";
    public static final String ATOM_FORMAT = "atom";
    public static final String RSS_FORMAT = "rss";
    public static final String XML_FORMAT = "xml";
    public static final String OPENSEARCH_DESCRIPTION_FORMAT = "opensearchdescription";
    
    
    /**
     * Construct
     * 
     * @param res
     */
    /*package*/ APIResponse(HttpServletResponse res)
    {
        super(res);
    }

}
