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
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.transform.client.registry.AbstractTransformRegistry;
import org.alfresco.transform.client.registry.SupportedTransform;
import org.alfresco.util.testing.category.DebugTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_TEXT_PLAIN;

/**
 * Abstract test class to check it is possible to create renditions from the quick files using combinations of
 * local transforms, legacy transforms and the Transform Service.
 *
 * @author adavis
 */
public abstract class AbstractRenditionTest extends AbstractRenditionIntegrationTest
{
    // This is the same order as produced by MimetypeMap
    public static final List<String> TAS_REST_API_SOURCE_EXTENSIONS = Arrays.asList(
            "gif", "jpg", "png", "msg", "doc","ppt", "xls", "docx", "pptx", "xlsx");

    public static final List<String> TAS_REST_API_EXCLUDE_LIST = Collections.EMPTY_LIST;

    public static final List<String> ALL_SOURCE_EXTENSIONS_EXCLUDE_LIST_LEGACY = Arrays.asList(
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

    @Override
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
                        thumbnailNames, renditionNames);

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
                        try
                        {
                            checkRendition(testFileName, renditionName, !expectedToFail.contains(sourceTragetRendition));
                            successes.add(sourceTragetRendition);
                            successCount++;
                        }
                        catch (AssertionFailedError e)
                        {
                            failures.add(sourceTragetRendition + " " + e.getMessage());
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
        internalTestAllSourceExtensions(196, 0, ALL_SOURCE_EXTENSIONS_EXCLUDE_LIST_LEGACY);
    }

    protected void internalTestAllSourceExtensions(int expectedRenditionCount, int expectedFailedCount,
                                                   List<String> excludeList) throws Exception
    {
        List<String> sourceExtensions = getAllSourceMimetypes();
        assertRenditionsOkayFromSourceExtension(sourceExtensions,
                excludeList, Collections.emptyList(), expectedRenditionCount, expectedFailedCount);
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

    /**
     * Gets transforms combinations that are possible regardless of renditions.
     */
    @Test
    @Category(DebugTests.class)
    public void testCountTotalTransforms()
    {
        StringBuilder sourceTargetList = new StringBuilder();
        StringBuilder sourceTargetPriorityList = new StringBuilder();
        AtomicInteger count = new AtomicInteger(0);
        int textTargetCount = 0;
        mimetypeService.getMimetypesByExtension();
        List<String> mimetypes = new ArrayList(mimetypeMap.getMimetypes());
        sortMimetypesByExtension(mimetypes);
        for (String sourceMimetype : mimetypes)
        {
            for (String targetMimetype : mimetypes)
            {
                if (transformServiceRegistry.isSupported(sourceMimetype, 1, targetMimetype, Collections.emptyMap(), null))
                {
                    logSourceTarget(sourceTargetList, sourceTargetPriorityList, count, sourceMimetype, targetMimetype);
                    if (MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
                    {
                        textTargetCount++;
                    }
                }
            }
        }

        System.out.println("Number of source to target mimetype transforms: "+count);
        System.out.println("Number of source to plain text transforms: "+textTargetCount);
        System.out.println(sourceTargetList);
        if (sourceTargetPriorityList.length() > 0)
        {
            System.out.println("");
            System.out.println("Alternate transforms");
            System.out.println(sourceTargetPriorityList);
        }
    }

    /**
     * Gets transforms combinations for the current set of renditions.
     */
    @Test
    @Category(DebugTests.class)
    public void testCountTotalRenditionTransforms()
    {
        StringBuilder sourceTargetList = new StringBuilder();
        AtomicInteger count = new AtomicInteger(0);
        RenditionDefinitionRegistry2 renditionDefinitionRegistry = renditionService2.getRenditionDefinitionRegistry2();
        List<String> sourceMimetypes = new ArrayList(mimetypeMap.getMimetypes());
        sortMimetypesByExtension(sourceMimetypes);
        for (String sourceMimetype: sourceMimetypes)
        {
            Set<String> targetMimetypes = new HashSet<>();
            for (String renditionName : renditionDefinitionRegistry.getRenditionNamesFrom(sourceMimetype, 1))
            {
                RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry.getRenditionDefinition(renditionName);
                String targetMimetype = renditionDefinition.getTargetMimetype();
                targetMimetypes.add(targetMimetype);
            }

            List<String> targetMimetypesSorted = new ArrayList(targetMimetypes);
            sortMimetypesByExtension(targetMimetypesSorted);
            for (String targetMimetype : targetMimetypesSorted)
            {
                logSourceTarget(sourceTargetList, null, count, sourceMimetype, targetMimetype);
            }
        }

        System.out.println("Number of source to target mimetype transforms via renditions: "+count.get());
        System.out.println(sourceTargetList);
    }

    private void logSourceTarget(StringBuilder sourceTargetList, StringBuilder sourceTargetPriorityList, AtomicInteger count, String sourceMimetype, String targetMimetype)
    {
        count.incrementAndGet();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        String line = String.format("%4d %4s %4s\n", count.get(), sourceExtension, targetExtension);
        sourceTargetList.append(line);

        if (sourceTargetPriorityList != null)
        {
            AbstractTransformRegistry registry = getAbstractTransformRegistry();
            if (registry != null)
            {
                Map<String, List<SupportedTransform>> supportedTransformsByTargetMimetype = registry.getData().retrieveTransforms(sourceMimetype);
                List<SupportedTransform> supportedTransforms = new ArrayList<>(supportedTransformsByTargetMimetype.get(targetMimetype));
                supportedTransforms.sort((t1, t2) -> t1.getPriority()-t2.getPriority());
                char a = 'a';
                int prevPriority = Integer.MAX_VALUE;
                for (SupportedTransform supportedTransform : supportedTransforms)
                {
                    int priority = supportedTransform.getPriority();
                    long maxSourceSizeBytes = supportedTransform.getMaxSourceSizeBytes();
                    String priorityUnchanged = prevPriority == priority ? "*" : "";
                    String transformName = supportedTransform.getName();
                    line = String.format("%4d %4s %4s %c) [%d%s] %s %d\n", count.get(), sourceExtension, targetExtension,
                            a++, priority, priorityUnchanged, transformName, maxSourceSizeBytes);
                    sourceTargetPriorityList.append(line);
                    prevPriority = priority;
                }
            }
        }
    }

    protected AbstractTransformRegistry getAbstractTransformRegistry()
    {
        return null;
    }

    private void sortMimetypesByExtension(List<String> mimetypes)
    {
        List<String> extensions = new ArrayList(mimetypes.size());
        for (String mimetype : mimetypes)
        {
            extensions.add(mimetypeMap.getExtension(mimetype));
        }
        Collections.sort(extensions);

        mimetypes.clear();
        for (String extension : extensions)
        {
            mimetypes.add(mimetypeService.getMimetype(extension));
        }
    }
}
