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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test intended for use in the continuous integration system that checks
 * whether the schema reference file (for whichever database the tests are being
 * run against) is in sync with the actual schema. If the test fails (and the
 * schema comparator is in working order) then the most likely cause is that a
 * new up-to-date schema reference file needs to be created.
 * <p>
 * Schema reference files are created using the {@link DbToXML} tool.
 * <p>
 * Note: if no reference file exists then the test will pass, this is to allow
 * piece meal introduction of schmea reference files.
 * 
 * @see DbToXML
 * @author Matt Ward
 */
@Category({ OwnJVMTestsCategory.class, DBTests.class })
public class SchemaReferenceFileTest
{
    private ClassPathXmlApplicationContext ctx;
    private SchemaBootstrap schemaBootstrap;

    @Before
    public void setUp() throws Exception
    {
        ctx = (ClassPathXmlApplicationContext) ApplicationContextHelper.getApplicationContext();
        schemaBootstrap = (SchemaBootstrap) ctx.getBean("schemaBootstrap");
    }

    @Test
    public void checkReferenceFile()
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(buff);
        int maybeProblems = schemaBootstrap.validateSchema(null, out);
        out.flush();

        if (maybeProblems > 0)
        {
            List<String> errors = computeRealErrors(buff);
            assertTrue("\n"+buff, errors.isEmpty());
        }
    }

    private List<String> computeRealErrors(ByteArrayOutputStream buff)
    {
        String[] lines = buff.toString().split("\\n");

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();
            if (line.isEmpty())
                break;
            errors.add(line);
        }
        return errors;
    }
}
