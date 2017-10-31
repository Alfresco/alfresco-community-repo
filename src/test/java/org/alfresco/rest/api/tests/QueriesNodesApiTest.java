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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.testing.category.RedundantTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
* V1 REST API tests for pre-defined 'live' search Queries on Nodes
 * 
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/nodes} </li>
 * </ul>
 *
 * @author janv
 */
public class QueriesNodesApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_QUERIES_LSN = "queries/nodes";

    private static final String DEAFULT_QUERY =
        "\"%s\" AND " +
        "(+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\") AND " +
        "-TYPE:\"cm:thumbnail\" AND " +
        "-TYPE:\"cm:failedThumbnail\" AND " +
        "-TYPE:\"cm:rating\" AND " +
        "-TYPE:\"fm:post\" AND " +
        "-TYPE:\"st:site\" AND " +
        "-ASPECT:\"st:siteContainer\" AND " +
        "-ASPECT:\"sys:hidden\" AND " +
        "-cm:creator:system AND " +
        "-QNAME:comment\\-* ";

    private static final String NODE_TYPE_QUERY =
        "\"%s\" AND " +
        "(+TYPE:\"%s\") AND " +
        "-ASPECT:\"sys:hidden\" AND " +
        "-cm:creator:system AND " +
        "-QNAME:comment\\-* ";

    private static final String ROOT_NODE_QUERY_PREFIX =
        "PATH:\"";
    private static final String ROOT_NODE_QUERY_SUFFIX =
        "//*\" " +
        "AND " +
        "(\"%s\") AND " +
        "(+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\") " +
        "AND -TYPE:\"cm:thumbnail\" AND " +
        "-TYPE:\"cm:failedThumbnail\" AND " +
        "-TYPE:\"cm:rating\" AND " +
        "-TYPE:\"fm:post\" AND " +
        "-TYPE:\"st:site\" AND " +
        "-ASPECT:\"st:siteContainer\" AND " +
        "-ASPECT:\"sys:hidden\" AND " +
        "-cm:creator:system AND " +
        "-QNAME:comment\\-* ";

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
    
    /**
     * Test basic api for nodes using parameter term with white-spaces.(REPO-1154)
     *
     * <p>
     * GET:
     * </p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/nodes}
     * 
     * @throws Exception
     */
    @Test
    public void testSearchTermWhiteSpace() throws Exception
    {
        setRequestContext(user1);

        String myFolderNodeId = getMyNodeId();

        String parentFolder = createFolder(myFolderNodeId, "folder term1").getId();
        //I use "find123" and "find123 find", the search using second term must return less result
        //The space must not break the query
        String childTerm = "find" + Math.random();
        String childTermWS = childTerm + " " + "find";

        Map<String, String> docProps = new HashMap<>(2);
        docProps.put("cm:title", childTerm);
        docProps.put("cm:description", childTerm);
        createTextFile(parentFolder, childTerm, childTerm, "UTF-8", docProps);

        docProps.put("cm:title", childTermWS);
        docProps.put("cm:description", childTermWS);
        createTextFile(parentFolder, childTermWS, childTermWS, "UTF-8", docProps);

        Paging paging = getPaging(0, 100);
        HashMap<String, String> params = new HashMap<>(1);
        params.put(Queries.PARAM_TERM, childTerm);
        HttpResponse response = getAll(URL_QUERIES_LSN, paging, params, 200);
        List<Node> nodesChildTerm = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        //check if the search returns all nodes which contain that query term
        assertEquals(2, nodesChildTerm.size());

        params.put(Queries.PARAM_TERM, childTermWS);
        response = getAll(URL_QUERIES_LSN, paging, params, 200);
        List<Node> nodesChildTermWS = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        //check if search works for words with space and the space don't break the query
        assertEquals(1, nodesChildTermWS.size());
        assertTrue(nodesChildTerm.size() >= nodesChildTermWS.size());

    }

    private List<Node> checkApiCall(String pathRegex, String queryForm, String term, String nodeType, String rootNodeId,
                                    String include, String orderBy, Paging paging, int expectedStatus,
                                    Boolean checkNodeOrderAsc, Boolean propertyNullCheck,
                                    List<String> ids) throws Exception
    {
        Map<String, String> params = new HashMap<>(1);
        params.put(Queries.PARAM_TERM, term);
        if (include != null)
        {
            params.put(Queries.PARAM_INCLUDE, include);
        }
        if (nodeType != null)
        {
            params.put(Queries.PARAM_NODE_TYPE, nodeType);
        }
        if (rootNodeId != null)
        {
            params.put(Queries.PARAM_ROOT_NODE_ID, rootNodeId);
        }
        if (orderBy != null)
        {
            params.put(Queries.PARAM_ORDERBY, orderBy);
        }

        // Create the list of NodeRefs returned from the dummy search
        dummySearchServiceQueryNodeRefs.clear();
        for (String id: ids)
        {
            NodeRef nodeRef = getNodeRef(id);
            dummySearchServiceQueryNodeRefs.add(nodeRef);
        }

        // Mix up the NodeRefs returned from the dummy search as the client side code is going to be doing the sorting.
        if (orderBy != null)
        {
            Collections.shuffle(dummySearchServiceQueryNodeRefs);
        }

        HttpResponse response = getAll(URL_QUERIES_LSN, paging, params, 200);
        List<Node> nodes = null;

        if (expectedStatus == 200)
        {
            String termWithEscapedAsterisks = term.replaceAll("\\*", "\\\\*").replaceAll("\"", "\\\\\"");
            String expectedQuery =
                  DEAFULT_QUERY.equals(queryForm)
                ? String.format(DEAFULT_QUERY, termWithEscapedAsterisks)
                : NODE_TYPE_QUERY.equals(queryForm)
                ? String.format(NODE_TYPE_QUERY, termWithEscapedAsterisks, nodeType)
                : ROOT_NODE_QUERY_SUFFIX.equals(queryForm)
                ? String.format(ROOT_NODE_QUERY_SUFFIX, termWithEscapedAsterisks)
                : "TODO";
            ArgumentCaptor<SearchParameters> searchParametersCaptor = ArgumentCaptor.forClass(SearchParameters.class);
            verify(mockSearchService, times(++callCountToMockSearchService)).query(searchParametersCaptor.capture());
            SearchParameters parameters = searchParametersCaptor.getValue();
            String query = parameters.getQuery();
            if (ROOT_NODE_QUERY_SUFFIX.equals(queryForm))
            {
                assertNotNull(query);
                assertTrue("Query should have started with "+ROOT_NODE_QUERY_PREFIX+" but was "+query, query.startsWith(ROOT_NODE_QUERY_PREFIX));
                assertTrue("Query should have ended with "+expectedQuery+" but was "+query, query.endsWith(expectedQuery));
                String path = query.substring(ROOT_NODE_QUERY_PREFIX.length(), query.length()-expectedQuery.length());
                assertTrue("Query path should match "+pathRegex+" but was "+path, Pattern.matches(pathRegex, path));
            }
            else
            {
                assertEquals("Query", expectedQuery, query);
            }
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, ids, checkNodeOrderAsc);
            if (propertyNullCheck != null)
            {
                for (Node node : nodes)
                {
                    if (propertyNullCheck)
                    {
                        assertNull(node.getAspectNames());
                        assertNull(node.getProperties());
                        assertNull(node.getPath());
                        assertNull(node.getIsLink());
                    }
                    else
                    {
                        assertNotNull(node.getAspectNames());
                        assertNotNull(node.getProperties());
                        assertNotNull(node.getPath());
                        assertNotNull(node.getIsLink());
                    }
                }
            }
        }
        return nodes;
    }

    private NodeRef getNodeRef(String id)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        // The following call to new NodeRef(...) returns a NodeRef like:
        //    workspace://SpacesStore/9db76769-96de-4de4-bdb4-a127130af362
        // We call tenantService.getName(nodeRef) to get a fully qualified NodeRef as Solr returns this.
        // They look like:
        //    workspace://@org.alfresco.rest.api.tests.queriespeopleapitest@SpacesStore/9db76769-96de-4de4-bdb4-a127130af362
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
        nodeRef = tenantService.getName(nodeRef);
        return nodeRef;
    }

    /**
     * Tests basic api for nodes live search - metadata (name, title, description) &/or full text search of file/content
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/nodes}
     */
    @Test
    public void testLiveSearchNodes_FTS_and_Metadata() throws Exception
    {
        setRequestContext(user1);
        
        int f1Count = 5;
        List<String> f1NodeIds = new ArrayList<>(f1Count);

        int f2Count = 3;
        List<String> f2NodeIds = new ArrayList<>(f2Count);

        int f3Count = 4;
        List<String> f3NodeIds = new ArrayList<>(f3Count);

        int totalCount = f1Count + f2Count + f3Count;
        List<String> allIds = new ArrayList<>(totalCount);

        String testTerm = "abc123basic";

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            Map<String, String> params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);

            // We can no longer check the assumption that there is a clean repo (ie. no nodes with search term 'abc123')
            // in the same way as before which used search - Generally ok not to check.

            String myFolderNodeId = getMyNodeId();

            String f1Id = createFolder(myFolderNodeId, "folder 1").getId();
            String f2Id = createFolder(myFolderNodeId, "folder 2").getId();

            String name = "name";
            String title = "title";
            String descrip = "descrip";

            String folderNameSuffix = " "+testTerm+" folder";
            String txtSuffix = ".txt";

            Map<String,String> idNameMap = new HashMap<>();
            Map<String,List<String>> textIdMap = new HashMap<>();

            int nameIdx = f1Count;
            for (int i = 1; i <= f1Count; i++)
            {
                // create doc - in folder 1
                String contentText = "f1 " + testTerm + " test document " + user1 + " document " + i;

                String num = String.format("%05d", nameIdx);
                String docName = name+num+name+txtSuffix;

                Map<String,String> docProps = new HashMap<>(2);
                docProps.put("cm:title", title + num + title);
                docProps.put("cm:description", descrip+num+descrip);

                Document doc = createTextFile(f1Id, docName, contentText, "UTF-8", docProps);

                f1NodeIds.add(doc.getId());
                idNameMap.put(doc.getId(), docName);
                addTo(textIdMap, name+num+name, doc.getId());
                addTo(textIdMap, docName, doc.getId());
                addTo(textIdMap, docProps.get("cm:title"), doc.getId());
                addTo(textIdMap, docProps.get("cm:description"), doc.getId());

                nameIdx--;
            }

            nameIdx = f2Count;
            for (int i = 1; i <= f2Count; i++)
            {
                // create doc - in folder 2
                String contentText = "f2 " + testTerm + " test document";

                String num = String.format("%05d", nameIdx);
                String docName = name+num+name+txtSuffix;

                Map<String, String> props = new HashMap<>(2);
                props.put("cm:title", title+num+title);
                props.put("cm:description", descrip+num+descrip);

                Document doc = createTextFile(f2Id, docName, contentText, "UTF-8", props);

                f2NodeIds.add(doc.getId());
                idNameMap.put(doc.getId(), docName);

                addTo(textIdMap, name+num+name, doc.getId());
                addTo(textIdMap, docName, doc.getId());
                addTo(textIdMap, props.get("cm:title"), doc.getId());
                addTo(textIdMap, props.get("cm:description"), doc.getId());

                nameIdx--;
            }

            nameIdx = f3Count;
            for (int i = 1; i <= f3Count; i++)
            {
                // create folders - in folder 3
                String num = String.format("%05d", nameIdx);
                String folderName = name+num+name+folderNameSuffix;

                Map<String, Object> props = new HashMap<>(2);
                props.put("cm:title", title+num+title);
                props.put("cm:description", descrip+num+descrip);

                Node node = createFolder(myFolderNodeId, folderName, props);

                f3NodeIds.add(node.getId());
                idNameMap.put(node.getId(), folderName);

                addTo(textIdMap, name+num+name, node.getId());
                addTo(textIdMap, folderName, node.getId());
                addTo(textIdMap, (String)props.get("cm:title"), node.getId());
                addTo(textIdMap, (String)props.get("cm:description"), node.getId());

                nameIdx--;
            }

            allIds.addAll(idNameMap.keySet());

            //
            // find nodes
            //

            // Search hits based on FTS (content) and also name (in case of folder nodes)
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, null, paging, 200, null, true, allIds);

            // Search - include optional fields - eg. aspectNames, properties, path, isLink
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, "aspectNames,properties,path,isLink", null, paging, 200, null, false, allIds);

            // Search hits restricted by node type
            checkApiCall(null, NODE_TYPE_QUERY, testTerm, "cm:folder", null, null, null, paging, 200, null, null, f3NodeIds);

            // Search - with -root- as the root node (for path-based / in-tree search)
            checkApiCall("/app:company_home", ROOT_NODE_QUERY_SUFFIX, testTerm, null, Nodes.PATH_ROOT, null, null, paging, 200, null, null, allIds);

            // Search - with -shared- as the root node (for path-based / in-tree search)
            checkApiCall("/app:company_home/app:shared", ROOT_NODE_QUERY_SUFFIX, testTerm, null, Nodes.PATH_SHARED, null, null, paging, 200, null, null, Collections.EMPTY_LIST);

            // Search - with folder 1 as root node (for path-based / in-tree search)
            checkApiCall("/app:company_home/app:user_homes/cm:user1-[0-9]*_x...._org.alfresco.rest.api.tests.queriesnodesapitest/cm:folder_x...._1",
                ROOT_NODE_QUERY_SUFFIX, testTerm, null, f1Id, null, null, paging, 200, null, null, f1NodeIds);

            // Search - with folder 2 as the root node (for path-based / in-tree search)
            checkApiCall("/app:company_home/app:user_homes/cm:user1-[0-9]*_x...._org.alfresco.rest.api.tests.queriesnodesapitest/cm:folder_x...._2",
                ROOT_NODE_QUERY_SUFFIX, testTerm, null, f2Id, null, null, paging, 200, null, null, f2NodeIds);

            // Search - with -my- as the root node (for path-based / in-tree search)
            checkApiCall("/app:company_home/app:user_homes/cm:user1-[0-9]*_x...._org.alfresco.rest.api.tests.queriesnodesapitest",
                ROOT_NODE_QUERY_SUFFIX, name+"*", null, Nodes.PATH_MY, null, null, paging, 200, null, null, allIds);

            // Search hits based on cm:name
            String term = name+String.format("%05d", 1)+name;
            List<String> ids = textIdMap.get(term);
            assertEquals(term, 3, ids.size());
            List<Node> nodes = checkApiCall(null, DEAFULT_QUERY, "\""+term+"\"", null, null, null, null, paging, 200, null, null, ids);
            for (Node node : nodes)
            {
                if (node.getIsFolder())
                {
                    assertEquals(term+folderNameSuffix, node.getName());
                }
                else
                {
                    assertEquals(term+txtSuffix, node.getName());
                }
            }

            // search for name with . (eg. ".txt") without double quotes
            term = name+String.format("%05d", 1)+name+txtSuffix;
            ids = textIdMap.get(term);
            assertEquals(term, 2, ids.size());
            checkApiCall(null, DEAFULT_QUERY, term, null, null, null, null, paging, 200, null, null, ids);

            // search for name with . (eg. ".txt") with double quotes
            term = name+String.format("%05d", 1)+name+txtSuffix;
            ids = textIdMap.get(term);
            assertEquals(term, 2, ids.size());
            checkApiCall(null, DEAFULT_QUERY, "\""+term+"\"", null, null, null, null, paging, 200, null, null, ids);

            // Search hits based on cm:title
            term = title+String.format("%05d", 2)+title;
            ids = textIdMap.get(term);
            assertEquals(term, 3, ids.size());
            nodes = checkApiCall(null, DEAFULT_QUERY, "\""+term+"\"", null, null, "properties", null, paging, 200, null, null, ids);
            assertEquals(term, nodes.get(0).getProperties().get("cm:title"));
            assertEquals(term, nodes.get(1).getProperties().get("cm:title"));
            assertEquals(term, nodes.get(2).getProperties().get("cm:title"));

            // Search hits based on cm:description
            term = descrip+String.format("%05d", 3)+descrip;
            ids = textIdMap.get(term);
            assertEquals(term, 3, ids.size());
            nodes = checkApiCall(null, DEAFULT_QUERY, "\""+term+"\"", null, null, "properties", null, paging, 200, null, null, ids);
            assertEquals(term, nodes.get(0).getProperties().get("cm:description"));
            assertEquals(term, nodes.get(1).getProperties().get("cm:description"));
            assertEquals(term, nodes.get(2).getProperties().get("cm:description"));

            // -ve test - no params (ie. no term)
            getAll(URL_QUERIES_LSN, paging, null, 400);

            // -ve test - no term
            params = new HashMap<>(1);
            params.put(Queries.PARAM_ROOT_NODE_ID, f1Id);
            getAll(URL_QUERIES_LSN, paging, params, 400);

            // -ve test - unknown root node id
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, "abc");
            params.put(Queries.PARAM_ROOT_NODE_ID, "dummy");
            getAll(URL_QUERIES_LSN, paging, params, 404);

            // -ve test - unknown node type
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, "abc");
            params.put(Queries.PARAM_NODE_TYPE, "cm:dummy");
            getAll(URL_QUERIES_LSN, paging, params, 400);

            // -ve test - term too short
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "ab");
            getAll(URL_QUERIES_LSN, paging, params, 400);

            // -ve test - term is still too short
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "  \"a b *\"  ");
            getAll(URL_QUERIES_LSN, paging, params, 400);

            // -ve test - unauthenticated - belts-and-braces ;-)
            setRequestContext(null);
            getAll(URL_QUERIES_LSN, paging, params, 401);
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            for (String docId : allIds)
            {
                deleteNode(docId, true, 204);
            }
        }
    }

    private void addTo(Map<String, List<String>> textIdMap, String text, String id)
    {
        List<String> ids = textIdMap.get(text);
        if (ids == null)
        {
            ids = new ArrayList<>();
            textIdMap.put(text, ids);
        }
        ids.add(id);
    }

    @Test
    public void testLiveSearchNodes_SortPage() throws Exception
    {
        setRequestContext(user1);
        
        int f1Count = 5;
        List<String> f1NodeIds = new ArrayList<>(f1Count);

        int f2Count = 3;
        List<String> f2NodeIds = new ArrayList<>(f2Count);

        int totalCount = f1Count + f2Count;
        List<String> allIds = new ArrayList<>(totalCount);

        String testTerm = "def456sortpage";

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            Map<String, String> params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);

            String myFolderNodeId = getMyNodeId();

            String f1Id = createFolder(myFolderNodeId, "folder sort 1").getId();
            String f2Id = createFolder(myFolderNodeId, "folder sort 2").getId();

            String name = "name";

            Map<String,String> idNameMap = new HashMap<>();

            int nameIdx = f1Count;
            for (int i = 1; i <= f1Count; i++)
            {
                // create doc - in folder 1
                String contentText = "f1 " + testTerm + " test document " + user1 + " document " + i;

                String num = String.format("%05d", nameIdx);
                String docName = name+num+name+".txt";

                Document doc = createTextFile(f1Id, docName, contentText, "UTF-8", null);

                f1NodeIds.add(doc.getId());
                idNameMap.put(doc.getId(), docName);

                nameIdx--;
            }

            nameIdx = f2Count;
            for (int i = 1; i <= f2Count; i++)
            {
                // create doc - in folder 2
                String contentText = "f2 " + testTerm + " test document";

                String num = String.format("%05d", nameIdx);
                String docName = name+num+name+".txt";

                Document doc = createTextFile(f2Id, docName, contentText, "UTF-8", null);

                f2NodeIds.add(doc.getId());
                idNameMap.put(doc.getId(), docName);

                nameIdx--;
            }

            List<String> idsSortedByNameAsc = new ArrayList<>(sortByValue(idNameMap).keySet());

            List<String> idsSortedByNameDescCreatedAtAsc = new ArrayList<>(totalCount);
            for (int i = 0; i < totalCount; i++)
            {
                if (i < f1Count)
                {
                    idsSortedByNameDescCreatedAtAsc.add(f1NodeIds.get(i));
                }
                if (i < f2Count)
                {
                    idsSortedByNameDescCreatedAtAsc.add(f2NodeIds.get(i));
                }
            }

            allIds.addAll(idNameMap.keySet());

            // test sort order

            // default sort order (modifiedAt desc)
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, null, paging, 200, false, true, allIds);

            // sort order - modifiedAt asc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "modifiedAt asc", paging, 200, false, true, allIds);

            // sort order - modifiedAt desc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "modifiedAt desc", paging, 200, true, true, allIds);

            // sort order - createdAt asc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "createdAt asc", paging, 200, true, true, allIds);

            // sort order - createdAt desc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "createdAt desc", paging, 200, false, true, allIds);

            // sort order - name asc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "name asc", paging, 200, true, true, idsSortedByNameAsc);

            // sort order - name desc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "name desc", paging, 200, false, true, idsSortedByNameAsc);

            // sort order - name desc, createdAt asc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "name desc, createdAt asc", paging, 200, false, true, idsSortedByNameDescCreatedAtAsc);

            // sort order - name asc, createdAt asc
            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, "name asc, createdAt desc", paging, 200, true, true, idsSortedByNameDescCreatedAtAsc);

            // basic paging test

            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, null, getPaging(0, 100), 200, false, true, allIds);

            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, null, getPaging(0, f1Count), 200, false, true, f1NodeIds);

            checkApiCall(null, DEAFULT_QUERY, testTerm, null, null, null, null, getPaging(f1Count, f2Count), 200, false, true, f2NodeIds);


            // TODO sanity check modifiedAt (for now modifiedAt=createdAt)

            // -ve test - invalid sort field
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put(Queries.PARAM_ORDERBY, "invalid asc");
            getAll(URL_QUERIES_LSN, paging, params, 400);

            // -ve test - unauthenticated - belts-and-braces ;-)
            setRequestContext(null);
            getAll(URL_QUERIES_LSN, paging, params, 401);
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            for (String docId : allIds)
            {
                deleteNode(docId, true, 204);
            }
        }
    }

    @Test
    @Category(RedundantTests.class)
    public void testLiveSearchNodes_Tags() throws Exception
    {
        setRequestContext(user1);
        
        PublicApiClient.Nodes nodesProxy = publicApiClient.nodes();

        int f1Count = 5;
        List<String> f1NodeIds = new ArrayList<>(f1Count);

        int f2Count = 3;
        List<String> f2NodeIds = new ArrayList<>(f2Count);

        int totalCount = f1Count + f2Count;
        List<String> allIds = new ArrayList<>(totalCount);

        String testTag = "ghi789tag";
        String testFileTag = "ghi789file";
        String testFolderTag = "ghi789folder";

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            String f1Id = createFolder(Nodes.PATH_MY, "folder tag 1").getId();
            String f2Id = createFolder(Nodes.PATH_MY, "folder tag 2").getId();

            String name = "name";

            for (int i = 1; i <= f1Count; i++)
            {
                // create doc - in folder 1
                String contentText = "f1 test document " + user1 + " document " + i;
                String docName = name+i;

                Document doc = createTextFile(f1Id, docName, contentText, "UTF-8", null);

                publicApiClient.setRequestContext(new RequestContext("", user1));

                nodesProxy.createNodeTag(doc.getId(), new Tag(testTag)); // ignore result
                nodesProxy.createNodeTag(doc.getId(), new Tag(testFileTag)); // ignore result

                f1NodeIds.add(doc.getId());
            }

            for (int i = 1; i <= f2Count; i++)
            {
                // create folder - in folder 2
                String folderName = name+i;

                Folder folder = createFolder(f2Id, folderName, null);

                publicApiClient.setRequestContext(new RequestContext("", user1));

                nodesProxy.createNodeTag(folder.getId(), new Tag(testTag)); // ignore result
                nodesProxy.createNodeTag(folder.getId(), new Tag(testFolderTag)); // ignore result

                f2NodeIds.add(folder.getId());
            }

            allIds.addAll(f1NodeIds);
            allIds.addAll(f2NodeIds);

            // Search hits based on tag

            Map<String, String> params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTag);
            HttpResponse response = getAll(URL_QUERIES_LSN, paging, params, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, null);

            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testFileTag);
            response = getAll(URL_QUERIES_LSN, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, f1NodeIds, null);

            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testFolderTag);
            response = getAll(URL_QUERIES_LSN, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, f2NodeIds, null);
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            for (String nodeId : allIds)
            {
                deleteNode(nodeId, true, 204);
            }
        }
    }

    private void checkNodeIds(List<Node> nodes, List<String> nodeIds, Boolean asc)
    {
        assertEquals(nodeIds.size(), nodes.size());

        if (asc == null)
        {
            // ignore order
            for (Node node : nodes)
            {
                assertTrue(nodeIds.contains(node.getId()));
            }
        }
        else if (asc)
        {
            int i = 0;
            for (Node node : nodes)
            {
                nodeIds.get(i).equals(node.getId());
                i++;
            }
        }
        else
        {
            int i = nodeIds.size() - 1;
            for (Node node : nodes)
            {
                nodeIds.get(i).equals(node.getId());
                i--;
            }
        }

    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
