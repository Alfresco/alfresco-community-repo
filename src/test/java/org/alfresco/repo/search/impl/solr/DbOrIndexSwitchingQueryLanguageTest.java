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
package org.alfresco.repo.search.impl.solr;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.testing.category.LuceneTests;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(LuceneTests.class)
public class DbOrIndexSwitchingQueryLanguageTest
{
    private DbOrIndexSwitchingQueryLanguage queryLang;
    private SearchParameters searchParameters;
    private @Mock LuceneQueryLanguageSPI dbQueryLang;
    private @Mock LuceneQueryLanguageSPI indexQueryLang;
    private @Mock SolrJSONResultSet indexResults;
    private @Mock ResultSet dbResults;
    private @Mock SOLRDAO solrDAO;
    private List<Node> changedNodes;
    
    @Before
    public void setUp() throws Exception
    {
        queryLang = new DbOrIndexSwitchingQueryLanguage();
        queryLang.setDbQueryLanguage(dbQueryLang);
        queryLang.setIndexQueryLanguage(indexQueryLang);
        queryLang.setSolrDao(solrDAO);
        searchParameters = new SearchParameters();
        changedNodes = new ArrayList<>();
        
        // By default, tests will have hybrid enabled.
        queryLang.setHybridEnabled(true);
    }

    @Test
    public void hybridSearch()
    {
        when(indexQueryLang.executeQuery(argThat(isSearchParamsSinceTxId(null)))).thenReturn(indexResults);
        when(indexResults.getLastIndexedTxId()).thenReturn(80L);
        when(dbQueryLang.executeQuery(argThat(isSearchParamsSinceTxId(80L)))).thenReturn(dbResults);
        when(solrDAO.getNodes(argThat(isNodeParamsFromTxnId(81L)), eq(null), eq(null))).thenReturn(changedNodes);
        
        searchParameters.setQueryConsistency(QueryConsistency.HYBRID);
        
        // These results will come back from the SOLR query.
        List<ChildAssociationRef> indexRefs = new ArrayList<>();
        indexRefs.add(childAssoc("Car1"));
        indexRefs.add(childAssoc("Car2"));
        indexRefs.add(childAssoc("Car3"));
        indexRefs.add(childAssoc("Car4"));
        when(indexResults.getChildAssocRefs()).thenReturn(indexRefs);
        
        // These results will come back from the DB query.
        List<ChildAssociationRef> dbRefs = new ArrayList<>();
        dbRefs.add(childAssoc("Car1")); // Updated node, so also in index
        dbRefs.add(childAssoc("Car5"));
        dbRefs.add(childAssoc("Car6"));
        when(dbResults.getChildAssocRefs()).thenReturn(dbRefs);
        
        // Nodes that have changed since last SOLR index.
        // includes nodes that will come back from the DB query, plus deleted nodes.
        changedNodes.add(node("Car1"));
        changedNodes.add(node("Car5"));
        changedNodes.add(node("Car6"));
        changedNodes.add(node("Car4")); // Deleted node - not in the DB query results.
        
        // Execute the hybrid query.
        ResultSet results = queryLang.executeQuery(searchParameters);
        
        // Check that the results have come back and that the are merged/de-duped.
        assertEquals(5, results.length());
        
        // NOTE: No assertion of ordering is currently present.
        // TODO: ordering?
        assertTrue(results.getChildAssocRefs().contains(childAssoc("Car1")));
        assertTrue(results.getChildAssocRefs().contains(childAssoc("Car2")));
        assertTrue(results.getChildAssocRefs().contains(childAssoc("Car3")));
        assertTrue(results.getChildAssocRefs().contains(childAssoc("Car5")));
        assertTrue(results.getChildAssocRefs().contains(childAssoc("Car6")));
    }
    
    @Test(expected=QueryModelException.class)
    public void hybridSearchWhenNoQueryLanguageAvailable()
    {
        searchParameters.setQueryConsistency(QueryConsistency.HYBRID);
        queryLang.setIndexQueryLanguage(null);
        queryLang.setDbQueryLanguage(null);
        
        queryLang.executeQuery(searchParameters);
    }
    
    @Test(expected=QueryModelException.class)
    public void hybridSearchWhenNoDBLanguageAvailable()
    {
        searchParameters.setQueryConsistency(QueryConsistency.HYBRID);
        queryLang.setDbQueryLanguage(null);
        
        queryLang.executeQuery(searchParameters);
    }
    
    @Test(expected=QueryModelException.class)
    public void hybridSearchWhenNoIndexLanguageAvailable()
    {
        searchParameters.setQueryConsistency(QueryConsistency.HYBRID);
        queryLang.setIndexQueryLanguage(null);
        
        queryLang.executeQuery(searchParameters);
    }

    @Test(expected=DisabledFeatureException.class)
    public void canDisableHybridSearch()
    {
        queryLang.setHybridEnabled(false);
        searchParameters.setQueryConsistency(QueryConsistency.HYBRID);
        queryLang.executeQuery(searchParameters);
    }


    @Test
    public void findAfts() throws Exception
    {
        java.util.regex.Matcher matcher = LuceneQueryLanguageSPI.AFTS_QUERY.matcher("{!afts tag=desc1,desc2}description:xyz");
        assertTrue(matcher.find());
        assertEquals("description:xyz", matcher.group(2));

        matcher = LuceneQueryLanguageSPI.AFTS_QUERY.matcher("{!afts tag=desc1}{http://www.alfresco.org/model/content/1.0}title:workflow");
        assertTrue(matcher.find());
        assertEquals("{http://www.alfresco.org/model/content/1.0}title:workflow", matcher.group(2));

        matcher = LuceneQueryLanguageSPI.AFTS_QUERY.matcher("{http://www.alfresco.org/model/content/1.0}title:workflow");
        assertFalse(matcher.find());

        matcher = LuceneQueryLanguageSPI.AFTS_QUERY.matcher("description:xyz");
        assertFalse(matcher.find());
    }

    /**
     * Custom matcher for SearchParameters having a particular value
     * for the property sinceTxId.
     * 
     * @param sinceTxId The value to match, may be null.
     * @return Matcher capable of checking for SearchParameters with the specified TX ID parameter.
     */
    private Matcher<SearchParameters> isSearchParamsSinceTxId(final Long sinceTxId)
    {
        return new BaseMatcher<SearchParameters>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText(SearchParameters.class.getSimpleName()+"[sinceTxId="+sinceTxId+"]");
            }

            @Override
            public boolean matches(Object item)
            {
                if (!(item instanceof SearchParameters))
                {
                    return false;
                }
                SearchParameters sp = (SearchParameters) item;
                if (sinceTxId == null)
                {
                    return sp.getSinceTxId() == null;
                }
                else
                {
                    return sinceTxId.equals(sp.getSinceTxId());
                }
            }
        };
    }
    
    private Matcher<NodeParameters> isNodeParamsFromTxnId(final Long fromTxnId)
    {
        return new BaseMatcher<NodeParameters>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText(NodeParameters.class.getSimpleName()+"[fromTxId="+fromTxnId+"]");
            }
            
            @Override
            public boolean matches(Object item)
            {
                if (!(item instanceof NodeParameters))
                {
                    return false;
                }
                NodeParameters np = (NodeParameters) item;
                if (fromTxnId == null)
                {
                    return np.getFromTxnId() == null;
                }
                else
                {
                    return fromTxnId.equals(np.getFromTxnId());
                }
            }
        };
    }

    private ChildAssociationRef childAssoc(String id)
    {
        return new ChildAssociationRef(
            ContentModel.ASSOC_CONTAINS,
            new NodeRef("test://store/parentRef"),
            ContentModel.TYPE_CONTENT,
            new NodeRef("test://store/" + id)
        );
    }

    private Node node(String id)
    {
        return new TestNode(id);
    }
}
