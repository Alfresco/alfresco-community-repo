/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.parsers;

import junit.framework.TestCase;
import org.alfresco.repo.search.impl.querymodel.*;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneDisjunction;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneFunctionalConstraint;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
import org.alfresco.service.namespace.NamespaceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.repo.search.impl.parsers.FTSQueryParser.RerankPhase.QUERY_PHASE;

public class FTSQueryParserTest extends TestCase
{

    private AlfrescoFunctionEvaluationContext functionContext;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        functionContext = new AlfrescoFunctionEvaluationContext(null, null, NamespaceService.CONTENT_MODEL_1_0_URI);
    }

    public void testQueryBetweenQuotationMarks()
    {
        String query = "\"peace and love\"";
        LuceneFunctionalConstraint functionalConstraint = (LuceneFunctionalConstraint) parseQuery(query, functionContext);
        
        assertTrue((Boolean) functionalConstraint.getFunctionArguments().get(FTSPhrase.ARG_IS_PHRASE)
                                     .getValue(functionContext));
        assertEquals("peace and love",
                functionalConstraint.getFunctionArguments().get(FTSPhrase.ARG_PHRASE).getValue(functionContext));
    }

    public void testQueryUsingAndOperator()
    {
        String query = "peace and love";
        LuceneFunctionalConstraint functionalConstraint = (LuceneFunctionalConstraint)parseQuery(query, functionContext);
        
        assertFalse((Boolean) functionalConstraint.getFunctionArguments().get(FTSPhrase.ARG_IS_PHRASE)
                                      .getValue(functionContext));
        assertEquals("peace love",
                functionalConstraint.getFunctionArguments().get(FTSPhrase.ARG_PHRASE).getValue(functionContext));
    }

    public void testQueryWithoutQuotationMarks()
    {
        String query = "peace love";
        LuceneDisjunction luceneDisjunction = (LuceneDisjunction) parseQuery(query, functionContext);
        
        List<Constraint> constraints = luceneDisjunction.getConstraints();
        assertEquals(2, constraints.size());
        LuceneFunctionalConstraint functionalConstraint0 = (LuceneFunctionalConstraint)constraints.get(0);
        LuceneFunctionalConstraint functionalConstraint1 = (LuceneFunctionalConstraint)constraints.get(1);
        assertEquals("peace", functionalConstraint0.getFunctionArguments().get(FTSTerm.ARG_TERM).getValue(functionContext));
        assertEquals("love", functionalConstraint1.getFunctionArguments().get(FTSTerm.ARG_TERM).getValue(functionContext));
    }

    private Constraint parseQuery(String query, AlfrescoFunctionEvaluationContext functionContext)
    {
        QueryModelFactory factory = new LuceneQueryModelFactory();
        FTSParser.Mode mode = FTSParser.Mode.DEFAULT_DISJUNCTION;
        Connective defaultFieldConnective = Connective.OR;
        Map<String, String> queryTemplates = new HashMap<>();
        String defaultField = "TEXT";
        Constraint constraint = FTSQueryParser.buildFTS(query, factory, functionContext, null, null, mode,
                defaultFieldConnective, queryTemplates, defaultField, QUERY_PHASE);
        return constraint;
    }

}
