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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WherePredicateApiTest extends AbstractSingleNetworkSiteTest
{
    private static final boolean NOT = true;
    private String folder0Id;
    private String file0Id;
    private Paging paging;

    @Before
    public void setup() throws Exception
    {
        super.setup();
        
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();

        String folder0Name = "folder " + RUNID;
        folder0Id = createFolder(myNodeId, folder0Name, null).getId();
        
        String file0Name = "file " + RUNID;
        file0Id = createEmptyTextFile(folder0Id, file0Name).getId();

        paging = getPaging(0, 100);
    }
    
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    /**
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<node>/children}
     */
    @Test
    public void testLogicalOperatorsGetChildren() throws Exception
    {
        String childrenUrl = getNodeChildrenUrl(folder0Id);
        // SIMPLE (+ve)
        String clause = new WhereClauseBuilder()
                                .predicate(Nodes.PARAM_ISFOLDER + "=true")
                                .build();
        getAll(childrenUrl, paging, getWhereClause(clause), 200);
        //AND (+ve)
        clause = new WhereClauseBuilder()
                        .predicate(Nodes.PARAM_ISFOLDER + "=true")
                        .and(Nodes.PARAM_ISFILE + "=false")
                        .build();
        getAll(childrenUrl, paging, getWhereClause(clause), 200);
        //NOT (-ve)
        clause = new WhereClauseBuilder()
                   .predicate(Nodes.PARAM_ISFOLDER + "=true", NOT)
                   .build();
        getAll(childrenUrl, paging, getWhereClause(clause), 400);
        // OR (-ve)
        clause = new WhereClauseBuilder()
                         .predicate(Nodes.PARAM_ISFOLDER + "=true")
                         .or(Nodes.PARAM_ISFILE + "=false")
                         .build();
        getAll(childrenUrl, paging, getWhereClause(clause), 400);
        // NOT + AND (-ve)
        clause = new WhereClauseBuilder()
                .predicate(Nodes.PARAM_ISFOLDER + "=true", NOT)
                .and(Nodes.PARAM_ISFILE + "=false")
                .build();
        getAll(childrenUrl, paging, getWhereClause(clause), 400);
    }
    
    /**
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<node>/secondary-children}
     */
    @Test
    public void testLogicalOperatorsGetSecondaryChildren() throws Exception
    {
        // SIMPLE (+ve)
        String clause = new WhereClauseBuilder()
                .predicate(Nodes.PARAM_ASSOC_TYPE + "=cm:contains")
                .build();
        getAll(getNodeSecondaryChildrenUrl(folder0Id), paging, getWhereClause(clause), 200);
        // NOT (-ve)
        clause = new WhereClauseBuilder()
                .predicate(Nodes.PARAM_ASSOC_TYPE + "=cm:contains", NOT)
                .build();
        getAll(getNodeSecondaryChildrenUrl(folder0Id), paging, getWhereClause(clause), 400);
    }

    /**
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<node>/renditions}
     */
    @Test
    public void testLogicalOperatorsGetRenditions() throws Exception
    {
        // SIMPLE (+ve)
        String clause = new WhereClauseBuilder()
                .predicate(Renditions.PARAM_STATUS + "=CREATED")
                .build();
        getAll(getNodeRenditionsUrl(file0Id), paging, getWhereClause(clause), 200);
        // NOT (-ve)
        clause = new WhereClauseBuilder()
                .predicate(Renditions.PARAM_STATUS + "=CREATED", NOT)
                .build();
        getAll(getNodeRenditionsUrl(file0Id), paging, getWhereClause(clause), 400);
    }
    
    /**
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/shared-links}
     */
    @Test
    public void testLogicalOperatorsGetShareLinks() throws Exception
    {
        // SIMPLE (+ve)
        String clause = new WhereClauseBuilder()
                .predicate(QuickShareLinks.PARAM_SHAREDBY + "='-me-'")
                .build();
        getAll("shared-links", paging, getWhereClause(clause), 200);
        // NOT (-ve)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
        clause = new WhereClauseBuilder()
                .predicate(QuickShareLinks.PARAM_SHAREDBY + "='-me-'", NOT)
                .build();
        getAll("shared-links", paging, getWhereClause(clause), 400);
    }
    
    /**
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/people/<personId>/favorites}
     */
    @Test
    public void testLogicalOperatorsGetFavorites() throws Exception
    {
        String favoritesUrl = getFavoritesUrl("-me-");
        // SIMPLE (+ve)
        String clause = new WhereClauseBuilder()
                                .predicate("EXISTS(target/file)")
                                .build();
        getAll(favoritesUrl, paging, getWhereClause(clause), 200);
        // OR (+ve)
        clause = new WhereClauseBuilder()
                .predicate("EXISTS(target/file)").or("EXISTS(target/folder)")
                .build();
        getAll(favoritesUrl, paging, getWhereClause(clause), 200);
        // AND (-ve)
        clause = new WhereClauseBuilder()
                .predicate("EXISTS(target/file)").and("EXISTS(target/folder)")
                .build();
        getAll(favoritesUrl, paging, getWhereClause(clause), 400);
        
        // NOT (-ve): uncomment when REPO-1249 is done
        /*clause = new WhereClauseBuilder()
                .predicate("EXISTS(target/file)", NOT)
                .build();
        getAll(favoritesUrl, paging, getWhereClause(clause), 400);*/
    }
    
    private Map<String, String> getWhereClause(String whereparams)
    {
        Map<String, String> params = new HashMap<>();
        params.put("where", whereparams);
        
        return params;
    }
    
    private class WhereClauseBuilder
    {
        private WhereClause whereClause;

        public WhereClauseBuilder predicate(String predicate, boolean negated)
        {
            whereClause = new WhereClause(predicate, negated);
            return this;
        }
        public WhereClauseBuilder predicate(String predicate)
        {
            return this.predicate(predicate, false);
        }

        public WhereClauseBuilder and(String predicate, boolean negated)
        {
            whereClause.and(new WhereClause(predicate, negated));
            return this;
        }
        public WhereClauseBuilder and(String predicate)
        {
            return and(predicate, false);
        }

        public WhereClauseBuilder or(String predicate, boolean negated)
        {
            whereClause.or(new WhereClause(predicate, negated));
            return this;
        }
        public WhereClauseBuilder or(String predicate)
        {
            return or(predicate, false);
        }

        public WhereClauseBuilder not()
        {
            whereClause.negate();
            return this;
        }

        public String build()
        {
            whereClause.group();
            return whereClause.toString();
        }

        private class WhereClause
        {
            private final String[] operators = new String[] { "MATCHES", "IN", "BETWEEN" };

            private String clause;

            public WhereClause(String clause, boolean negated)
            {
                this.clause = clause;
                if (negated)
                {
                    negate();
                }
            }

            private void group()
            {
                clause = "(" + clause + ")";
            }

            private void and(WhereClause otherClause)
            {
                this.clause += " AND " + otherClause;
            }

            private void or(WhereClause otherClause)
            {
                this.clause += " OR " + otherClause;
            }

            public void negate()
            {
                for (String op : operators)
                {
                    if (clause.contains(op))
                    {
                        clause.replace(op, "NOT " + op);
                        return;
                    }
                }
                clause = "NOT " + clause;
            }

            @Override
            public String toString()
            {
                return clause;
            }
        }
    }
    
    private String getNodeSecondaryChildrenUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + "secondary-children";
    }
    
    private String getFavoritesUrl(String nodeId)
    {
        return "people" + "/" + nodeId + "/" + "favorites";
    }
}
