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

import static java.util.Locale.ENGLISH;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.DialectUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.extensions.surf.util.I18NUtil;

public class SchemaDifferenceHelper
{
    private static Log logger = LogFactory.getLog(SchemaDifferenceHelper.class);

    private Dialect dialect;
    private PatchService patchService;
    private List<SchemaUpgradeScriptPatch> optionalUpgradePatches;
    private ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

    public SchemaDifferenceHelper(Dialect dialect, PatchService patchService)
    {
        this.dialect = dialect;
        this.patchService = patchService;
        this.optionalUpgradePatches = new ArrayList<SchemaUpgradeScriptPatch>(4);
    }

    public SchemaDifferenceHelper(Dialect dialect, PatchService patchService,
            List<SchemaUpgradeScriptPatch> upgradePatches)
    {
        this.dialect = dialect;
        this.patchService = patchService;
        this.optionalUpgradePatches = upgradePatches;
    }

    public void addUpgradeScriptPatch(SchemaUpgradeScriptPatch patch)
    {
        if (patch.isIgnored())
        {
            this.optionalUpgradePatches.add(patch);
        }
    }

    public String findPatchCausingDifference(Difference difference)
    {
        for (SchemaUpgradeScriptPatch patch: optionalUpgradePatches)
        {
           if (!isPatchApplied(patch))
           {
               List<String> problemPatterns = getProblemsPatterns(patch);
               for (String problemPattern: problemPatterns)
               {
                   if (describe(difference).matches(problemPattern))
                   {
                       return patch.getId();
                   }
               }
           }
        }

        return null;
    }

    private boolean isPatchApplied(SchemaUpgradeScriptPatch patch)
    {
        return patchService.getPatch(patch.getId()) != null;
    }

    protected Resource getDialectResource(String resourceUrl)
    {
        if(resourceUrl == null)
        {
            return null;
        }

        return DialectUtil.getDialectResource(rpr, dialect.getClass(), resourceUrl);
    }

    private List<String> getProblemsPatterns(SchemaUpgradeScriptPatch patch)
    {
        List<String> optionalProblems = new ArrayList<>();
        String problemFileUrl = patch.getProblemPatternsFileUrl();
        Resource problemFile = getDialectResource(problemFileUrl);

        if (problemFile != null)
        {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(problemFile.getInputStream(), StandardCharsets.UTF_8))) 
            {
                String line = reader.readLine();
                while (line != null)
                {
                   optionalProblems.add(line);
                   line = reader.readLine();
                }
            }
            catch (Exception ex)
            {
                logger.error("Error while parsing problems patterns for patch " + patch.getId() + ex);
            }
        }

        return optionalProblems;
    }

    protected String describe(Difference difference)
    {
        if (difference.getLeft() == null)
        {
            return I18NUtil.getMessage(
                        "system.schema_comp.diff.target_only",
                        ENGLISH,
                        difference.getRight().getDbObject().getTypeName(),
                        difference.getRight().getPath(),
                        difference.getRight().getPropertyValue());
        }
        if (difference.getRight() == null)
        {
            return I18NUtil.getMessage(
                        "system.schema_comp.diff.ref_only",
                        ENGLISH,
                        difference.getLeft().getDbObject().getTypeName(),
                        difference.getLeft().getPath(),
                        difference.getLeft().getPropertyValue());
        }

        return I18NUtil.getMessage(
                    "system.schema_comp.diff",
                    ENGLISH,
                    difference.getLeft().getDbObject().getTypeName(),
                    difference.getLeft().getPath(),
                    difference.getLeft().getPropertyValue(),
                    difference.getRight().getPath(),
                    difference.getRight().getPropertyValue());
    }
}
