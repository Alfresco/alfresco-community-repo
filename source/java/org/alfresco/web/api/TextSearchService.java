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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Alfresco Text (simple) Search Service
 * 
 * @author davidc
 */
public class TextSearchService implements APIService
{
    // NOTE: startPage and startIndex are 1 offset.

    // search parameters 
    // TODO: allow configuration of these
    private static final StoreRef searchStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    private static final int itemsPerPage = 10;

    // dependencies
    private ServiceRegistry serviceRegistry;
    private SearchService searchService;
    private TemplateService templateService;

    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#init(javax.servlet.ServletContext)
     */
    public void init(ServletContext context)
    {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(context);
        init(appContext);
    }

    /**
     * Internal initialisation
     * 
     * @param context
     */
    private void init(ApplicationContext context)
    {
        serviceRegistry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
        searchService = (SearchService)context.getBean(ServiceRegistry.SEARCH_SERVICE.getLocalName());
        templateService = (TemplateService)context.getBean(ServiceRegistry.TEMPLATE_SERVICE.getLocalName());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#execute(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse)
     */
    public void execute(APIRequest req, APIResponse res)
        throws IOException
    {
        //
        // execute the search
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
        
        SearchResult results = search(searchTerms, startPage);
        
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
        
        // execute template
        Map<String, Object> searchModel = new HashMap<String, Object>(7, 1.0f);
        searchModel.put("request", req);
        searchModel.put("search", results);
        res.setContentType(contentType + ";charset=UTF-8");
        templateService.processTemplateString(null, template, searchModel, res.getWriter());
    }

    /**
     * Execute the search
     * 
     * @param searchTerms
     * @param startPage
     * @return
     */
    private SearchResult search(String searchTerms, int startPage)
    {
        SearchResult searchResult = null;
        ResultSet results = null;
        
        try
        {
            // Construct search statement
            String[] terms = searchTerms.split(" "); 
            Map<String, Object> statementModel = new HashMap<String, Object>(7, 1.0f);
            statementModel.put("terms", terms);
            String query = templateService.processTemplateString(null, QUERY_STATEMENT, statementModel);
            results = searchService.query(searchStore, SearchService.LANGUAGE_LUCENE, query);

            int totalResults = results.length();
            int totalPages = (totalResults / itemsPerPage);
            totalPages += (totalResults % itemsPerPage != 0) ? 1 : 0;
            
            // are we out-of-range
            if (totalPages != 0 && (startPage < 1 || startPage > totalPages))
            {
                throw new APIException("Start page " + startPage + " is outside boundary of " + totalPages + " pages");
            }

            searchResult = new SearchResult();
            searchResult.setSearchTerms(searchTerms);
            searchResult.setItemsPerPage(itemsPerPage);
            searchResult.setStartPage(startPage);
            searchResult.setTotalPages(totalPages);
            searchResult.setTotalResults(totalResults);
            searchResult.setStartIndex(((startPage -1) * itemsPerPage) + 1);
            searchResult.setTotalPageItems(Math.min(itemsPerPage, totalResults - searchResult.getStartIndex() + 1));
            TemplateNode[] nodes = new TemplateNode[searchResult.getTotalPageItems()];
            for (int i = 0; i < searchResult.getTotalPageItems(); i++)
            {
                nodes[i] = new TemplateNode(results.getNodeRef(i + searchResult.getStartIndex() - 1), serviceRegistry, null);
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
        private int itemsPerPage;
        private int totalPages;
        private int totalResults;
        private int totalPageItems;
        private int startPage;
        private int startIndex;
        private TemplateNode[] results;
        
        
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

        /*package*/ void setResults(TemplateNode[] results)
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

        public String getId()
        {
            if (id == null)
            {
                id = GUID.generate();
            }
            return id;
        }
    }
    
    
    // TODO: place into accessible file
    private final static String ATOM_TEMPLATE = 
        "<#assign dateformat=\"yyyy-MM-dd\">" +
        "<#assign timeformat=\"HH:mm:sszzz\">" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\">\n" +
        "  <title>Alfresco Search: ${search.searchTerms}</title>\n" + 
        "  <updated>2003-12-13T18:30:02Z</updated>\n" +   // TODO:
        "  <author>\n" + 
        "    <name>Alfresco</name>\n" +   // TODO: Issuer of search?
        "  </author>\n" + 
        "  <id>urn:uuid:${search.id}</id>\n" +
        "  <opensearch:totalResults>${search.totalResults}</opensearch:totalResults>\n" +
        "  <opensearch:startIndex>${search.startIndex}</opensearch:startIndex>\n" +
        "  <opensearch:itemsPerPage>${search.itemsPerPage}</opensearch:itemsPerPage>\n" +
        "  <opensearch:Query role=\"request\" searchTerms=\"${search.searchTerms}\" startPage=\"${search.startPage}\"/>\n" +
        "  <link rel=\"alternate\" href=\"${request.servicePath}/search/text?q=${search.searchTerms}&amp;p=${search.startPage}&amp;format=html\" type=\"text/html\"/>\n" +
        "  <link rel=\"self\" href=\"${request.servicePath}/search/text?q=${search.searchTerms}&amp;p=${search.startPage}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "  <link rel=\"first\" href=\"${request.servicePath}/search/text?q=${search.searchTerms}&amp;p=1&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "<#if search.startPage &gt; 1>" +
        "  <link rel=\"previous\" href=\"${request.servicePath}/search/text?q=${search.searchTerms}&amp;p=${search.startPage - 1}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "</#if>" +
        "<#if search.startPage &lt; search.totalPages>" +
        "  <link rel=\"next\" href=\"${request.servicePath}/search/text?q=${search.searchTerms}&amp;p=${search.startPage + 1}&amp;format=atom\" type=\"application/atom+xml\"/>\n" + 
        "</#if>" +
        "  <link rel=\"last\" href=\"${request.servicePath}/search/text?q=${search.searchTerms}&amp;p=${search.totalPages}&amp;format=atom\" type=\"application/atom+xml\"/>\n" +
        "  <link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"${request.servicePath}/search/text/textsearchdescription.xml\"/>\n" +
        "<#list search.results as row>" +            
        "  <entry>\n" +
        "    <title>${row.name}</title>\n" +
        "    <link href=\"${request.path}/${row.url}\"/>\n" +
        "    <id>urn:uuid:${row.id}</id>\n" +
        "    <updated>${row.properties.modified?string(dateformat)}T${row.properties.modified?string(timeformat)}</updated>\n" +
        "    <summary>${row.properties.description}</summary>\n" +
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
        "    Results <b>${search.startIndex}</b> - <b>${search.startIndex + search.totalPageItems - 1}</b> of <b>${search.totalResults}</b> for <b>${search.searchTerms}.</b>\n" +
        "    <ul>\n" +
        "<#list search.results as row>" +            
        "      <li>\n" +
        "        <a href=\"${request.path}/${row.url}\">\n" +
        "           ${row.name}\n" +
        "        </a>\n" +
        "        <div>\n" +
        "          ${row.properties.description}\n" +
        "        </div>\n" +
        "      </li>\n" +
        "</#list>" +
        "    </ul>\n" +
        "    <a href=\"${request.servicePath}/search/text?q=${search.searchTerms}&p=1\">first</a>" +
        "<#if search.startPage &gt; 1>" +
        "    <a href=\"${request.servicePath}/search/text?q=${search.searchTerms}&p=${search.startPage - 1}\">previous</a>" +
        "</#if>" +
        "    <a href=\"${request.servicePath}/search/text?q=${search.searchTerms}&p=${search.startPage}\">${search.startPage}</a>" +
        "<#if search.startPage &lt; search.totalPages>" +
        "    <a href=\"${request.servicePath}/search/text?q=${search.searchTerms}&p=${search.startPage + 1}\">next</a>" +
        "</#if>" +
        "    <a href=\"${request.servicePath}/search/text?q=${search.searchTerms}&p=${search.totalPages}\">last</a>" +
        "  </body>\n" +
        "</html>\n";

    // TODO: place into accessible file
    private final static String QUERY_STATEMENT =  
        "( " +
        "  TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" AND " +
        "  (" +
        "    (" +
        "<#list 1..terms?size as i>" +
        "      @\\{http\\://www.alfresco.org/model/content/1.0\\}name:${terms[i - 1]}" +
        "<#if (i < terms?size)>" +
        "      OR " +
        "</#if>" +
        "</#list>" +
        "    ) " +
        "    ( " +
        "<#list 1..terms?size as i>" +
        "      TEXT:${terms[i - 1]}" +
        "<#if (i < terms?size)>" +
        "      OR " +
        "</#if>" +
        "</#list>" +
        "    )" +
        "  )" +
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
        TextSearchService method = new TextSearchService();
        method.init(context);
        method.test();
    }

    /**
     * Simple test that can be executed outside of web context
     * 
     * TODO: Move to test harness
     */
    private void test()
    {
        SearchResult result = search("alfresco tutorial", 1);

        Map<String, Object> searchModel = new HashMap<String, Object>(7, 1.0f);
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("servicePath", "http://localhost:8080/alfresco/service");
        request.put("path", "http://localhost:8080/alfresco");
        searchModel.put("request", request);
        searchModel.put("search", result);
        
        StringWriter rendition = new StringWriter();
        PrintWriter writer = new PrintWriter(rendition);
        templateService.processTemplateString(null, HTML_TEMPLATE, searchModel, writer);
        System.out.println(rendition.toString());
    }

}