/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.rest.api.impl;

import static org.alfresco.rest.api.impl.QueriesImpl.AbstractQuery.Sort.IN_QUERY_SORT;
import static org.alfresco.rest.api.impl.QueriesImpl.AbstractQuery.Sort.POST_QUERY_SORT;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.ISO9075;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.SearchLanguageConversion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Queries implementation
 * 
 * @author janv
 * @author Alan Davis
 */
public class QueriesImpl implements Queries, InitializingBean
{
    private final static Map<String,QName> NODE_SORT_PARAMS_TO_QNAMES = sortParamsToQNames(
        PARAM_NAME,       ContentModel.PROP_NAME,
        PARAM_CREATEDAT,  ContentModel.PROP_CREATED,
        PARAM_MODIFIEDAT, ContentModel.PROP_MODIFIED);

    private final static Map<String, QName> PEOPLE_SORT_PARAMS_TO_QNAMES = sortParamsToQNames(
        ContentModel.PROP_USERNAME,
        ContentModel.PROP_FIRSTNAME,
        ContentModel.PROP_LASTNAME);

    private final static Map<String, QName> SITE_SORT_PARAMS_TO_QNAMES = sortParamsToQNames(
            PARAM_SITE_ID,  ContentModel.PROP_NAME,
            PARAM_SITE_TITLE,  ContentModel.PROP_TITLE,
            PARAM_SITE_DESCRIPTION, ContentModel.PROP_DESCRIPTION);

    /**
     * Helper method to build a map of sort parameter names to QNames. This method iterates through
     * the parameters. If a parameter is a String it is assumed to be a sort parameter name and will
     * be followed by a QName to which it maps. If however it is a QName the local name of the OName
     * is used as the sort parameter name.
     * @param parameters to build up the map.
     * @return the map
     */
    private static Map<String, QName> sortParamsToQNames(Object... parameters)
    {
        Map<String, QName> map = new HashMap<>();
        for (int i=0; i<parameters.length; i++)
        {
            map.put(
                parameters[i] instanceof String
                ? (String)parameters[i++]
                : ((QName)parameters[i]).getLocalName(),
                (QName)parameters[i]);
        }
        return Collections.unmodifiableMap(map);
    }
    
    private ServiceRegistry sr;
    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private SiteService siteService;

    private Nodes nodes;
    private People people;
    private Sites sites;

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setPeople(People people)
    {
        this.people = people;
    }

    public void setSites(Sites sites)
    {
        this.sites = sites;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("sr", this.sr);
        ParameterCheck.mandatory("nodes", this.nodes);
        ParameterCheck.mandatory("people", this.people);
        ParameterCheck.mandatory("sites", this.sites);
        
        this.searchService = sr.getSearchService();
        this.nodeService = sr.getNodeService();
        this.namespaceService = sr.getNamespaceService();
        this.dictionaryService = sr.getDictionaryService();
        this.siteService = sr.getSiteService();
    }

    @Override
    public CollectionWithPagingInfo<Node> findNodes(Parameters parameters)
    {
        return new AbstractQuery<Node>(nodeService, searchService)
        {
            private final Map<String, UserInfo> mapUserInfo = new HashMap<>(10);
            
            @Override
            protected void buildQuery(StringBuilder query, String term, SearchParameters sp, String queryTemplateName)
            {
                sp.addQueryTemplate(queryTemplateName, "%(cm:name cm:title cm:description TEXT TAG)");

                String rootNodeId = parameters.getParameter(PARAM_ROOT_NODE_ID);
                if (rootNodeId != null)
                {
                    NodeRef nodeRef = nodes.validateOrLookupNode(rootNodeId, null);
                    query.append("PATH:\"").append(getQNamePath(nodeRef.getId())).append("//*\" AND (");
                }
                query.append(term);
                if (rootNodeId != null)
                {
                    query.append(")");
                }
                
                String nodeTypeStr = parameters.getParameter(PARAM_NODE_TYPE);
                if (nodeTypeStr != null)
                {
                    QName filterNodeTypeQName = nodes.createQName(nodeTypeStr);
                    if (dictionaryService.getType(filterNodeTypeQName) == null)
                    {
                        throw new InvalidArgumentException("Unknown filter nodeType: "+nodeTypeStr);
                    }

                    query.append(" AND (+TYPE:\"").append(nodeTypeStr).append(("\")"));
                    query.append(" AND -ASPECT:\"sys:hidden\" AND -cm:creator:system AND -QNAME:comment\\-* ");
                }
                else
                {
                    query.append(" AND (+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\")");
                    query.append(" AND -TYPE:\"cm:thumbnail\" AND -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"fm:post\"");
                    query.append(" AND -TYPE:\"st:site\" AND -ASPECT:\"st:siteContainer\"");
                    query.append(" AND -ASPECT:\"sys:hidden\" AND -cm:creator:system AND -QNAME:comment\\-* ");
                }
            }

            private String getQNamePath(String nodeId)
            {
                NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

                Map<String, String> cache = new HashMap<>();
                StringBuilder buf = new StringBuilder(128);
                Path path = null;
                try
                {
                   path = nodeService.getPath(nodeRef);
                }
                catch (InvalidNodeRefException inre)
                {
                    throw new EntityNotFoundException(nodeId);
                }

                for (Path.Element e : path)
                {
                    if (e instanceof Path.ChildAssocElement)
                    {
                        QName qname = ((Path.ChildAssocElement) e).getRef().getQName();
                        if (qname != null)
                        {
                            String prefix = cache.get(qname.getNamespaceURI());
                            if (prefix == null)
                            {
                                // first request for this namespace prefix, get and cache result
                                Collection<String> prefixes = namespaceService.getPrefixes(qname.getNamespaceURI());
                                prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
                                cache.put(qname.getNamespaceURI(), prefix);
                            }
                            buf.append('/').append(prefix).append(':').append(ISO9075.encode(qname.getLocalName()));
                        }
                    }
                    else
                    {
                        buf.append('/').append(e.toString());
                    }
                }
                return buf.toString();
            }

            @Override
            protected List<Node> newList(int capacity)
            {
                return new ArrayList<Node>(capacity);
            }

            @Override
            protected Node convert(NodeRef nodeRef, List<String> includeParam)
            {
                return nodes.getFolderOrDocument(nodeRef, null, null, includeParam, mapUserInfo);
            }
        }.find(parameters, PARAM_TERM, MIN_TERM_LENGTH_NODES, "keywords",
            IN_QUERY_SORT, NODE_SORT_PARAMS_TO_QNAMES,
            new SortColumn(PARAM_MODIFIEDAT, false));
    }
    
    @Override
    public CollectionWithPagingInfo<Person> findPeople(Parameters parameters)
    {
        return new AbstractQuery<Person>(nodeService, searchService)
        {
            @Override
            protected void buildQuery(StringBuilder query, String term, SearchParameters sp, String queryTemplateName)
            {
                sp.addQueryTemplate(queryTemplateName, "|%firstName OR |%lastName OR |%userName");
                sp.setExcludeTenantFilter(false);
                sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);

                query.append("TYPE:\"").append(ContentModel.TYPE_PERSON).append("\" AND (\"*");
                query.append(term);
                query.append("*\")");
            }

            @Override
            protected List<Person> newList(int capacity)
            {
                return new ArrayList<Person>(capacity);
            }

            @Override
            protected Person convert(NodeRef nodeRef, List<String> includeParam)
            {
                String personId = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
                Person person = people.getPerson(personId);
                return person;
            }
            
            // TODO Do the sort in the query on day. A comment in the code for the V0 API used for live people
            //      search says adding sort values for this query don't work - tried it and they really don't.
        }.find(parameters, PARAM_TERM, MIN_TERM_LENGTH_PEOPLE, "_PERSON",
            POST_QUERY_SORT, PEOPLE_SORT_PARAMS_TO_QNAMES,
            new SortColumn(PARAM_FIRSTNAME, true), new SortColumn(PARAM_LASTNAME, true));
    }

    @Override
    public CollectionWithPagingInfo<Site> findSites(Parameters parameters)
    {
        // TODO review
        AbstractQuery.Sort sortType = IN_QUERY_SORT;
        String sortTypeStr = parameters.getParameter(PARAM_SORT_TYPE);
        if (sortTypeStr != null) {
            if (sortTypeStr.equalsIgnoreCase("in-query"))
            {
                sortType = IN_QUERY_SORT;
            }
            else if (sortTypeStr.equalsIgnoreCase("post-query"))
            {
                sortType = POST_QUERY_SORT;
            }
            else
            {
               throw new IllegalArgumentException("Unexpected sortType: "+sortTypeStr+" (expected in-query or post-query)");
            }
        }
        
        return new AbstractQuery<Site>(nodeService, searchService)
        {
            @Override
            protected void buildQuery(StringBuilder query, String term, SearchParameters sp, String queryTemplateName)
            {
                sp.addQueryTemplate(queryTemplateName, "%(cm:name cm:title cm:description)");
                sp.setExcludeTenantFilter(false);
                sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);

                query.append("TYPE:\"").append(SiteModel.TYPE_SITE).append("\" AND (\"*");
                query.append(term);
                query.append("*\")");
            }

            @Override
            protected String getTerm(Parameters parameters, String termName, int minTermLength)
            {
                String filter = super.getTerm(parameters, termName, minTermLength);
                String escNameFilter = SearchLanguageConversion.escapeLuceneQuery(filter.replace('"', ' '));
                return escNameFilter;
            }

            @Override
            protected List<Site> newList(int capacity)
            {
                return new ArrayList<>(capacity);
            }

            @Override
            protected Site convert(NodeRef nodeRef, List<String> includeParam)
            {
                return getSite(siteService.getSite(nodeRef), true);
            }

            // note: see also Sites.getSite
            private Site getSite(SiteInfo siteInfo, boolean includeRole)
            {
                // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
                String siteId = siteInfo.getShortName();
                String role = null;
                if(includeRole)
                {
                    role = sites.getSiteRole(siteId);
                }
                return new Site(siteInfo, role);
            }
        }.find(parameters, PARAM_TERM, MIN_TERM_LENGTH_SITES, "_SITE", sortType, SITE_SORT_PARAMS_TO_QNAMES, new SortColumn(PARAM_SITE_TITLE, true));
    }
    
    public abstract static class AbstractQuery<T>
    {
        public enum Sort
        {
            IN_QUERY_SORT, POST_QUERY_SORT
        }
        
        private final NodeService nodeService;
        private final SearchService searchService;
        
        public AbstractQuery(NodeService nodeService, SearchService searchService)
        {
            this.nodeService = nodeService;
            this.searchService = searchService;
        }

        public CollectionWithPagingInfo<T> find(Parameters parameters,
            String termName, int minTermLength, String queryTemplateName,
            Sort sort, Map<String, QName> sortParamsToQNames, SortColumn... defaultSort)
        {
            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            sp.setDefaultFieldName(queryTemplateName);
            
            String term = getTerm(parameters, termName, minTermLength);

            StringBuilder query = new StringBuilder();
            buildQuery(query, term, sp, queryTemplateName);
            sp.setQuery(query.toString());

            Paging paging = parameters.getPaging();
            PagingRequest pagingRequest = Util.getPagingRequest(paging);
            
            List<SortColumn> defaultSortCols = (defaultSort != null ? Arrays.asList(defaultSort) : Collections.emptyList());
            if (sort == IN_QUERY_SORT)
            {
                addSortOrder(parameters, sortParamsToQNames, defaultSortCols, sp);

                sp.setSkipCount(pagingRequest.getSkipCount());
                sp.setMaxItems(pagingRequest.getMaxItems());
            }
            
            ResultSet queryResults = null;
            List<T> collection = null;
            try
            {
                queryResults = searchService.query(sp);
                
                List<NodeRef> nodeRefs = queryResults.getNodeRefs();
                
                if (sort == POST_QUERY_SORT)
                {
                    nodeRefs = postQuerySort(parameters, sortParamsToQNames, defaultSortCols, nodeRefs);
                }
                
                collection = newList(nodeRefs.size());
                List<String> includeParam = parameters.getInclude();

                for (NodeRef nodeRef : nodeRefs)
                {
                    T t = convert(nodeRef, includeParam);
                    collection.add(t);
                }
            }
            finally
            {
                if (queryResults != null)
                {
                    queryResults.close();
                }
            }

            if (sort == POST_QUERY_SORT)
            {
                return listPage(collection, paging);
            }
            else
            {
                return CollectionWithPagingInfo.asPaged(paging, collection, queryResults.hasMore(), new Long(queryResults.getNumberFound()).intValue());
            }
        }

        /**
         * Builds up the query and is expected to call {@link SearchParameters#setDefaultFieldName(String)}
         * and {@link SearchParameters#addQueryTemplate(String, String)}
         * @param query StringBuilder into which the query should be built.
         * @param term to be searched for
         * @param sp SearchParameters
         * @param queryTemplateName
         */
        protected abstract void buildQuery(StringBuilder query, String term, SearchParameters sp, String queryTemplateName);
        
        /**
         * Returns a list of the correct type.
         * @param capacity of the list
         * @return a new list.
         */
        protected abstract List<T> newList(int capacity);

        /**
         * Converts a nodeRef into the an object of the required type.
         * @param nodeRef to be converted
         * @param includeParam additional fields to be included
         * @return the object
         */
        protected abstract T convert(NodeRef nodeRef, List<String> includeParam);
        
        protected String getTerm(Parameters parameters, String termName, int minTermLength)
        {
            String term = parameters.getParameter(termName);
            if (term == null)
            {
                throw new InvalidArgumentException("Query '"+termName+"' not specified");
            }

            term = term.trim();
            term = term.replace("\"", "");
            int cnt = 0;
            for (int i = 0; i < term.length(); i++)
            {
                char c = term.charAt(i);
                if (Character.isLetterOrDigit(c))
                {
                    cnt++;
                    if (cnt == minTermLength)
                    {
                        break;
                    }
                }
            }

            if (cnt < minTermLength)
            {
                throw new InvalidArgumentException("Query '"+termName+"' is too short. Must have at least "+minTermLength+" alphanumeric chars");
            }

            return term;
        }

        /**
         * Adds sort order to the SearchParameters.
         */
        protected void addSortOrder(Parameters parameters, Map<String, QName> sortParamsToQNames,
            List<SortColumn> defaultSortCols, SearchParameters sp)
        {
            List<SortColumn> sortCols = getSorting(parameters, defaultSortCols);
            for (SortColumn sortCol : sortCols)
            {
                QName sortPropQName = sortParamsToQNames.get(sortCol.column);
                if (sortPropQName == null)
                {
                    throw new InvalidArgumentException("Invalid sort field: "+sortCol.column);
                }
                sp.addSort("@" + sortPropQName,  sortCol.asc);
            }
        }

        private List<SortColumn> getSorting(Parameters parameters, List<SortColumn> defaultSortCols)
        {
            List<SortColumn> sortCols = parameters.getSorting();
            if (sortCols == null || sortCols.size() == 0)
            {
                sortCols = defaultSortCols == null ? Collections.emptyList() : defaultSortCols;
            }
            return sortCols;
        }

        protected List<NodeRef> postQuerySort(Parameters parameters, Map<String, QName> sortParamsToQNames,
            List<SortColumn> defaultSortCols, List<NodeRef> nodeRefs)
        {
            final List<SortColumn> sortCols = getSorting(parameters, defaultSortCols);
            int sortColCount = sortCols.size();
            if (sortColCount > 0)
            {
                // make copy of nodeRefs because it can be unmodifiable list.
                nodeRefs = new ArrayList<NodeRef>(nodeRefs);
                
                List<QName> sortPropQNames = new ArrayList<>(sortColCount);
                for (SortColumn sortCol : sortCols)
                {
                    QName sortPropQName = sortParamsToQNames.get(sortCol.column);
                    if (sortPropQName == null)
                    {
                        throw new InvalidArgumentException("Invalid sort field: "+sortCol.column);
                    }
                    sortPropQNames.add(sortPropQName);
                }
                
                final Collator col = AlfrescoCollator.getInstance(I18NUtil.getLocale());
                Collections.sort(nodeRefs, new Comparator<NodeRef>()
                {
                    @Override
                    public int compare(NodeRef n1, NodeRef n2)
                    {
                        int result = 0;
                        for (int i=0; i<sortCols.size(); i++)
                        {
                            SortColumn sortCol = sortCols.get(i);
                            QName sortPropQName = sortPropQNames.get(i);
                            
                            Serializable  p1 = getProperty(n1, sortPropQName);
                            Serializable  p2 = getProperty(n2, sortPropQName);

                            result = ((p1 instanceof Long) && (p2 instanceof Long)
                                ? Long.compare((Long)p1, (Long)p2)
                                : col.compare(p1.toString(), p2.toString()))
                                * (sortCol.asc ? 1 : -1);
                            
                            if (result != 0)
                            {
                                break;
                            }
                        }
                        return result;
                    }

                    private Serializable getProperty(NodeRef nodeRef, QName sortPropQName)
                    {
                        Serializable result = nodeService.getProperty(nodeRef, sortPropQName);
                        return result == null ? "" : result;
                    }

                });
            }
            return nodeRefs;
        }

        // note: see also AbstractNodeRelation
        protected static <T> CollectionWithPagingInfo<T> listPage(List<T> result, Paging paging)
        {
            // return 'page' of results (based on full result set)
            int skipCount = paging.getSkipCount();
            int pageSize = paging.getMaxItems();
            int pageEnd = skipCount + pageSize;

            final List<T> page = new ArrayList<>(pageSize);
            if (result == null)
            {
                result = Collections.emptyList();
            }

            Iterator<T> it = result.iterator();
            for (int counter = 0; counter < pageEnd && it.hasNext(); counter++)
            {
                T element = it.next();
                if (counter < skipCount)
                {
                    continue;
                }
                if (counter > pageEnd - 1)
                {
                    break;
                }
                page.add(element);
            }

            int totalCount = result.size();
            boolean hasMoreItems = ((skipCount + page.size()) < totalCount);

            return CollectionWithPagingInfo.asPaged(paging, page, hasMoreItems, totalCount);
        }
    }
}
