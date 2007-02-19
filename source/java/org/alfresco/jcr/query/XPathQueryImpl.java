/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
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
