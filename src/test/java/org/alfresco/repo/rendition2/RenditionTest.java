/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import junit.framework.AssertionFailedError;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.transform.client.model.config.TransformServiceRegistry;
import org.alfresco.util.testing.category.DebugTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Test it is possible to create renditions from the quick files.
 *
 * @author adavis
 */
public class RenditionTest extends AbstractRenditionIntegrationTest
{
    // This is the same order as produced by MimetypeMap
    public static final List<String> TAS_REST_API_SOURCE_EXTENSIONS = Arrays.asList(
            "gif", "jpg", "png", "msg", "doc","ppt", "xls", "docx", "pptx", "xlsx");

    public static final List<String> TAS_REST_API_EXCLUDE_LIST = Collections.EMPTY_LIST;

    public static final List<String> ALL_SOURCE_EXTENSIONS_EXCLUDE_LIST = Arrays.asList(
            "key jpg imgpreview",
            "key jpg medium",
            "key png doclib",
            "key png avatar",
            "key png avatar32",

            "pages jpg imgpreview",
            "pages jpg medium",
            "pages png doclib",
            "pages png avatar",
            "pages png avatar32",

            "numbers jpg imgpreview",
            "numbers jpg medium",
            "numbers png doclib",
            "numbers png avatar",
            "numbers png avatar32",

            "tiff jpg imgpreview",
            "tiff jpg medium",
            "tiff png doclib",
            "tiff png avatar",
            "tiff png avatar32",

            "wpd pdf pdf",
            "wpd jpg medium",
            "wpd png doclib",
            "wpd png avatar",
            "wpd png avatar32",
            "wpd jpg imgpreview");

    @Autowired
    private TransformServiceRegistry transformServiceRegistry;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getAdminUserName());
    }

    private Set<String> getThumbnailNames(List<ThumbnailDefinition> thumbnailDefinitions)
    {

        Set<String> names = new HashSet<>();
        for (ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
        {
            String name = thumbnailDefinition.getName();
            names.add(name);
        }
        return names;
    }

    private void assertRenditionsOkayFromSourceExtension(List<String> sourceExtensions, List<String> excludeList, List<String> expectedToFail,
                                                         int expectedRenditionCount, int expectedFailedCount) throws Exception
    {
        int renditionCount = 0;
        int failedCount = 0;
        int successCount = 0;
        int excludedCount = 0;
        RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
        StringJoiner failures = new StringJoiner("\n");
        StringJoiner successes = new StringJoiner("\n");

        for (String sourceExtension : sourceExtensions)
        {
            String sourceMimetype = mimetypeMap.getMimetype(sourceExtension);
            String testFileName = getTestFileName(sourceMimetype);
            if (testFileName != null)
            {
                Set<String> renditionNames = renditionDefinitionRegistry2.getRenditionNamesFrom(sourceMimetype, -1);
                List<ThumbnailDefinition> thumbnailDefinitions = thumbnailRegistry.getThumbnailDefinitions(sourceMimetype, -1);
                Set<String> thumbnailNames = getThumbnailNames(thumbnailDefinitions);
                assertEquals("There should be the same renditions ("+renditionNames+") as deprecated thumbnails ("+thumbnailNames+")",
                        renditionNames, thumbnailNames);

                renditionCount += renditionNames.size();
                for (String renditionName : renditionNames)
                {
                    RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
                    String targetMimetype = renditionDefinition.getTargetMimetype();
                    String targetExtension = mimetypeMap.getExtension(targetMimetype);

                    String sourceTragetRendition = sourceExtension + ' ' + targetExtension + ' ' + renditionName;
                    if (excludeList.contains(sourceTragetRendition))
                    {
                        excludedCount++;
                    }
                    else
                    {
                        String task = sourceExtension + " " + targetExtension + " " + renditionName;

                        try
                        {
                            checkRendition(testFileName, renditionName, !expectedToFail.contains(sourceTragetRendition));
                            successes.add(task);
                            successCount++;
                        }
                        catch (AssertionFailedError e)
                        {
                            failures.add(task + " " + e.getMessage());
                            failedCount++;
                        }
                    }
                }
            }
        }

        int expectedSuccessCount = expectedRenditionCount - excludedCount - expectedFailedCount;
        System.out.println("FAILURES:\n"+failures+"\n");
        System.out.println("SUCCESSES:\n"+successes+"\n");
        System.out.println("renditionCount: "+renditionCount+" expected "+expectedRenditionCount);
        System.out.println("   failedCount: "+failedCount+" expected "+expectedFailedCount);
        System.out.println("  successCount: "+successCount+" expected "+expectedSuccessCount);

        assertEquals("Rendition count has changed", expectedRenditionCount, renditionCount);
        assertEquals("Failed rendition count has changed", expectedFailedCount, failedCount);
        assertEquals("Successful rendition count has changed", expectedSuccessCount, successCount);
        if (failures.length() > 0)
        {
            fail(failures.toString());
        }
    }

    @Test
    public void testExpectedNumberOfRenditions() throws Exception
    {
        RenditionDefinitionRegistry2 renditionDefinitionRegistry21 = renditionService2.getRenditionDefinitionRegistry2();
        Set<String> renditionNames = renditionDefinitionRegistry21.getRenditionNames();
        assertEquals("Added or removed a definition (rendition-service2-contex.xml)?", 7, renditionNames.size());
    }

    @Category(DebugTests.class)
    public void testTasRestApiRenditions() throws Exception
    {
        internalTestTasRestApiRenditions(62, 0);
    }

    protected void internalTestTasRestApiRenditions(int expectedRenditionCount, int expectedFailedCount) throws Exception
    {
        assertRenditionsOkayFromSourceExtension(TAS_REST_API_SOURCE_EXTENSIONS, TAS_REST_API_EXCLUDE_LIST,
                Collections.emptyList(), expectedRenditionCount, expectedFailedCount);
    }

    @Category(DebugTests.class)
    @Test
    public void testAllSourceExtensions() throws Exception
    {
        internalTestAllSourceExtensions(196, 0);
    }

    protected void internalTestAllSourceExtensions(int expectedRenditionCount, int expectedFailedCount) throws Exception
    {
        List<String> sourceExtensions = getAllSourceMimetypes();
        assertRenditionsOkayFromSourceExtension(sourceExtensions,
                ALL_SOURCE_EXTENSIONS_EXCLUDE_LIST,
                Collections.emptyList(), expectedRenditionCount, expectedFailedCount);
    }

    @Category(DebugTests.class)
    @Test
    public void testTransformServiceConfig() throws Exception
    {
        internalTestTransformServiceConfig(57, 0);
    }

    // Tests all renditions supported by the TransformServiceRegistry (in the case of Transform Service, see
    // transform-service-config.json and the LegacyLocalTransformServiceRegistry see the original ACS config).
    protected void internalTestTransformServiceConfig(int expectedRenditionCount, int expectedFailedCount) throws Exception
    {
        List<String> sourceExtensions = getAllSourceMimetypes();
        List<String> excludeList = new ArrayList<>();

        for (String sourceExtension : sourceExtensions)
        {
            String sourceMimetype = mimetypeMap.getMimetype(sourceExtension);
            String testFileName = getTestFileName(sourceMimetype);
            if (testFileName != null)
            {
                Set<String> renditionNames = renditionDefinitionRegistry2.getRenditionNamesFrom(sourceMimetype, -1);
                for (String renditionName : renditionNames)
                {
                    RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
                    String targetMimetype = renditionDefinition.getTargetMimetype();
                    String targetExtension = mimetypeMap.getExtension(targetMimetype);

                    String sourceTragetRendition = sourceExtension + ' ' + targetExtension + ' ' + renditionName;
                    Map<String, String> actualOptions = renditionDefinition.getTransformOptions();
                    if (!transformServiceRegistry.isSupported(sourceMimetype, -1L, targetMimetype,
                            actualOptions, null))
                    {
                        excludeList.add(sourceTragetRendition);
                    }
                }
            }
        }

        assertRenditionsOkayFromSourceExtension(sourceExtensions, excludeList,
                Collections.emptyList(), expectedRenditionCount, expectedFailedCount);
    }

    private List<String> getAllSourceMimetypes()
    {
        List<String> sourceExtensions = new ArrayList<>();
        for (String sourceMimetype : mimetypeMap.getMimetypes())
        {
            String sourceExtension = mimetypeMap.getExtension(sourceMimetype);
            sourceExtensions.add(sourceExtension);
        }
        return sourceExtensions;
    }

    @Test
    public void testGifRenditions() throws Exception
    {
        internalTestGifRenditions(5, 0);
    }

    protected void internalTestGifRenditions(int expectedRenditionCount, int expectedFailedCount) throws Exception
    {
        assertRenditionsOkayFromSourceExtension(Arrays.asList("gif"),
                Collections.emptyList(), Collections.emptyList(), expectedRenditionCount, expectedFailedCount);
    }
}
