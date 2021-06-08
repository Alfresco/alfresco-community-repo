/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.util.schemacomp;

import static org.alfresco.util.schemacomp.Difference.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.surf.util.I18NUtil;

public class SchemaDifferenceHelperUnitTest
{
    private static final String TEST_PATCH_ID = "patch.db-V1.0-test";
    private static final String BASE_PROBLEM_PATTERN = ".*missing %s.*%s";

    private SchemaDifferenceHelper differenceHelper;
    private PatchService patchService;
    private Dialect dialect;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setup()
    {
        dialect = mock(Dialect.class);
        patchService = mock(PatchService.class);
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
    }

    @Test
    public void shouldNotFindPatchWhenThereAreNoUpgradePatches()
    {
        Difference diff = createDifference();
        differenceHelper = createHelper(Arrays.asList());

        assertNull(differenceHelper.findPatchCausingDifference(diff));
    }

    @Test
    public void shouldNotFindPatchWhenUpgradePatchHasBeenApplied() throws IOException
    {
        Difference diff = createDifference();
        SchemaUpgradeScriptPatch upgradeScript = createUpgradeScript(TEST_PATCH_ID);
        when(patchService.getPatch(TEST_PATCH_ID)).thenReturn(new AppliedPatch());

        differenceHelper = createHelper(Arrays.asList(upgradeScript));
        String result = differenceHelper.findPatchCausingDifference(diff);

        assertNull(result);
    }

    @Test
    public void shouldNotFindPatchWhenUpgradePatchDoesNotProvideAnyProblemPatternsFile() throws IOException
    {
        Difference diff = createDifference();
        SchemaUpgradeScriptPatch upgradeScript = createUpgradeScript(TEST_PATCH_ID);
        upgradeScript.setProblemsPatternFileUrl(null);
        when(patchService.getPatch(TEST_PATCH_ID)).thenReturn(null);

        differenceHelper = createHelper(Arrays.asList(upgradeScript));
        String result = differenceHelper.findPatchCausingDifference(diff);

        assertNull(result);
    }

    @Test
    public void shouldFindPatchWhenDifferenceCausedByUpgradePatchIsDetected() throws IOException
    {
        Index index = createTableIndex("alf_node");
        Difference diff = new Difference(Where.ONLY_IN_REFERENCE, new DbProperty(index), null);
        SchemaUpgradeScriptPatch upgradeScript = createUpgradeScript(TEST_PATCH_ID,
                String.format(BASE_PROBLEM_PATTERN, index.getTypeName(), "idx_alf_node_test"));
        when(patchService.getPatch(TEST_PATCH_ID)).thenReturn(null);

        differenceHelper = createHelper(Arrays.asList(upgradeScript));
        String result = differenceHelper.findPatchCausingDifference(diff);

        assertEquals(TEST_PATCH_ID, result);
    }

    private Difference createDifference()
    {
        return new Difference(Where.IN_BOTH_BUT_DIFFERENCE, new DbProperty(mock(DbObject.class)), new DbProperty(mock(DbObject.class)));
    }

    private Index createTableIndex(String tableName)
    {
        Table table = new Table(tableName);
        table.setParent(new Schema(""));
        return new Index(table, "idx_alf_node_test", Arrays.asList("col_a", "col_b"));
    }

    private SchemaUpgradeScriptPatch createUpgradeScript(String id, String problemPattern) throws IOException
    {
        SchemaUpgradeScriptPatch upgradeScript = new SchemaUpgradeScriptPatch();
        upgradeScript.setId(id);
        Path file = createTempFile(problemPattern);
        upgradeScript.setProblemsPatternFileUrl(file.toAbsolutePath().toString());
        return upgradeScript;
    }

    private SchemaUpgradeScriptPatch createUpgradeScript(String id) throws IOException
    {
        return createUpgradeScript(id, "");
    }

    private Path createTempFile() throws IOException
    {
        return Files.createTempFile(testFolder.getRoot().toPath(), null, "txt");
    }

    private Path createTempFile(String content) throws IOException
    {
        Path tempFile = createTempFile();
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
        return tempFile;
    }

    private SchemaDifferenceHelper createHelper(List<SchemaUpgradeScriptPatch> upgradePatches)
    {
        return new SchemaDifferenceHelper(dialect, patchService, upgradePatches)
        {
            @Override
            protected Resource getDialectResource(String resourceUrl)
            {
                try
                {
                    return new InputStreamResource(new FileInputStream(resourceUrl));
                }
                catch (Exception e)
                {
                    return null;
                }
            }
        };
    }
}
