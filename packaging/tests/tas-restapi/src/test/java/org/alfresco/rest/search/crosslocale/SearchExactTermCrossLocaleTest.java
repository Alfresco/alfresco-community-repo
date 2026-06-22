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

package org.alfresco.rest.search.crosslocale;

import org.testng.annotations.Test;

import org.alfresco.rest.search.AbstractSearchExactTermTest;

/**
 * Tests including all different tokenization (false, true, both) modes with Exact Term queries. Search Services must be configured with Cross Locale enabled in order to run these tests. These tests are based in AFTSDefaultTextQueryIT class, but an additional type of property has been added (tok:true) in order to provide full coverage for the available options.
 */
public class SearchExactTermCrossLocaleTest extends AbstractSearchExactTermTest
{

    /**
     * Note these tests are searching in cm:name, cm:title, cm:description and cm:content properties
     */
    @Test
    public void exactSearch_singleTerm_shouldReturnResultsContainingExactTerm()
    {
        /* 2 results are expected: - Document #2 >> name: "Run", description: "you are supposed to run jump", title: "Run : a philosophy" - Document #5 >> content: "run is Good as jump" */
        assertResponseCardinality("=run", 2);

        /* No result for runner, Document #5 has "runners" in description, you can see the difference between exact search and not */
        assertResponseCardinality("=runner", 0);
        assertResponseCardinality("runner", 1);

        /* 3 results are expected: - Document #2 >> description: "you are supposed to run jump", content: "after many runs you are tired and if you jump it happens the same" - Document #4 >> name: "Jump" - Document #5 >> content: "run is Good as jump" */
        assertResponseCardinality("=jump", 3);

    }

    @Test
    public void exactSearch_singleTermConjunction_shouldReturnFullFieldValueMatch()
    {

        /**
         * Since REST API is getting the results from DB or Search Services, using single term expressions is always retrieved from DB when using default configuration "solr.query.fts.queryConsistency=TRANSACTIONAL_IF_POSSIBLE". Combining this single term with range queries (like cm:created) will ensure the results are coming from SOLR in this mode.
         */

        /* 1 result is expected for non-tokenised field (tok:false) - Document #4 >> title: "Running" */
        assertResponseCardinality("=tok:false:Running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        /* 0 results are expected for non-tokenised field (tok:false), as there is no title: "Run" */
        assertResponseCardinality("=tok:false:Run AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 0);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved: - https://alfresco.atlassian.net/browse/SEARCH-2461 - https://alfresco.atlassian.net/browse/SEARCH-2953
     */
    @Test(enabled = false)
    public void failing_exactSearch_singleTerm_shouldReturnFullFieldValueMatch()
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
    public void exactSearch_singleTermConjunction_shouldReturnPartialFieldValueMatch()
    {

        /**
         * Since REST API is getting the results from DB or Search Services, using single term expressions is always retrieved from DB when using default configuration "solr.query.fts.queryConsistency=TRANSACTIONAL_IF_POSSIBLE". Combining this single term with range queries (like cm:created) will ensure the results are coming from SOLR in this mode.
         */

        /* 4 results are expected for tokenised fields (tok:true, tok:both) - Document #1 >> title: "Running jumping" - Document #3 >> title: "Running jumping twice jumpers" - Document #4 >> title: "Running" - Document #5 >> title: "Running the art of jumping" */
        assertResponseCardinality("=tok:true:running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 4);
        assertResponseCardinality("=tok:both:running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 4);

        /**
         * 4 results are expected for tokenised fields (tok:true, tok:both) - Document #1 >> title: "Running jumping" - Document #3 >> title: "Running jumping twice jumpers" - Document #4 >> title: "Running" - Document #5 >> title: "Running the art of jumping"
         */
        assertResponseCardinality("=tok:true:Running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 4);
        assertResponseCardinality("=tok:both:Running AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 4);

        /**
         * 1 result is expected for tokenised fields (tok:true, tok:both) - Document #2 >> title: "Run : a philosophy"
         */
        assertResponseCardinality("=tok:true:Run AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);
        assertResponseCardinality("=tok:both:Run AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved: - https://alfresco.atlassian.net/browse/SEARCH-2461
     */
    @Test(enabled = false)
    public void failing_exactSearch_singleTerm_shouldReturnPartialFieldValueMatch()
    {

        // SEARCH-2461
        assertResponseCardinality("=tok:true:running", 4);
        assertResponseCardinality("=tok:both:running", 4);

        // SEARCH-2461
        assertResponseCardinality("=tok:true:Running", 4);
        assertResponseCardinality("=tok:both:Running", 4);

        // SEARCH-2461
        assertResponseCardinality("=tok:true:Run", 1);
        assertResponseCardinality("=tok:both:Run", 1);

    }

    /**
     * Note these tests are searching in cm:name, cm:title, cm:description and cm:content properties
     */
    @Test(enabled = false)
    public void exactSearch_multiTerm_shouldReturnResultsContainingExactTerm()
    {
        /* 3 results are expected: - Document #2 >> name: "Run", description: "you are supposed to run jump", title: "Run : a philosophy", content: "after many runs you are tired and if you jump it happens the same" - Document #4 >> name: "Jump" - Document #5 >> content: "run is Good as jump" */
        assertResponseCardinality("=run =jump", 3);

        /* No result for runner or jumper Document #3 has "jumpers" in description and title Document #5 has "runners" and "jumpers" in description You can see the difference between exact search and not */
        assertResponseCardinality("=runner =jumper", 0);
        assertResponseCardinality("runner jumper", 2);

        /* 5 results are expected: - Document #1 >> name: "Running", description: "Running is a sport is a nice activity", content: "when you are running you are doing an amazing sport", title: "Running jumping" - Document #2 >> name: "Run", description: "you are supposed to run jump", content: "after many runs you are tired and if you jump it happens the same", title: "Run : a philosophy" - Document #3 >> title: "Running jumping twice jumpers" - Document #4 >> content: "runnings jumpings", title: "Running" - Document #5 >> name: "Running jumping", title: "Running the art of jumping" */
        assertResponseCardinality("=running =jumping", 5);
    }

    @Test
    public void exactSearch_multiTermInField_shouldReturnPartialFieldValueMatch()
    {
        /**
         * 4 results are expected for tokenised fields (tok:true, tok:both) - Document #1 >> title: "Running jumping" - Document #3 >> title: "Running jumping twice jumpers" - Document #4 >> title: "Running" - Document #5 >> title: "Running the art of jumping"
         */
        assertResponseCardinality("=tok:both:running =tok:both:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 4);
        assertResponseCardinality("=tok:true:running =tok:true:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 4);
    }

    /**
     * These tests should be re-enabled once the following tickets have been solved: - https://alfresco.atlassian.net/browse/SEARCH-2461
     */
    @Test(enabled = false)
    public void failing_exactSearch_multiTermInField_shouldReturnPartialFieldValueMatch()
    {

        // SEARCH-2461
        assertResponseCardinality("=tok:both:running =tok:both:jumpers", 4);
        assertResponseCardinality("=tok:true:running =tok:true:jumpers", 4);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved: - https://alfresco.atlassian.net/browse/SEARCH-2461 - https://alfresco.atlassian.net/browse/SEARCH-2953
     */
    @Test(enabled = false)
    public void failing_exactSearch_multiTermInField_shouldReturnFullFieldValueMatch()
    {

        // SEARCH-2953
        assertResponseCardinality("=tok:false:running =tok:false:jumpers AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:running =tok:false:jumpers", 0);

    }

    /**
     * Note these tests are searching in cm:name, cm:title, cm:description and cm:content properties
     */
    @Test
    public void exactSearch_exactPhrase_shouldReturnResultsContainingExactPhrase()
    {
        /* 1 results are expected: - Document #2 >> description: "you are supposed to run jump" */
        assertResponseCardinality("=\"run jump\"", 1);

        /* No result for "runner jumper" using exact term search Document #5 has "runners" and "jumpers" in description, so it should be a result for not exact term search You can see the difference between exact search and not */
        assertResponseCardinality("=\"runner jumper\"", 0);
        assertResponseCardinality("\"runner jumper\"", 1);

        /* 3 results are expected for exact term search: - Document #1 >> title: "Running jumping" - Document #3 >> title: "Running jumping twice jumpers" - Document #5 >> name: "Running jumping"
         *
         * When not using exact term search, 4 results are expected Since 'Milestone' wiki page (coming from ootb content) is including "running" in the content, we are checking for 5 results instead of 4
         *
         * You can see the difference between exact search and not */
        assertResponseCardinality("=\"running jumping\"", 3);
        assertResponseCardinality("\"running jumping\"", 5);
    }

    @Test
    public void exactSearch_phraseInFieldConjunction_shouldReturnFullFieldValueMatch()
    {
        /**
         * 1 results is expected for non-tokenised field (tok:false) - Document #1 >> title: "Running jumping"
         */
        assertResponseCardinality("=tok:false:\"Running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        /**
         * No result is expected for non-tokenised field (tok:false), as there is no title: "Running jumping twice"
         */
        assertResponseCardinality("=tok:false:\"Running jumping twice\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 0);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved: - https://alfresco.atlassian.net/browse/SEARCH-2461 - https://alfresco.atlassian.net/browse/SEARCH-2953
     */
    @Test(enabled = false)
    public void failing_exactSearch_phraseInFieldConjunction_shouldReturnFullFieldValueMatch()
    {

        // SEARCH-2953
        assertResponseCardinality("=tok:false:\"running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:\"running jumping\"", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:\"Running jumping\"", 1);

        // SEARCH-2461
        assertResponseCardinality("=tok:false:\"Running jumping twice\"", 0);

    }

    @Test
    public void exactSearch_phraseInFieldConjunction_shouldReturnPartialFieldValueMatch()
    {
        /**
         * 2 results are expected for tokenised fields (tok:true, tok:both) - Document #1 >> title: "Running jumping" - Document #3 >> title: "Running jumping twice jumpers"
         */
        assertResponseCardinality("=tok:true:\"running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 2);
        assertResponseCardinality("=tok:both:\"running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 2);

        /**
         * 2 results are expected for tokenised fields (tok:true, tok:both) - Document #1 >> title: "Running jumping" - Document #3 >> title: "Running jumping twice jumpers"
         */
        assertResponseCardinality("=tok:true:\"Running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 2);
        assertResponseCardinality("=tok:both:\"Running jumping\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 2);

        /**
         * 1 result is expected for tokenised fields (tok:true, tok:both) - Document #3 >> title: "Running jumping twice jumpers"
         */
        assertResponseCardinality("=tok:true:\"Running jumping twice\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);
        assertResponseCardinality("=tok:both:\"Running jumping twice\" AND cm:created:['" + fromDate + "' TO '" + toDate + "']", 1);

    }

    /**
     * These tests should be re-enabled once the following tickets have been solved: - https://alfresco.atlassian.net/browse/SEARCH-2461
     */
    @Test(enabled = false)
    public void failing_exactSearch_phraseInFieldConjunction_shouldReturnPartialFieldValueMatch()
    {

        // SEARCH-2461
        assertResponseCardinality("=tok:true:\"running jumping\"", 2);
        assertResponseCardinality("=tok:both:\"running jumping\"", 2);

        // SEARCH-2461
        assertResponseCardinality("=tok:true:\"Running jumping\"", 2);
        assertResponseCardinality("=tok:both:\"Running jumping\"", 2);

        // SEARCH-2461
        assertResponseCardinality("=tok:true:\"Running jumping twice\"", 1);
        assertResponseCardinality("=tok:both:\"Running jumping twice\"", 1);

    }

}
