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
package org.alfresco.jcr.query;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;


/**
 * Alfresco implementation of XPath Query
 * 
 * @author David Caruana
 */
public class XPathQueryImpl extends QueryImpl
{

    /**
     * Construct
     * 
     * @param statement  the xpath statement
     */
    public XPathQueryImpl(SessionImpl session, String statement)
    {
        super(session, statement);
    }

    @Override
    public void isValidStatement() throws InvalidQueryException
    {
        // TODO
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.Query#execute()
     */
    public QueryResult execute() throws RepositoryException
    {
        SearchService search = getSession().getRepositoryImpl().getServiceRegistry().getSearchService();
        NodeService nodes = getSession().getRepositoryImpl().getServiceRegistry().getNodeService();
        NodeRef root = nodes.getRootNode(getSession().getWorkspaceStore());
        List<NodeRef> nodeRefs = search.selectNodes(root, getStatement(), null, getSession().getNamespaceResolver(), false, SearchService.LANGUAGE_JCR_XPATH);
        return new NodeRefListQueryResultImpl(getSession(), nodeRefs).getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.Query#getLanguage()
     */
    public String getLanguage()
    {
        return Query.XPATH;
    }

}
