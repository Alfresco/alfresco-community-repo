/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
