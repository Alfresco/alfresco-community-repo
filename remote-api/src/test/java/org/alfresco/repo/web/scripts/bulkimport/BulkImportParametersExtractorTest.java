/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.bulkimport;

import static org.alfresco.repo.web.scripts.bulkimport.AbstractBulkFileSystemImportWebScript.PARAMETER_BATCH_SIZE;
import static org.alfresco.repo.web.scripts.bulkimport.AbstractBulkFileSystemImportWebScript.PARAMETER_DISABLE_RULES;
import static org.alfresco.repo.web.scripts.bulkimport.AbstractBulkFileSystemImportWebScript.PARAMETER_TARGET_NODEREF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.BulkImportParameters.ExistingFileMode;
import org.alfresco.repo.web.scripts.bulkimport.AbstractBulkFileSystemImportWebScript.BulkImportParametersExtractor;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptException;

public class BulkImportParametersExtractorTest
{
    private static final String TEST_NODE_REF = "workspace://SpacesStore/this-is-just-a-test-ref";
    private static final String TEST_MISSING_NODE_REF = "workspace://SpacesStore/this-is-just-a-not-existing-test-ref";
    private static final Integer DEFAULT_BATCH_SIZE = 1234;
    private static final Integer DEFAULT_NUMBER_OF_THREADS = 4321;

    @Test
    public void shouldExtractTargetRef() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF));

        final BulkImportParameters params = extractor.extract();

        assertNotNull(params);
        assertNotNull(params.getTarget());
        assertEquals(TEST_NODE_REF, params.getTarget().toString());
    }

    @Test
    public void shouldFallbackToDefaultValues() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF));

        final BulkImportParameters params = extractor.extract();

        assertEquals(DEFAULT_BATCH_SIZE, params.getBatchSize());
        assertEquals(DEFAULT_NUMBER_OF_THREADS, params.getNumThreads());
        assertFalse(params.isDisableRulesService());
        assertEquals(ExistingFileMode.SKIP, params.getExistingFileMode());
        assertNull(params.getLoggingInterval());
    }

    @Test
    public void shouldExtractDisableFolderRulesFlagWhenSetToTrue() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF,
                PARAMETER_DISABLE_RULES, "true"
                                                                             ));

        final BulkImportParameters params = extractor.extract();

        assertTrue(params.isDisableRulesService());
    }

    @Test
    public void shouldExtractDisableFolderRulesFlagWhenSetToFalse() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF,
                PARAMETER_DISABLE_RULES, "false"
                                                                             ));

        final BulkImportParameters params = extractor.extract();

        assertFalse(params.isDisableRulesService());
    }

    @Test
    public void shouldExtractDisableFolderRulesFlagWhenSetToNotBooleanValue() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF,
                PARAMETER_DISABLE_RULES, "unknown"
                                                                             ));

        final BulkImportParameters params = extractor.extract();

        assertFalse(params.isDisableRulesService());
    }

    @Test
    public void shouldPropagateFileNotFoundExceptionWhenTargetIsNotFound()
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_MISSING_NODE_REF));

        assertThrows(FileNotFoundException.class, () -> extractor.extract());
    }

    @Test
    public void shouldFailWithWebScriptExceptionWhenInvalidBatchSizeIsRequested() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF,
                PARAMETER_BATCH_SIZE, "not-a-number"));

        try
        {
            extractor.extract();
        }
        catch (WebScriptException e)
        {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains(PARAMETER_BATCH_SIZE));
            return;
        }

        fail("Expected exception to be thrown.");
    }

    @Test
    public void shouldFailWithWebScriptExceptionWhenNegativeBatchSizeIsRequested() throws FileNotFoundException
    {
        final BulkImportParametersExtractor extractor = givenExtractor(Map.of(
                PARAMETER_TARGET_NODEREF, TEST_NODE_REF,
                PARAMETER_BATCH_SIZE, "-1"));

        try
        {
            extractor.extract();
        }
        catch (WebScriptException e)
        {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains(PARAMETER_BATCH_SIZE));
            return;
        }

        fail("Expected exception to be thrown.");
    }

    private BulkImportParametersExtractor givenExtractor(Map<String, String> params)
    {

        return new BulkImportParametersExtractor(params::get, this::testRefCreator, DEFAULT_BATCH_SIZE, DEFAULT_NUMBER_OF_THREADS);
    }

    private NodeRef testRefCreator(String nodeRef, String path) throws FileNotFoundException
    {
        if (TEST_MISSING_NODE_REF.equals(nodeRef))
        {
            throw new FileNotFoundException(new NodeRef(nodeRef));
        }
        return new NodeRef(nodeRef);
    }

}