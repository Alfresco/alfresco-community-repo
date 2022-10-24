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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import junit.framework.TestCase;
import org.mockito.Mockito;

public class UnsafeMethodsTest extends TestCase
{
    private static final String TEST_TEMPLATES_DIR = "/org/alfresco/repo/template/templates/";
    private static final String ALLOWED_TEXT = ": ALLOWED";
    private static final String BLOCKED_TEXT = ": BLOCKED";

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);

    public void testUnsafeMethods() throws Exception
    {
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        File templatesDir = new File(jarFile.getPath() + TEST_TEMPLATES_DIR);
        configuration.setDirectoryForTemplateLoading(templatesDir);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        Template template = configuration.getTemplate("unsafemethods.ftl");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("allowedText", ALLOWED_TEXT);
        model.put( "blockedText", BLOCKED_TEXT);

        final List<String> allowedMethods = createListOfAllowedMethods();
        final List<String> blockedMethods = createListOfBlockedMethods();

        // Prepare Spies for methods testing
        Thread thread = Mockito.spy(new Thread());
        model.put("thread", thread);

        // Apply the freemarker template
        List<String> results = applyTemplate(template, model);

        // Verify methods were allowed or blocked
        verifyFreemarkerOutput(results, allowedMethods, blockedMethods);
        verifyMethodInvocations(thread);
    }

    private List<String> applyTemplate(Template template, Map<String, Object> inputModel ) throws TemplateException, IOException
    {
        StringWriter stringWriter = new StringWriter();
        template.process(inputModel, stringWriter);
        String[] lines = stringWriter.toString().split(System.getProperty("line.separator"));
        return new ArrayList<>(Arrays.asList(lines));
    }

    private void verifyFreemarkerOutput(Collection<String> results, List<String> allowedMethods, List<String> blockedMethods)
    {
        allowedMethods.forEach(method -> assertTrue("Expected method '" + method + "' to be allowed", results.contains(method + ALLOWED_TEXT)));

        blockedMethods.forEach(method -> assertTrue("Expected method '" + method + "' to be blocked", results.contains(method + BLOCKED_TEXT)));
    }

    private void verifyMethodInvocations(Thread thread) throws InterruptedException
    {
        // verify an unlisted method is not blocked
        Mockito.verify(thread, Mockito.times(1)).getId();

        // Verify an originally blocked method is still blocked
        Mockito.verify(thread, Mockito.never()).interrupt();

        // Verify a newly blocked method in the patched version is blocked
        Mockito.verify(thread, Mockito.never()).currentThread();

    }

    private static List<String> createListOfAllowedMethods()
    {
        final List<String> allowedMethods = new ArrayList<>(1);
        allowedMethods.add("java.lang.Thread.getId()");
        return allowedMethods;
    }

    private static List<String> createListOfBlockedMethods()
    {
        List<String> blockedMethods = new ArrayList<>(2);
        blockedMethods.add("java.lang.Thread.interrupt()");
        blockedMethods.add("java.lang.Thread.currentThread()");
        return blockedMethods;
    }
}
