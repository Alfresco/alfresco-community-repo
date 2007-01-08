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
package org.alfresco.web.api.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.template.ISO8601DateFormatMethod;
import org.alfresco.repo.template.UrlEncodeMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.web.api.APIException;
import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIRequest.HttpMethod;
import org.alfresco.web.api.APIRequest.RequiredAuthentication;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;


/**
 * Alfresco Text (simple) Search Service
 * 
 * @author davidc
 */
public class TextSearch extends APIServiceImpl
{
    // Logger
    private static final Log logger = LogFactory.getLog(TextSearch.class);

    // search parameters 
    // TODO: allow configuration of search store
    protected static final StoreRef SEARCH_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final int DEFAULT_ITEMS_PER_PAGE = 10;

    // dependencies
    protected SearchService searchService;

    // icon resolver
    protected TemplateImageResolver iconResolver = new TemplateImageResolver()
    {
        public String resolveImagePathForName(String filename, boolean small)
        {
            return Utils.getFileTypeImage(getAPIContext(), filename, small);
        }
    };

    /**
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getRequiredAuthentication()
     */
    public RequiredAuthentication getRequiredAuthentication()
    {
        return APIRequest.RequiredAuthentication.User;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getHttpMethod()
     */
    public HttpMethod getHttpMethod()
    {
        return APIRequest.HttpMethod.GET;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    public void execute(APIRequest req, APIResponse res)
        throws IOException
    {
        //
        // process parameters
        //
        
        String searchTerms = req.getParameter("q");
        String startPageArg = req.getParameter("p");
        int startPage = 1;
        try
        {
            startPage = new Integer(startPageArg);
        }
        catch(NumberFormatException e)
        {
            // NOTE: use default startPage
        }
        String itemsPerPageArg = req.getParameter("c");
        int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        try
        {
            itemsPerPage = new Integer(itemsPerPageArg);
        }
        catch(NumberFormatException e)
        {
            // NOTE: use default itemsPerPage
        }
        Locale locale = I18NUtil.getLocale();
        String language = req.getParameter("l");
        if (language != null && language.length() > 0)
        {
            // NOTE: Simple conversion from XML Language Id to Java Locale Id
            locale = new Locale(language.replace("-", "_"));
        }
        
        //
        // execute the search
        //
        
        SearchResult results = search(searchTerms, startPage, itemsPerPage, locale);
        
        //
        // render the results
        //
        
        String contentType = APIResponse.HTML_TYPE;
        String template = HTML_TEMPLATE;

        // TODO: data-drive this
        String format = req.getParameter("format");
        if (format != null)
        {
            if (format.equals("atom"))
            {
                contentType = APIResponse.ATOM_TYPE;
                template = ATOM_TEMPLATE;
            }
        }
        
        Map<String, Object> model = createTemplateModel(req, res);
        model.put("search", results);
        res.setContentType(contentType + ";charset=UTF-8");
        renderTemplate(template, model, res);
    }

    /**
     * Execute the search
     * 
     * @param searchTerms
     * @param startPage
     * @return
     */
    private SearchResult search(String searchTerms, int startPage, int itemsPerPage, Locale locale)
    {
        SearchResult searchResult = null;
        ResultSet results = null;
        
        try
        {
            // construct search statement
            String[] terms = searchTerms.split(" "); 
            Map<String, Object> statementModel = new HashMap<String, Object>(7, 1.0f);
            statementModel.put("terms", terms);
            String query = getTemplateService().processTemplateString(null, QUERY_STATEMENT, statementModel);
            
            // execute query
            if (logger.isDebugEnabled())
            {
                logger.debug("Search parameters: searchTerms=" + searchTerms + ", startPage=" + startPage + ", itemsPerPage=" + itemsPerPage + ", search locale=" + locale.toString());
                logger.debug("Issuing lucene search: " + query);
            }

            SearchParameters parameters = new SearchParameters();
            parameters.addStore(SEARCH_STORE);
            parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
            parameters.setQuery(query);
            if (locale != null)
            {
                parameters.addLocale(locale);
            }
            results = searchService.query(parameters);
            int totalResults = results.length();
            
            if (logger.isDebugEnabled())
                logger.debug("Results: " + totalResults + " rows (limited: " + results.getResultSetMetaData().getLimitedBy() + ")");
            
            // are we out-of-range
            int totalPages = (totalResults / itemsPerPage);
            totalPages += (totalResults % itemsPerPage != 0) ? 1 : 0;
            if (totalPages != 0 && (startPage < 1 || startPage > totalPages))
            {
                throw new APIException("Start page " + startPage + " is outside boundary of " + totalPages + " pages");
            }

            // construct search result
            searchResult = new SearchResult();
            searchResult.setSearchTerms(searchTerms);
            searchResult.setLocale(locale);
            searchResult.setItemsPerPage(itemsPerPage);
            searchResult.setStartPage(startPage);
            searchResult.setTotalPages(totalPages);
            searchResult.setTotalResults(totalResults);
            searchResult.setStartIndex(((startPage -1) * itemsPerPage) + 1);
            searchResult.setTotalPageItems(Math.min(itemsPerPage, totalResults - searchResult.getStartIndex() + 1));
            SearchTemplateNode[] nodes = new SearchTemplateNode[searchResult.getTotalPageItems()];
            for (int i = 0; i < searchResult.getTotalPageItems(); i++)
            {
                NodeRef node = results.getNodeRef(i + searchResult.getStartIndex() - 1);
                float score = results.getScore(i + searchResult.getStartIndex() - 1);
                nodes[i] = new SearchTemplateNode(node, score);
            }
            searchResult.setResults(nodes);
            return searchResult;
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }        
    }

    /**
     * Search Result
     *  
     * @author davidc
     */
    public static class SearchResult
    {
        private String id;
        private String searchTerms;
        private Locale locale;
        private int itemsPerPage;
        private int totalPages;
        private int totalResults;
        private int totalPageItems;
        private int startPage;
        private int startIndex;
        private SearchTemplateNode[] results;
        
        
        public int getItemsPerPage()
        {
            return itemsPerPage;
        }
        
        /*package*/ void setItemsPerPage(int itemsPerPage)
        {
            this.itemsPerPage = itemsPerPage;
        }

        public TemplateNode[] getResults()
        {
            return results;
        }

        /*package*/ void setResults(SearchTemplateNode[] results)
        {
            this.results = results;
        }

        public int getStartIndex()
        {
            return startIndex;
        }

        /*package*/ void setStartIndex(int startIndex)
        {
            this.startIndex = startIndex;
        }

        public int getStartPage()
        {
            return startPage;
        }

        /*package*/ void setStartPage(int startPage)
        {
            this.startPage = startPage;
        }

        public int getTotalPageItems()
        {
            return totalPageItems;
        }

        /*package*/ void setTotalPageItems(int totalPageItems)
        {
            this.totalPageItems = totalPageItems;
        }

        public int getTotalPages()
        {
            return totalPages;
        }

        /*package*/ void setTotalPages(int totalPages)
        {
            this.totalPages = totalPages;
        }

        public int getTotalResults()
        {
            return totalResults;
        }

        /*package*/ void setTotalResults(int totalResults)
        {
            this.totalResults = totalResults;
        }

        public String getSearchTerms()
        {
            return searchTerms;
        }

        /*package*/ void setSearchTerms(String searchTerms)
        {
            this.searchTerms = searchTerms;
        }

        public Locale getLocale()
        {
            return locale;
        }
        
        /**
         * @return XML 1.0 Language Identification
         */
        public String getLocaleId()
        {
            return locale.toString().replace('_', '-');
        }

        /*package*/ void setLocale(Locale locale)
        {
            this.locale = locale;
        }

        public String getId()
        {
            if (id == null)
            {
                id = GUID.generate();
            }
            return id;
        }
    }
    
    /**
     * Search result row template node
     */
    public class SearchTemplateNode extends TemplateNode
    {
        private static final long serialVersionUID = -1791913270786140012L;
        private float score;

        /**
         * Construct
         * 
         * @param nodeRef
         * @param score
         */
        public SearchTemplateNode(NodeRef nodeRef, float score)
        {
            super(nodeRef, getServiceRegistry(), iconResolver);
            this.score = score;
        }
        
        /**
         * Gets the result row score
         * 
         * @return  score
         */
        public float getScore()
        {
            return score;
        }
    }
        
    
    // TODO: place into accessible file
    private final static String ATOM_TEMPLATE = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:relevance=\"http://a9.com/-/opensearch/extensions/relevance/1.0/\">\n" +
        "  <generator version=\"${agent.version}\">Alfresco (${agent.edition})</generator>\n" +
        "  <title>Alfresco Search: ${search.searchTerms}</title>\n" + 
        "  <updated>${xmldate(date)}</updated>\n" +
        "  <icon>${request.path}/images/logo/AlfrescoLogo16.ico</icon>\n" +
        "  <author>\n" + 
        "    <name><#if request.authenticatedUsername?exists>${request.authenticatedUsername}<#else>unknown</#if></name>\n" +
        "  </author>\n" + 
        "  <id>urn:uuid:${search.id}</id>\n" +
        "  <opensearch:totalResults>${search.totalResults}</opensearch:totalResults>\n" +
        "  <opensearch:startIndex>${search.startIndex}</opensearch:startIndex>\n" +
        "  <opensearch:itemsPerPage>${search.itemsPerPage}</opensearch:itemsPerPage>\n" +
        "  <opensearch:Query role=\"request\" searchTerms=\"${search.searchTerms}\" startPage=\"${search.startPage}\" count=\"${search.itemsPerPage}\" language=\"${search.localeId}\"/>\n" +
        "  <link rel=\"alternate\" href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&amp;p=${search.startPage}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string(\"true\",\"\")}&amp;format=html\" type=\"text/html\"/>\n" +
        "  <link rel=\"self\" href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&amp;p=${search.startPage}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string(\"true\",\"\")}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "  <link rel=\"first\" href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&amp;p=1&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string(\"true\",\"\")}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "<#if search.startPage &gt; 1>" +
        "  <link rel=\"previous\" href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&amp;p=${search.startPage - 1}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string(\"true\",\"\")}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "</#if>" +
        "<#if search.startPage &lt; search.totalPages>" +
        "  <link rel=\"next\" href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&amp;p=${search.startPage + 1}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string(\"true\",\"\")}&amp;format=atom\" type=\"application/atom+xml\"/>\n" + 
        "</#if>" +
        "  <link rel=\"last\" href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&amp;p=${search.totalPages}&amp;c=${search.itemsPerPage}&amp;l=${search.localeId}&amp;guest=${request.guest?string(\"true\",\"\")}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "  <link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"${request.servicePath}/search/text/textsearchdescription.xml\"/>\n" +
        "<#list search.results as row>" +            
        "  <entry>\n" +
        "    <title>${row.name}</title>\n" +
        "    <link href=\"${request.path}${row.url}\"/>\n" +
        "    <icon>${request.path}${row.icon16}</icon>\n" +  // TODO: Standard for entry icons?
        "    <id>urn:uuid:${row.id}</id>\n" +
        "    <updated>${xmldate(row.properties.modified)}</updated>\n" +
        "    <summary>${row.properties.description}</summary>\n" +
        "    <author>\n" + 
        "      <name>${row.properties.creator}</name>\n" +
        "    </author>\n" + 
        "    <relevance:score>${row.score}</relevance:score>\n" +
        "  </entry>\n" +
        "</#list>" +
        "</feed>";        
        
    // TODO: place into accessible file
    private final static String HTML_TEMPLATE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
        "  <head profile=\"http://a9.com/-/spec/opensearch/1.1/\">\n" + 
        "    <title>Alfresco Text Search: ${search.searchTerms}</title>\n" + 
        "    <link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"${request.servicePath}/search/text/textsearchdescription.xml\" title=\"Alfresco Text Search\"/>\n" +
        "    <meta name=\"totalResults\" content=\"${search.totalResults}\"/>\n" +
        "    <meta name=\"startIndex\" content=\"${search.startIndex}\"/>\n" +
        "    <meta name=\"itemsPerPage\" content=\"${search.itemsPerPage}\"/>\n" +
        "  </head>\n" +
        "  <body>\n" +
        "    <h2>Alfresco Text Search</h2>\n" +
        "    Results <b>${search.startIndex}</b> - <b>${search.startIndex + search.totalPageItems - 1}</b> of <b>${search.totalResults}</b> for <b>${search.searchTerms}</b> " +
            "visible to user <b><#if request.authenticatedUsername?exists>${request.authenticatedUsername}<#else>unknown</#if>.</b>\n" + 
        "    <ul>\n" +
        "<#list search.results as row>" +            
        "      <li>\n" +
        "        <img src=\"${request.path}${row.icon16}\"/>" +
        "        <a href=\"${request.path}${row.url}\">${row.name}</a>\n" +
        "        <div>\n" +
        "          ${row.properties.description}\n" +
        "        </div>\n" +
        "      </li>\n" +
        "</#list>" +
        "    </ul>\n" +
        "    <a href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&p=1&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string(\"true\",\"\")}\">first</a>" +
        "<#if search.startPage &gt; 1>" +
        "    <a href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&p=${search.startPage - 1}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string(\"true\",\"\")}\">previous</a>" +
        "</#if>" +
        "    <a href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&p=${search.startPage}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string(\"true\",\"\")}\">${search.startPage}</a>" +
        "<#if search.startPage &lt; search.totalPages>" +
        "    <a href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&p=${search.startPage + 1}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string(\"true\",\"\")}\">next</a>" +
        "</#if>" +
        "    <a href=\"${request.servicePath}/search/text?q=${urlencode(search.searchTerms)}&p=${search.totalPages}&c=${search.itemsPerPage}&l=${search.localeId}&guest=${request.guest?string(\"true\",\"\")}\">last</a>" +
        "  </body>\n" +
        "</html>\n";

    // TODO: place into accessible file
    private final static String QUERY_STATEMENT =  
        "(" +
          "TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" AND " +
          "(" +
            "(" +
        "<#list 1..terms?size as i>" +
              "@\\{http\\://www.alfresco.org/model/content/1.0\\}name:${terms[i - 1]}" +
        "<#if (i < terms?size)>" +
             " OR " +
        "</#if>" +
        "</#list>" +
            ") " +
            "(" +
        "<#list 1..terms?size as i>" +
              "TEXT:${terms[i - 1]}" +
        "<#if (i < terms?size)>" +
             " OR " +
        "</#if>" +
        "</#list>" +
            ")" +
          ")" +
        ")";

    
    
    /**
     * Simple test that can be executed outside of web context
     *  
     * TODO: Move to test harness
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
        TextSearch method = new TextSearch();
        method.setServiceRegistry((ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY));
        method.setTemplateService((TemplateService)context.getBean(ServiceRegistry.TEMPLATE_SERVICE.getLocalName()));
        method.setSearchService((SearchService)context.getBean(ServiceRegistry.SEARCH_SERVICE.getLocalName()));
        method.setDescriptorService((DescriptorService)context.getBean(ServiceRegistry.DESCRIPTOR_SERVICE.getLocalName()));
        method.setHttpUri("/search/text");
        method.test();
    }

    /**
     * Simple test that can be executed outside of web context
     * 
     * TODO: Move to test harness
     */
    private void test()
    {
        SearchResult result = search("alfresco tutorial", 1, 5, I18NUtil.getLocale());

        Map<String, Object> searchModel = new HashMap<String, Object>(7, 1.0f);
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("servicePath", "http://localhost:8080/alfresco/service");
        request.put("path", "http://localhost:8080/alfresco");
        request.put("guest", false);
        searchModel.put("xmldate", new ISO8601DateFormatMethod());
        searchModel.put("urlencode", new UrlEncodeMethod());
        searchModel.put("date", new Date());
        searchModel.put("agent", getDescriptorService().getServerDescriptor());
        searchModel.put("request", request);
        searchModel.put("search", result);
        
        StringWriter rendition = new StringWriter();
        PrintWriter writer = new PrintWriter(rendition);
        getTemplateService().processTemplateString(null, ATOM_TEMPLATE, searchModel, writer);
        System.out.println(rendition.toString());
    }

}