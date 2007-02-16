/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.search;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.util.SearchLanguageConversion;

/**
 * Provides basic searcher support
 * 
 * @author Andy Hind
 */
public abstract class AbstractSearcherComponent implements SearchService
{
    /**
     * Not implemented, but will eventually map directly to
     * {@link SearchLanguageConversion}.
     */
    protected String translateQuery(String fromLanguage, String toLangage, String query)
    {
        throw new UnsupportedOperationException();
    }

    public ResultSet query(StoreRef store, String language, String query)
    {
        return query(store, language, query, null, null);
    }

    public ResultSet query(StoreRef store, String language, String query,
            QueryParameterDefinition[] queryParameterDefintions)
    {
        return query(store, language, query, null, queryParameterDefintions);
    }

    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths)
    {
        return query(store, language, query, attributePaths, null);
    }

    public List<NodeRef> selectNodes(NodeRef contextNodeRef, String xpath, QueryParameterDefinition[] parameters,
            NamespacePrefixResolver namespacePrefixResolver, boolean followAllParentLinks)
            throws InvalidNodeRefException, XPathException
    {
        return selectNodes(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks,
                SearchService.LANGUAGE_XPATH);
    }

    public List<Serializable> selectProperties(NodeRef contextNodeRef, String xpath,
            QueryParameterDefinition[] parameters, NamespacePrefixResolver namespacePrefixResolver,
            boolean followAllParentLinks) throws InvalidNodeRefException, XPathException
    {
        return selectProperties(contextNodeRef, xpath, parameters, namespacePrefixResolver, followAllParentLinks,
                SearchService.LANGUAGE_XPATH);
    }
}
