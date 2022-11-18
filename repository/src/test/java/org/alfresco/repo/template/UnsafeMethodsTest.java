/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import junit.framework.TestCase;

public class UnsafeMethodsTest extends TestCase
{
    private static final String TEST_TEMPLATES_PACKAGE = "/org/alfresco/repo/template/templates/";
    private static final String ALLOWED_TEXT = ": ALLOWED";
    private static final String BLOCKED_TEXT = ": BLOCKED";
    private static final String EXPECTED_RESULT = "Freemarker Unsafe Methods Testing\n" +
            "=================================\n" +
            "java.lang.Thread.getId(): ALLOWED\n" +
            "java.lang.Thread.interrupt(): BLOCKED\n" +
            "java.lang.Thread.currentThread(): BLOCKED\n";

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);

    public void testUnsafeMethods() throws Exception
    {
        configuration.setClassForTemplateLoading(getClass(), TEST_TEMPLATES_PACKAGE);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        Template template = configuration.getTemplate("unsafemethods.ftl");

        Thread currentThread = Thread.currentThread();
        Map<String, Object> model = Map.of(
                "allowedText", ALLOWED_TEXT,
                "blockedText", BLOCKED_TEXT,
                "thread", currentThread);

        String result = applyTemplate(template, model);

        assertFalse(currentThread.isInterrupted());
        assertEquals(EXPECTED_RESULT, result);
    }

    private String applyTemplate(Template template, Map<String, Object> inputModel ) throws TemplateException, IOException
    {
        try (StringWriter stringWriter = new StringWriter())
        {
            template.process(inputModel, stringWriter);
            return stringWriter.toString();
        }
    }
}
