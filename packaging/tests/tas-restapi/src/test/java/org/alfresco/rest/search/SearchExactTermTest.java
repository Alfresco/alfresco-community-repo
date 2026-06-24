/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.rest.search;

import org.testng.annotations.Test;

/**
 * Tests Exact Term queries against standard properties (cm:name, cm:title, cm:description, cm:content). These tests are engine-agnostic and do not require Cross Locale or Solr-specific tokenisation configuration.
 */
public class SearchExactTermTest extends AbstractSearchExactTermTest
{

    /**
     * Note these tests are searching in cm:name property only (default for exact term without cross locale). 1 result expected for =run >> Document #2 >> name: "Run" 1 result expected for =jump >> Document #4 >> name: "Jump"
     */
    @Test
    public void exactSearch_singleTerm_shouldReturnResultsContainingExactTermInName()
    {
        /* 1 result is expected: - Document #2 >> name: "Run" */
        assertResponseCardinality("=run", 1);

        /* No result for runner in cm:name property, one record has runners in "description" property. You can see the difference between exact search and not */
        assertResponseCardinality("=runner", 0);
        assertResponseCardinality("runner", 1);

        /* 1 result is expected: - Document #4 >> name: "Jump" */
        assertResponseCardinality("=jump", 1);
    }

    @Test
    public void exactSearch_singleTermConjunction_shouldReturnFullFieldValueMatch() throws Exception
    {

        /**
         * Since REST API is getting the results from DB or Search Services, using single term expressions is always
         * retrieved from DB when using default configuration "solr.query.fts.queryConsistency=TRANSACTIONAL_IF_POSSIBLE".
         * Combining this single term with range queries (like cm:created) will ensure the results
         * are coming from SOLR in this mode.
         */

        /*
         * 1 result is expected for non-tokenised field (tok:false)
         * - Document #4 >> title: "Running"
         */
        assertResponseCardinality("=tok:false:Running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        /*
         * 0 results are expected: there is no result that have exactly cm:title:"Run"
         * The closest we have is record Run (tok:false:"Run : a philosophy")
         * As you can see we don't have a full match, so it's not in the results.
         *
         */
        assertResponseCardinality("=tok:false:Run AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 0);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved:
     * - https://alfresco.atlassian.net/browse/SEARCH-2461
     * - https://alfresco.atlassian.net/browse/SEARCH-2953
     */
    @Test(enabled=false)
    public void failing_exactSearch_singleTermConjunction_shouldReturnFullFieldValueMatch() throws Exception
    {

        // SEARCH-2953
        assertResponseCardinality("=tok:false:running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:running", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:Running", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:Run", 0);

    }


    @Test
    public void exactSearch_singleTermConjunction_shouldReturnException() throws Exception
    {

        /**
         * Since REST API is getting the results from DB or Search Services, using single term expressions is always
         * retrieved from DB when using default configuration "solr.query.fts.queryConsistency=TRANSACTIONAL_IF_POSSIBLE".
         * Combining this single term with range queries (like cm:created) will ensure the results
         * are coming from SOLR in this mode.
         */

        /**
         * Unsupported Exception is expected when using exact term search with tokenised properties
         */
        assertException("=tok:true:running AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
        assertException("=tok:both:running AND cm:created:['" + fromDate + "' TO '" + toDate + "']");

        /**
         * Unsupported Exception is expected when using exact term search with tokenised properties
         */
        assertException("=tok:true:Running AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
        assertException("=tok:both:Running AND cm:created:['" + fromDate + "' TO '" + toDate + "']");

        /**
         * Unsupported Exception is expected when using exact term search with tokenised properties
         */
        assertException("=tok:true:Run AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
        assertException("=tok:both:Run AND cm:created:['" + fromDate + "' TO '" + toDate + "']");

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved:
     * - https://alfresco.atlassian.net/browse/SEARCH-2461
     */
    @Test(enabled=false)
    public void failing_exactSearch_singleTermConjunction_shouldReturnException() throws Exception
    {

        // SEARCH-2461
        assertException("=tok:true:running");
        assertException("=tok:both:running");

        // SEARCH-2461
        assertException("=tok:true:Running");
        assertException("=tok:both:Running");

        // SEARCH-2461
        assertException("=tok:true:Run");
        assertException("=tok:both:Run");

    }

    @Test
    public void exactSearch_multiTerm_shouldReturnResultsContainingExactTerm()
    {
        /* 2 results are expected: - Document #2 >> name: "Run" - Document #4 >> name: "Jump" */
        assertResponseCardinality("=run =jump", 2);

        /* No result for runner or jumper in cm:name property One document has runners and another record has jumpers in description You can see the difference between exact search and not */
        assertResponseCardinality("=runner =jumper", 0);
        assertResponseCardinality("runner jumper", 2);

        /* 2 results are expected: - Document #1 >> name: "Running" - Document #5 >> name: "Running jumping" */
        assertResponseCardinality("=running =jumping", 2);
    }

    @Test
    public void exactSearch_multiTermInFieldWithOnlyUnTokenizedAnalysis_shouldReturnFullFieldValueMatch() throws Exception
    {

        /*
         * 1 result is expected
         * - Document #4 >> title: "Running"
         */
        assertResponseCardinality("=tok:false:Running =tok:false:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        /**
         * Unsupported Exception is expected when using exact term search with tokenised properties
         */
        assertException("=tok:both:running =tok:both:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
        assertException("=tok:true:running =tok:true:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
    }

    /**
     * These tests should be re-enabled once the following tickets have been solved:
     * - https://alfresco.atlassian.net/browse/SEARCH-2461
     * - https://alfresco.atlassian.net/browse/SEARCH-2953
     */
    @Test(enabled=false)
    public void failing_exactSearch_multiTermInFieldWithOnlyUnTokenizedAnalysis_shouldReturnFullFieldValueMatch() throws Exception
    {
        // SEARCH-2953
        assertResponseCardinality("=tok:false:running =tok:false:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:running =tok:false:jumpers", 1);
        assertException("=tok:both:running =tok:both:jumpers");
        assertException("=tok:true:running =tok:true:jumpers");

    }

    @Test
    public void exactSearch_exactPhrase_shouldReturnResultsContainingExactPhrase()
    {
        /* No result for "run jump" in cm:name property */
        assertResponseCardinality("=\"run jump\"", 0);

        /* No result for "runner jumper" in cm:name property One document has runners jumpers in description You can see the difference between exact search and not */
        assertResponseCardinality("=\"runner jumper\"", 0);
        assertResponseCardinality("\"runner jumper\"", 1);

        /* 1 result is expected for exact term search: - Document #5 >> name: "Running jumping" */
        assertResponseCardinality("=\"running jumping\"", 1);

        /* 5 results are expected for non-exact phrase search: - Document #1 >> name: "Running", title: "Running jumping" - Document #2 >> name: "Run", description: "you are supposed to run jump" - Document #3 >> title: "Running jumping twice jumpers" - Document #4 >> content: "runnings jumpings", title: "Running" - Document #5 >> name: "Running jumping", title: "Running the art of jumping" */
        assertResponseCardinality("\"running jumping\"", 5);
    }

    @Test
    public void exactSearch_phraseInFieldConjunction_shouldReturnFullFieldValueMatch() throws Exception
    {
        /*
         * 1 result is expected for exact term search
         * - Document #5 >> name: "Running jumping"
         */
        assertResponseCardinality("=tok:false:\"Running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        /*
         * No result for "Running jumping twice" in cm:name property is expected
         */
        assertResponseCardinality("=tok:false:\"Running jumping twice\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 0);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved:
     * - https://alfresco.atlassian.net/browse/SEARCH-2461
     * - https://alfresco.atlassian.net/browse/SEARCH-2953
     */
    @Test(enabled=false)
    public void failing_exactSearch_phraseInFieldConjunction_shouldReturnFullFieldValueMatch() throws Exception
    {

        // SEARCH-2953
        assertResponseCardinality("=tok:false:\"running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:\"running jumping\"", 1);
        assertResponseCardinality("=tok:false:\"Running jumping\"", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:\"Running jumping twice\"", 0);

    }

    @Test
    public void exactSearch_phraseInFieldConjunction_shouldReturnOrException() throws Exception
    {
        /**
         * Unsupported Exception is expected when using exact term search with tokenised properties
         */
        assertException("=tok:true:\"running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
        assertException("=tok:both:\"running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']");

        /**
         * Unsupported Exception is expected when using exact term search with tokenised properties
         */
        assertException("=tok:true:\"Running jumping twice\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
        assertException("=tok:both:\"Running jumping twice\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']");
    }

    /**
     * These tests should be re-enabled once the following tickets have been solved:
     * - https://alfresco.atlassian.net/browse/SEARCH-2461
     */
    @Test(enabled=false)
    public void failing_exactSearch_phraseInFieldConjunction_shouldReturnException() throws Exception
    {
        // SEARCH-2461
        assertException("=tok:true:\"Running jumping\"");
        assertException("=tok:both:\"Running jumping\"");

        // SEARCH-2461
        assertException("=tok:true:\"Running jumping twice\"");
        assertException("=tok:both:\"Running jumping twice\"");
    }
}
