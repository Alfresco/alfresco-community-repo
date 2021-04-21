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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

@Category({OwnJVMTestsCategory.class})
public class SchemaBootstrapTest
{
    private static final String BOOTSTRAP_TEST_CONTEXT = "classpath*:alfresco/dbscripts/test-bootstrap-context.xml";
    private static final List<String> TEST_SCHEMA_REFERENCE_URLS = Arrays.asList(
            "classpath:alfresco/dbscripts/create/${db.script.dialect}/Test-Schema-Reference-ALF.xml",
            "classpath:alfresco/dbscripts/create/${db.script.dialect}/Schema-Reference-ACT.xml");

    private static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides(BOOTSTRAP_TEST_CONTEXT);

    @ClassRule
    public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT);

    private SchemaBootstrap schemaBootstrap;
    private SchemaUpgradeScriptPatch optionalPatch;

    @Before
    public void setUp() throws Exception
    {
        schemaBootstrap = (SchemaBootstrap) APP_CONTEXT_INIT.getApplicationContext().getBean("schemaBootstrap");
        schemaBootstrap.setSchemaReferenceUrls(TEST_SCHEMA_REFERENCE_URLS);
        optionalPatch = (SchemaUpgradeScriptPatch) APP_CONTEXT_INIT.getApplicationContext().getBean("patchDbVOAddIndexTest");
    }

    @Test
    public void shouldSchemaValidationReportProblemsCausedByUnappliedOptionalPatch()
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();

        PrintWriter out = new PrintWriter(buff);
        int numProblems = schemaBootstrap.validateSchema(null, out);
        out.flush();

        assertEquals(1, numProblems);
        String problems = buff.toString();
        assertTrue("Missing optional patch-specific problems report: \n" + problems,
                problems.contains("The following problems will be resolved once the long running patch "
                        + optionalPatch.getId() + " has been run"));
    }

}
