/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.saxpath.base.XPathReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabel;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandler;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;

/**
 * Search component for use by the ScriptService.
 * <p>
 * Provides access to Lucene search facilities including saved search objects. The results from a search are returned as an array (collection) of scriptable Node wrapper objects.
 * <p>
 * The object is added to the root of the model to provide syntax such as: <code>var results = search.luceneSearch(statement);</code> and <code>var results = search.savedSearch(node);</code>
 * 
 * @author Kevin Roast
 */
public class Search extends BaseScopableProcessorExtension implements InitializingBean
{
    private static Log logger = LogFactory.getLog(Search.class);

    /** Service registry */
    protected ServiceRegistry services;

    /** Default store reference */
    protected StoreRef storeRef;

    /** Repository helper */
    protected Repository repository;

    private SwitchableApplicationContextFactory searchSubsystem;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "services", services);
    }

    /**
     * Set the default store reference
     * 
     * @param storeRef
     *            the default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        // ensure this is not set again by a script instance!
        if (this.storeRef != null)
        {
            throw new IllegalStateException("Default store URL can only be set once.");
        }
        this.storeRef = new StoreRef(storeRef);
    }

    /**
     * Set the service registry
     * 
     * @param services
     *            the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }

    /**
     * Set the repository helper
     * 
     * @param repository
     *            the repository helper
     */
    public void setRepositoryHelper(Repository repository)
    {
        this.repository = repository;
    }

    public void setSearchSubsystemSwitchableApplicationContextFactory(SwitchableApplicationContextFactory searchSubsystem)
    {
        this.searchSubsystem = searchSubsystem;
    }

    // JavaScript API

    public String getSearchSubsystem()
    {
        return (searchSubsystem == null) ? "" : searchSubsystem.getCurrentSourceBeanName();
    }

    /**
     * Find a single Node by the Node reference
     * 
     * @param ref
     *            The NodeRef of the Node to find
     * 
     * @return the Node if found or null if failed to find
     */
    public ScriptNode findNode(NodeRef ref)
    {
        ParameterCheck.mandatory("ref", ref);
        if (this.services.getNodeService().exists(ref)
                && (this.services.getPermissionService().hasPermission(ref,
                        PermissionService.READ) == AccessStatus.ALLOWED))
        {
            return new ScriptNode(ref, this.services, getScope());
        }
        return null;
    }

    /**
     * Find a single Node by the Node reference
     * 
     * @param ref
     *            The fully qualified NodeRef in String format
     * 
     * @return the Node if found or null if failed to find
     */
    public ScriptNode findNode(String ref)
    {
        ParameterCheck.mandatoryString("ref", ref);
        return findNode(new NodeRef(ref));
    }

    /**
     * Helper to convert a Web Script Request URL to a Node Ref
     * 
     * 1) Node - {store_type}/{store_id}/{node_id}
     *
     * Resolve to node via its Node Reference.
     * 
     * 2) Path - {store_type}/{store_id}/{path}
     * 
     * Resolve to node via its display path.
     * 
     * @param referenceType
     *            one of "node", "path"
     * @param reference
     *            array of reference segments (as described above for each reference type)
     * @return ScriptNode the script node
     */
    public ScriptNode findNode(String referenceType, String[] reference)
    {
        ParameterCheck.mandatoryString("referenceType", referenceType);
        ParameterCheck.mandatory("reference", reference);
        ScriptNode result = null;
        NodeRef nodeRef = this.repository.findNodeRef(referenceType, reference);
        if (nodeRef != null)
        {
            result = new ScriptNode(nodeRef, this.services, getScope());
        }
        return result;
    }

    /**
     * Execute a XPath search
     * 
     * @param search
     *            XPath search string to execute
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable xpathSearch(String search)
    {
        return xpathSearch(null, search);
    }

    /**
     * Execute a XPath search
     * 
     * @param store
     *            Store reference to search against i.e. workspace://SpacesStore
     * @param search
     *            XPath search string to execute
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable xpathSearch(String store, String search)
    {
        if (search != null && search.length() != 0)
        {
            Object[] results = query(store, search, null, SearchService.LANGUAGE_XPATH);
            return Context.getCurrentContext().newArray(getScope(), results);
        }
        else
        {
            return Context.getCurrentContext().newArray(getScope(), 0);
        }
    }

    /**
     * Execute a SelectNodes XPath search
     * 
     * @param search
     *            SelectNodes XPath search string to execute
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable selectNodes(String search)
    {
        return selectNodes(null, search);
    }

    /**
     * Execute a SelectNodes XPath search
     * 
     * @param store
     *            Store reference to search against i.e. workspace://SpacesStore
     * @param search
     *            SelectNodes XPath search string to execute
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable selectNodes(String store, String search)
    {
        if (search != null && search.length() != 0)
        {
            Object[] nodeArray = new Object[0];
            if (store == null)
            {
                store = "workspace://SpacesStore";
            }
            try
            {
                NodeService nodeService = this.services.getNodeService();
                List<NodeRef> nodes = this.services.getSearchService().selectNodes(
                        nodeService.getRootNode(new StoreRef(store)), search, null, this.services.getNamespaceService(), false);
                if (nodes.size() != 0)
                {
                    int index = 0;
                    nodeArray = new Object[nodes.size()];
                    for (NodeRef node : nodes)
                    {
                        nodeArray[index++] = new ScriptNode(node, this.services, getScope());
                    }
                }
            }
            catch (Throwable err)
            {
                throw new AlfrescoRuntimeException("Failed to execute search: " + search, err);
            }

            return Context.getCurrentContext().newArray(getScope(), nodeArray);
        }
        else
        {
            return Context.getCurrentContext().newArray(getScope(), 0);
        }
    }

    /**
     * Validation Xpath query
     * 
     * @param query
     *            xpath query
     * @return true if xpath query valid
     */
    public boolean isValidXpathQuery(String query)
    {
        try
        {
            XPathReader reader = new XPathReader();
            reader.parse(query);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Execute a Lucene search
     * 
     * @param search
     *            Lucene search string to execute
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable luceneSearch(String search)
    {
        return luceneSearch(null, search);
    }

    /**
     * Execute a Lucene search
     * 
     * @param store
     *            Store reference to search against i.e. workspace://SpacesStore
     * @param search
     *            Lucene search string to execute
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable luceneSearch(String store, String search)
    {
        if (search != null && search.length() != 0)
        {
            Object[] results = query(store, search, null, SearchService.LANGUAGE_LUCENE);
            return Context.getCurrentContext().newArray(getScope(), results);
        }
        else
        {
            return Context.getCurrentContext().newArray(getScope(), 0);
        }
    }

    /**
     * Execute a Lucene search (sorted)
     * 
     * @param search
     *            Lucene search string to execute
     * @param sortColumn
     *            column to sort on
     * @param asc
     *            true => ascending sort
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable luceneSearch(String search, String sortColumn, boolean asc)
    {
        return luceneSearch(null, search, sortColumn, asc, 0);
    }

    public Scriptable luceneSearch(String search, String sortColumn, boolean asc, int max)
    {
        return luceneSearch(null, search, sortColumn, asc, max);
    }

    public Scriptable luceneSearch(String store, String search, String sortColumn, boolean asc)
    {
        return luceneSearch(store, search, sortColumn, asc, 0);
    }

    /**
     * Execute a Lucene search (sorted)
     * 
     * @param store
     *            Store reference to search against i.e. workspace://SpacesStore
     * @param search
     *            Lucene search string to execute
     * @param sortColumn
     *            column to sort on
     * @param asc
     *            true => ascending sort
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable luceneSearch(String store, String search, String sortColumn, boolean asc, int max)
    {
        if (search == null || search.length() == 0)
        {
            return Context.getCurrentContext().newArray(getScope(), 0);
        }

        SortColumn[] sort = null;
        if (sortColumn != null && sortColumn.length() != 0)
        {
            sort = new SortColumn[1];
            sort[0] = new SortColumn(sortColumn, asc);
        }
        Object[] results = query(store, search, sort, SearchService.LANGUAGE_LUCENE, max, 0);
        return Context.getCurrentContext().newArray(getScope(), results);
    }

    /**
     * Execute a saved Lucene search
     * 
     * @param savedSearch
     *            Node that contains the saved search XML content
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable savedSearch(ScriptNode savedSearch)
    {
        String search = null;

        // read the Saved Search XML on the specified node - and get the Lucene search from it
        try
        {
            if (savedSearch != null)
            {
                ContentReader content = this.services.getContentService().getReader(
                        savedSearch.getNodeRef(), ContentModel.PROP_CONTENT);
                if (content != null && content.exists())
                {
                    // get the root element
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(new StringReader(content.getContentString()));
                    Element rootElement = document.getRootElement();

                    Element queryElement = rootElement.element("query");
                    if (queryElement != null)
                    {
                        search = queryElement.getText();
                    }
                }
            }
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException("Failed to find or load saved Search: " + savedSearch.getNodeRef(), err);
        }

        if (search != null)
        {
            Object[] results = query(null, search, null, SearchService.LANGUAGE_LUCENE);
            return Context.getCurrentContext().newArray(getScope(), results);
        }
        else
        {
            return Context.getCurrentContext().newArray(getScope(), 0);
        }
    }

    /**
     * Execute a saved Lucene search
     * 
     * @param searchRef
     *            NodeRef string that points to the node containing saved search XML content
     * 
     * @return JavaScript array of Node results from the search - can be empty but not null
     */
    public Scriptable savedSearch(String searchRef)
    {
        if (searchRef != null)
        {
            return savedSearch(new ScriptNode(new NodeRef(searchRef), services, null));
        }
        else
        {
            return Context.getCurrentContext().newArray(getScope(), 0);
        }
    }

    /**
     * Searchs the store for all nodes with the given tag applied.
     * 
     * @param store
     *            store ref string, default used if null provided
     * @param tag
     *            tag name
     * @return ScriptNode[] nodes with tag applied
     */
    public ScriptNode[] tagSearch(String store, String tag)
    {
        StoreRef searchStoreRef = null;
        if (store != null)
        {
            searchStoreRef = new StoreRef(store);
        }
        else
        {
            searchStoreRef = this.storeRef;
        }

        List<NodeRef> nodeRefs = this.services.getTaggingService().findTaggedNodes(searchStoreRef, tag);
        ScriptNode[] nodes = new ScriptNode[nodeRefs.size()];
        int index = 0;
        for (NodeRef node : nodeRefs)
        {
            nodes[index] = new ScriptNode(node, this.services, getScope());
            index++;
        }
        return nodes;
    }

    /**
     * Execute a query based on the supplied search definition object.
     * 
     * Search object is defined in JavaScript thus:
     * 
     * <pre>
     * search
     * {
     *    query: string,          mandatory, in appropriate format and encoded for the given language
     *    store: string,          optional, defaults to 'workspace://SpacesStore'
     *    language: string,       optional, one of: lucene, xpath, fts-alfresco - defaults to 'lucene'
     *    templates: [],          optional, Array of query language template objects (see below) - if supported by the language 
     *    sort: [],               optional, Array of sort column objects (see below) - if supported by the language
     *    page: object,           optional, paging information object (see below) - if supported by the language
     *    namespace: string,      optional, the default namespace for properties
     *    defaultField: string,   optional, the default field for query elements when not explicit in the query
     *    defaultOperator: string,optional, the default operator for query elements when they are not explicit in the query AND or OR
     *    fieldFacets: [],        optional, Array of fields (as full QName strings) to facet against
     *    onerror: string         optional, result on error - one of: exception, no-results - defaults to 'exception'
     * }
     * 
     * sort
     * {
     *    column: string,         mandatory, sort column in appropriate format for the language
     *    ascending: boolean      optional, defaults to false
     * }
     * 
     * page
     * {
     *    maxItems: int,          optional, max number of items to return in result set
     *    skipCount: int          optional, number of items to skip over before returning results
     * }
     * 
     * template
     * {
     *    field: string,          mandatory, custom field name for the template
     *    template: string        mandatory, query template replacement for the template
     * }
     * 
     * Note that only some query languages support custom query templates, such as 'fts-alfresco'. 
     * See the following documentation for more details:
     * <a href="http://wiki.alfresco.com/wiki/Full_Text_Search_Query_Syntax#Templates">Templates</a>
     * </pre>
     * 
     * @param search
     *            Search definition object as above
     * 
     * @return Array of ScriptNode results
     */
    public Scriptable query(Object search)
    {
        return (Scriptable) queryResultSet(search).get("nodes", getScope());
    }

    @SuppressWarnings("unchecked")
    public Scriptable queryResultSet(Object search)
    {
        Object[] results = null;
        Map<String, Object> meta = null;

        // convert values from JS to Java - may contain native JS object such as ConsString etc.
        search = new ValueConverter().convertValueForJava(search);
        if (search instanceof Serializable)
        {
            Serializable obj = new ValueConverter().convertValueForRepo((Serializable) search);
            if (obj instanceof Map)
            {
                Map<Serializable, Serializable> def = (Map<Serializable, Serializable>) obj;

                // test for mandatory values
                String query = (String) def.get("query");
                if (query == null || query.length() == 0)
                {
                    throw new AlfrescoRuntimeException("Failed to search: Missing mandatory 'query' value.");
                }

                // collect optional values
                String store = (String) def.get("store");
                String language = (String) def.get("language");
                List<Map<Serializable, Serializable>> sort = (List<Map<Serializable, Serializable>>) def.get("sort");
                Map<Serializable, Serializable> page = (Map<Serializable, Serializable>) def.get("page");
                List<String> facets = (List<String>) def.get("fieldFacets");
                List<String> filterQueries = (List<String>) def.get("filterQueries");
                String namespace = (String) def.get("namespace");
                String onerror = (String) def.get("onerror");
                String defaultField = (String) def.get("defaultField");
                String defaultOperator = (String) def.get("defaultOperator");
                String searchTerm = (String) def.get("searchTerm");
                boolean spellCheck = Boolean.TRUE.equals(def.get("spellCheck"));

                // extract supplied values

                // sorting columns
                SortColumn[] sortColumns = null;
                if (sort != null)
                {
                    sortColumns = new SortColumn[sort.size()];
                    int index = 0;
                    for (Map<Serializable, Serializable> column : sort)
                    {
                        String strCol = (String) column.get("column");
                        if (strCol == null || strCol.length() == 0)
                        {
                            throw new AlfrescoRuntimeException("Failed to search: Missing mandatory 'sort: column' value.");
                        }
                        Boolean boolAsc = (Boolean) column.get("ascending");
                        boolean ascending = (boolAsc != null ? boolAsc.booleanValue() : false);
                        sortColumns[index++] = new SortColumn(strCol, ascending);
                    }
                }

                // paging settings
                int maxResults = -1;
                int skipResults = 0;
                if (page != null)
                {
                    if (page.get("maxItems") != null)
                    {
                        Object maxItems = page.get("maxItems");
                        if (maxItems instanceof Number)
                        {
                            maxResults = ((Number) maxItems).intValue();
                        }
                        else if (maxItems instanceof String)
                        {
                            // try and convert to int (which it what it should be!)
                            maxResults = Integer.parseInt((String) maxItems);
                        }
                    }
                    if (page.get("skipCount") != null)
                    {
                        Object skipCount = page.get("skipCount");
                        if (skipCount instanceof Number)
                        {
                            skipResults = ((Number) page.get("skipCount")).intValue();
                        }
                        else if (skipCount instanceof String)
                        {
                            skipResults = Integer.parseInt((String) skipCount);
                        }
                    }
                }

                // query templates
                Map<String, String> queryTemplates = null;
                List<Map<Serializable, Serializable>> templates = (List<Map<Serializable, Serializable>>) def.get("templates");
                if (templates != null)
                {
                    queryTemplates = new HashMap<String, String>(templates.size(), 1.0f);

                    for (Map<Serializable, Serializable> template : templates)
                    {
                        String field = (String) template.get("field");
                        if (field == null || field.length() == 0)
                        {
                            throw new AlfrescoRuntimeException("Failed to search: Missing mandatory 'template: field' value.");
                        }
                        String t = (String) template.get("template");
                        if (t == null || t.length() == 0)
                        {
                            throw new AlfrescoRuntimeException("Failed to search: Missing mandatory 'template: template' value.");
                        }
                        queryTemplates.put(field, t);
                    }
                }

                SearchParameters sp = new SearchParameters();
                sp.addStore(store != null ? new StoreRef(store) : this.storeRef);
                sp.setLanguage(language != null ? language : SearchService.LANGUAGE_LUCENE);
                sp.setQuery(query);
                sp.setSearchTerm(searchTerm);
                sp.setSpellCheck(spellCheck);
                if (defaultField != null)
                {
                    sp.setDefaultFieldName(defaultField);
                }
                if (defaultOperator != null && defaultOperator.length() != 0)
                {
                    try
                    {
                        sp.setDefaultOperator(Operator.valueOf(defaultOperator.toUpperCase()));
                    }
                    catch (IllegalArgumentException e)
                    {
                        // ignore invalid Operator and the default value will be used
                    }
                }
                if (namespace != null)
                {
                    sp.setNamespace(namespace);
                }
                if (maxResults > 0)
                {
                    sp.setLimit(maxResults);
                    sp.setLimitBy(LimitBy.FINAL_SIZE);
                }
                if (skipResults > 0)
                {
                    sp.setSkipCount(skipResults);
                }
                if (sort != null)
                {
                    for (SortColumn sd : sortColumns)
                    {
                        sp.addSort(sd.column, sd.asc);
                    }
                }
                if (queryTemplates != null)
                {
                    for (String field : queryTemplates.keySet())
                    {
                        sp.addQueryTemplate(field, queryTemplates.get(field));
                    }
                }
                if (facets != null)
                {
                    SolrFacetHelper solrFacetHelper = services.getSolrFacetHelper();
                    for (String field : facets)
                    {
                        if (field.isEmpty())
                        {
                            continue;
                        }
                        final String modifiedField = "@" + field;
                        if (solrFacetHelper.hasFacetQueries(modifiedField))
                        {
                            List<String> facetQueries = solrFacetHelper.getFacetQueries(modifiedField);
                            addFacetQuery(sp, field, facetQueries, query);
                        }
                        else
                        {
                            final FieldFacet fieldFacet;
                            if (solrFacetHelper.isSpecialFacetId(field))
                            {
                                fieldFacet = new FieldFacet(field);
                            }
                            else
                            {
                                fieldFacet = new FieldFacet(modifiedField);
                            }
                            sp.addFieldFacet(fieldFacet);
                        }
                    }
                }
                if (filterQueries != null)
                {
                    for (String filter : filterQueries)
                    {
                        sp.addFilterQuery(filter);
                    }
                }

                Map<Serializable, Serializable> highlighting = (Map<Serializable, Serializable>) def.get("highlight");
                if (highlighting != null)
                {
                    int snippetCount = this.getIntegerValue("snippetCount", 20, highlighting);
                    int fragmentSize = this.getIntegerValue("fragmentSize", 50, highlighting);
                    Integer maxAnalyzedChars = null;// see SEARCH-284
                    boolean usePhraseHighlighter = this.getBooleanValue("usePhraseHighlighter", true, highlighting);
                    boolean mergeContiguous = this.getBooleanValue("mergeContiguous", true, highlighting);

                    String prefix = (String) highlighting.get("prefix");
                    if (prefix == null)
                    {
                        prefix = "<mark>";
                    }
                    String postfix = (String) highlighting.get("postfix");
                    if (postfix == null)
                    {
                        postfix = "</mark>";
                    }

                    List<FieldHighlightParameters> fieldHighlightParameters = new ArrayList<FieldHighlightParameters>();
                    List<Map<Serializable, Serializable>> fields = (List<Map<Serializable, Serializable>>) highlighting.get("fields");
                    if (fields != null)
                    {
                        for (Map<Serializable, Serializable> field : fields)
                        {
                            String propertyName = (String) field.get("field");
                            if (propertyName != null)
                            {
                                fieldHighlightParameters.add(new FieldHighlightParameters(propertyName, snippetCount, fragmentSize, mergeContiguous, prefix, postfix));
                            }
                        }
                    }

                    GeneralHighlightParameters ghp = new GeneralHighlightParameters(snippetCount, fragmentSize, mergeContiguous, prefix, postfix, maxAnalyzedChars, usePhraseHighlighter, fieldHighlightParameters);
                    sp.setHighlight(ghp);
                }

                // error handling opions
                boolean exceptionOnError = true;
                if (onerror != null)
                {
                    if (onerror.equals("exception"))
                    {
                        // default value, do nothing
                    }
                    else if (onerror.equals("no-results"))
                    {
                        exceptionOnError = false;
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException("Failed to search: Unknown value supplied for 'onerror': " + onerror);
                    }
                }

                // execute search based on search definition
                Pair<Object[], Map<String, Object>> r = queryResultMeta(sp, exceptionOnError);
                results = r.getFirst();
                meta = r.getSecond();
            }
        }

        if (results == null)
        {
            results = new Object[0];
        }

        // construct a JS return object
        // {
        // nodes: [], // Array of ScriptNode results
        // meta: {
        // numberFound: long, // total number found in index, or -1 if not known or not supported by this resultset
        // facets: { // facets are returned for each field as requested in the SearchParameters fieldfacets
        // field: { // each field contains a map of facet to value
        // facet: value,
        // ...
        // },
        // ...
        // }
        // }
        // }
        Scriptable scope = getScope();
        Scriptable res = Context.getCurrentContext().newObject(scope);
        res.put("nodes", res, Context.getCurrentContext().newArray(scope, results));
        res.put("meta", res, meta);
        return res;
    }

    /**
     * Attempts to retrieve and parse an attribute in the supplied object to an integer. If the attribute cannot be found or cannot be parsed then the supplied default is returned.
     * 
     * @param attribute
     * @param defaultValue
     * @param sourceObject
     * @return
     */
    public int getIntegerValue(String attribute, int defaultValue, Map<Serializable, Serializable> sourceObject)
    {
        int intValue = defaultValue;
        Number configuredInteger = (Number) sourceObject.get(attribute);
        if (configuredInteger != null)
        {
            intValue = configuredInteger.intValue();
        }
        return intValue;
    }

    /**
     * Attempts to retrieve and parse an attribute in the supplied object to an integer. If the attribute cannot be found or cannot be parsed then the supplied default is returned.
     * 
     * @param attribute
     * @param defaultValue
     * @param sourceObject
     * @return
     */
    public boolean getBooleanValue(String attribute, boolean defaultValue, Map<Serializable, Serializable> sourceObject)
    {
        Boolean bool = (Boolean) sourceObject.get(attribute);
        if (bool == null)
        {
            bool = defaultValue;
        }
        return bool;
    }

    /**
     * Encode a string to ISO9075 - used to build valid paths for Lucene queries etc.
     * 
     * @param s
     *            Value to encode
     * 
     * @return encoded value
     */
    public String ISO9075Encode(String s)
    {
        return ISO9075.encode(s);
    }

    /**
     * Decode a string from ISO9075
     * 
     * @param s
     *            Value to decode
     * 
     * @return decoded value
     */
    public String ISO9075Decode(String s)
    {
        return ISO9075.decode(s);
    }

    /**
     * Execute the query
     * 
     * Removes any duplicates that may be present (ID search can cause duplicates - it is better to remove them here)
     * 
     * @param store
     *            StoreRef to search against - null for default configured store
     * @param search
     *            Lucene search to execute
     * @param sort
     *            Columns to sort by
     * @param language
     *            Search language to use e.g. SearchService.LANGUAGE_LUCENE
     * 
     * @return Array of Node objects
     */
    protected Object[] query(String store, String search, SortColumn[] sort, String language)
    {
        return query(store, search, sort, language, -1, 0);
    }

    /**
     * Execute the query
     * 
     * Removes any duplicates that may be present (ID search can cause duplicates - it is better to remove them here)
     * 
     * @param store
     *            StoreRef to search against - null for default configured store
     * @param search
     *            Lucene search to execute
     * @param sort
     *            Columns to sort by
     * @param language
     *            Search language to use e.g. SearchService.LANGUAGE_LUCENE
     * @param maxResults
     *            Maximum results to return if > 0
     * @param skipResults
     *            Results to skip in the result set
     * 
     * @return Array of Node objects
     */
    protected Object[] query(String store, String search, SortColumn[] sort, String language, int maxResults, int skipResults)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(store != null ? new StoreRef(store) : this.storeRef);
        sp.setLanguage(language != null ? language : SearchService.LANGUAGE_LUCENE);
        sp.setQuery(search);
        if (maxResults > 0)
        {
            sp.setLimit(maxResults);
            sp.setLimitBy(LimitBy.FINAL_SIZE);
        }
        if (skipResults > 0)
        {
            sp.setSkipCount(skipResults);
        }
        if (sort != null)
        {
            for (SortColumn sd : sort)
            {
                sp.addSort(sd.column, sd.asc);
            }
        }

        return query(sp, true);
    }

    /**
     * Execute the query
     * 
     * Removes any duplicates that may be present (ID search can cause duplicates - it is better to remove them here)
     * 
     * @param sp
     *            SearchParameters describing the search to execute.
     * @param exceptionOnError
     *            True to throw a runtime exception on error, false to return empty resultset
     * 
     * @return Array of Node objects
     */
    protected Object[] query(SearchParameters sp, boolean exceptionOnError)
    {
        return queryResultMeta(sp, exceptionOnError).getFirst();
    }

    /**
     * Execute the query
     * 
     * Removes any duplicates that may be present (ID search can cause duplicates - it is better to remove them here)
     * 
     * @param sp
     *            SearchParameters describing the search to execute.
     * @param exceptionOnError
     *            True to throw a runtime exception on error, false to return empty resultset
     * 
     * @return Pair containing Object[] of Node objects, and the ResultSet metadata hash.
     */
    protected Pair<Object[], Map<String, Object>> queryResultMeta(SearchParameters sp, boolean exceptionOnError)
    {
        Collection<ScriptNode> set = null;
        Map<String, Object> meta = new HashMap<>(8);

        long time = 0L;
        if (logger.isDebugEnabled())
        {
            logger.debug("query=" + sp.getQuery() + " limit=" + (sp.getLimitBy() != LimitBy.UNLIMITED ? sp.getLimit() : "none"));
            time = System.currentTimeMillis();
        }

        // perform the search against the repo
        ResultSet results = null;
        try
        {
            results = this.services.getSearchService().query(sp);

            // results nodes
            if (results.length() != 0)
            {
                NodeService nodeService = this.services.getNodeService();
                set = new LinkedHashSet<ScriptNode>(results.length(), 1.0f);
                for (ResultSetRow row : results)
                {
                    NodeRef nodeRef = row.getNodeRef();
                    if (nodeService.exists(nodeRef))
                    {
                        set.add(new ScriptNode(nodeRef, this.services, getScope()));
                    }
                }
            }
            // results metadata
            meta.put("numberFound", results.getNumberFound());
            meta.put("hasMore", results.hasMore());

            Map<String, Map<String, List<String>>> highlightingMeta = new HashMap<>();
            Map<NodeRef, List<Pair<String, List<String>>>> highlighting = results.getHighlighting();
            for (Entry<NodeRef, List<Pair<String, List<String>>>> highlight : highlighting.entrySet())
            {
                NodeRef nodeRef = highlight.getKey();

                Map<String, List<String>> scriptProperties = new HashMap<String, List<String>>();
                List<Pair<String, List<String>>> highlights = highlight.getValue();
                for (Pair<String, List<String>> propertyHighlight : highlights)
                {
                    String property = propertyHighlight.getFirst();
                    List<String> value = propertyHighlight.getSecond();
                    scriptProperties.put(property, value);
                }

                highlightingMeta.put(nodeRef.toString(), scriptProperties);
            }
            meta.put("highlighting", highlightingMeta);

            // results facets
            FacetLabelDisplayHandlerRegistry facetLabelDisplayHandlerRegistry = services.getFacetLabelDisplayHandlerRegistry();
            Map<String, List<ScriptFacetResult>> facetMeta = new HashMap<>();
            for (FieldFacet ff : sp.getFieldFacets())
            {
                // for each field facet, get the facet results
                List<Pair<String, Integer>> fs = results.getFieldFacet(ff.getField());
                List<ScriptFacetResult> facets = new ArrayList<>();
                for (Pair<String, Integer> f : fs)
                {
                    // ignore zero hit fields
                    if (f.getSecond() > 0)
                    {
                        String facetValue = f.getFirst();
                        FacetLabelDisplayHandler handler = facetLabelDisplayHandlerRegistry.getDisplayHandler(ff.getField());
                        String label = (handler == null) ? facetValue : handler.getDisplayLabel(facetValue).getLabel();
                        Integer labelIndex = (handler == null) ? -1 : handler.getDisplayLabel(facetValue).getLabelIndex();
                        facets.add(new ScriptFacetResult(facetValue, label, labelIndex, f.getSecond()));
                    }
                }
                // store facet results per field
                facetMeta.put(ff.getField(), facets);
            }

            // Start of bucketing
            // ACE-1615: Populate the facetMeta map with empty lists. If there is a
            // facet query with >0 hits, the relevant list will be populated
            // with the results, otherwise the list remains empty.
            for (String bucketedField : services.getSolrFacetHelper().getBucketedFieldFacets())
            {
                facetMeta.put(bucketedField, new ArrayList<ScriptFacetResult>());
            }
            Set<Entry<String, Integer>> facetQueries = results.getFacetQueries().entrySet();
            for (Entry<String, Integer> entry : facetQueries)
            {
                // ignore zero hit facet queries
                if (entry.getValue() > 0)
                {
                    String key = entry.getKey();
                    // for example the key could be: {!afts}@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]
                    // qName => @{http://www.alfresco.org/model/content/1.0}created
                    // 7 => {!afts}
                    key = key.substring(7);
                    String qName = key.substring(0, key.lastIndexOf(':'));

                    // Retrieve the previous facet queries
                    List<ScriptFacetResult> fqs = facetMeta.get(qName);
                    if (fqs == null)
                    {
                        fqs = new ArrayList<>();
                        logger.info("Field facet [" + key + "] has not been registered.");
                    }
                    // Get the handler for this qName
                    FacetLabelDisplayHandler handler = facetLabelDisplayHandlerRegistry.getDisplayHandler(qName);
                    FacetLabel facetLabel = (handler == null) ? new FacetLabel(key, key, -1) : handler.getDisplayLabel(key);

                    fqs.add(new ScriptFacetResult(facetLabel.getValue(), facetLabel.getLabel(), facetLabel.getLabelIndex(), entry.getValue()));
                }
            } // End of bucketing
            meta.put("facets", facetMeta);
            SpellCheckResult spellCheckResult = results.getSpellCheckResult();
            meta.put("spellcheck", new ScriptSpellCheckResult(
                    sp.getSearchTerm(),
                    spellCheckResult.getResultName(),
                    spellCheckResult.isSearchedFor(),
                    spellCheckResult.getResults(),
                    spellCheckResult.isSpellCheckExist()));
        }
        catch (Throwable err)
        {
            if (exceptionOnError)
            {
                throw new AlfrescoRuntimeException("Failed to execute search: " + sp.getQuery(), err);
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Failed to execute search: " + sp.getQuery(), err);
                // put expected values to handle case where exception occurs in search
                meta.put("numberFound", 0);
                meta.put("hasMore", false);
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
            if (logger.isDebugEnabled())
                logger.debug("query time: " + (System.currentTimeMillis() - time) + "ms");
        }

        Object[] res = set != null ? set.toArray(new Object[(set.size())]) : new Object[0];
        return new Pair<Object[], Map<String, Object>>(res, meta);
    }

    /**
     * Adds facet queries to the {@code SearchParameters}
     * 
     * @param sp
     *            the SearchParameters
     * @param field
     *            the requested field facet
     * @param facetQueries
     *            list of generated facet queries
     * @param query
     *            the requested search query
     */
    protected void addFacetQuery(SearchParameters sp, String field, List<String> facetQueries, String query)
    {
        // Workaround for ACE-1605
        if (query.indexOf(field) < 0)
        {
            for (String fq : facetQueries)
            {
                sp.addFacetQuery(fq);
            }
        }
        else
        {
            String fq = services.getSolrFacetHelper().createFacetQueriesFromSearchQuery(field, query);
            if (fq != null)
            {
                sp.addFacetQuery(fq);
            }
        }
    }

    /**
     * Search sort column
     */
    public class SortColumn
    {
        /**
         * Constructor
         * 
         * @param column
         *            column to sort on
         * @param asc
         *            sort direction
         */
        public SortColumn(String column, boolean asc)
        {
            this.column = column;
            this.asc = asc;
        }

        public String column;
        public boolean asc;
    }
}
