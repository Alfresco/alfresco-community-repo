/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.cmis.renditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionKind;
import org.alfresco.cmis.mapping.BaseCMISTest;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Stas Sokolovsky
 */
public class CMISRenditionServiceTest extends BaseCMISTest
{
    private static final String[] THUMBNAIL_NAMES = new String[] { "doclib", "webpreview", "imgpreview" };
    private static final CMISRenditionKind[] THUMBNAIL_KINDS = new CMISRenditionKind[] { CMISRenditionKind.THUMBNAIL, CMISRenditionKind.WEB_PREVIEW, CMISRenditionKind.WEB_PREVIEW };

    private NodeRef document;
    private List<CMISRendition> documentRenditions = new ArrayList<CMISRendition>();
    private CMISRendition icon16Rendition = new CMISRenditionImpl(null, "alf:icon16", "image/gif", CMISRenditionKind.ICON16, 16, 16, null, null, null);
    private CMISRendition icon32Rendition = new CMISRenditionImpl(null, "alf:icon32", "image/gif", CMISRenditionKind.ICON32, 32, 32, null, null, null);

    public void setUp() throws Exception
    {
        super.setUp();

        String documentName = "TestDocument" + System.currentTimeMillis();
        document = createDocument(documentName, "Test Content", MimetypeMap.MIMETYPE_PDF);

        documentRenditions = new ArrayList<CMISRendition>();
        for (int i = 0; i < THUMBNAIL_NAMES.length; ++i)
        {
            documentRenditions.add(createRendition(document, THUMBNAIL_NAMES[i], THUMBNAIL_KINDS[i]));
        }
    }

    public void testGetAllRenditions() throws Exception
    {
        List<CMISRendition> receivedRenditions = cmisRenditionService.getRenditions(document, "*");

        List<CMISRendition> expectedRenditions = new ArrayList<CMISRendition>();
        expectedRenditions.addAll(documentRenditions);
        expectedRenditions.add(icon16Rendition);
        expectedRenditions.add(icon32Rendition);

        assertRendiions(receivedRenditions, expectedRenditions);
    }

    public void testGetRenditionsByKind() throws Exception
    {
        testGetRenditionsByKind(CMISRenditionKind.THUMBNAIL);
        testGetRenditionsByKind(CMISRenditionKind.WEB_PREVIEW);
        testGetRenditionsByKind(CMISRenditionKind.ICON16);
        testGetRenditionsByKind(CMISRenditionKind.ICON32);

        testGetRenditionsByKind(CMISRenditionKind.WEB_PREVIEW, CMISRenditionKind.ICON32);
        testGetRenditionsByKind(CMISRenditionKind.THUMBNAIL, CMISRenditionKind.WEB_PREVIEW, CMISRenditionKind.ICON16, CMISRenditionKind.ICON32);
    }

    public void testGetRenditionsByMimetype() throws Exception
    {
        for (CMISRendition rendition : documentRenditions)
        {
            testGetRenditionsByMimetype(rendition.getMimeType());
        }
        testGetRenditionsByMimetype(icon16Rendition.getMimeType());
        testGetRenditionsByMimetype(icon32Rendition.getMimeType());

        String[] mimetypes = getDifferentMimetypes();
        testGetRenditionsByMimetype(mimetypes);
    }

    public void testEmptyFilter() throws Exception
    {
        List<CMISRendition> receivedRenditions = cmisRenditionService.getRenditions(document, null);
        assertTrue(receivedRenditions == null || receivedRenditions.size() == 0);

        receivedRenditions = cmisRenditionService.getRenditions(document, "cmis:none");
        assertTrue(receivedRenditions == null || receivedRenditions.size() == 0);
    }

    public void testRenditionsByBaseMimetype() throws Exception
    {
        String[] mimetypes = getDifferentMimetypes();
        List<String> baseMimetypeFilters = new ArrayList<String>();
        for (String mimetype : mimetypes)
        {
            String baseMimetype = getBaseType(mimetype);
            String filter = baseMimetype + "/*";
            baseMimetypeFilters.add(filter);

            testGetRenditionsByMimetype(filter);
        }

        testGetRenditionsByMimetype(baseMimetypeFilters.toArray(new String[0]));
    }

    public void testMixedFilter() throws Exception
    {
        String[] mimetypes = getDifferentMimetypes();
        testGetRenditions(THUMBNAIL_KINDS, mimetypes);
    }

    private void testGetRenditionsByMimetype(String... mimetypes) throws Exception
    {
        testGetRenditions(null, mimetypes);
    }

    private void testGetRenditionsByKind(CMISRenditionKind... kinds) throws Exception
    {
        testGetRenditions(kinds, null);
    }

    private void testGetRenditions(CMISRenditionKind[] kinds, String[] mimetypes) throws Exception
    {
        String filter = createFilter(kinds, mimetypes);
        List<CMISRendition> receivedRenditions = cmisRenditionService.getRenditions(document, filter);

        List<CMISRendition> expectedRenditions = new ArrayList<CMISRendition>();
        if (kinds != null)
        {
            for (CMISRenditionKind kind : kinds)
            {
                expectedRenditions.addAll(getRenditionsByKind(kind));
            }
        }
        if (mimetypes != null)
        {
            for (String mimetype : mimetypes)
            {
                expectedRenditions.addAll(getRenditionsByMimetype(mimetype));
            }
        }

        assertRendiions(receivedRenditions, expectedRenditions);
    }

    protected void tearDown() throws Exception
    {
        fileFolderService.delete(document);
        super.tearDown();
    }

    private CMISRendition createRendition(NodeRef nodeRef, String thumbnailName, CMISRenditionKind kind)
    {
        ThumbnailDefinition details = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(thumbnailName);
        NodeRef thumbnailNodeRef = thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT, details.getMimetype(), details.getTransformationOptions(), details
                .getName());

        CMISRenditionImpl rendition = new CMISRenditionImpl();
        rendition.setStreamId(thumbnailNodeRef.toString());
        rendition.setKind(kind);
        rendition.setMimeType(details.getMimetype());
        if (details.getTransformationOptions() instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions) details.getTransformationOptions();
            rendition.setWidth(imageOptions.getResizeOptions().getWidth());
            rendition.setHeight(imageOptions.getResizeOptions().getHeight());
        }

        return rendition;
    }

    private NodeRef createDocument(String documentName, String documentContent, String mimetype)
    {
        NodeRef textDocument = fileFolderService.create(rootNodeRef, "TEXT" + documentName, ContentModel.TYPE_CONTENT).getNodeRef();
        ContentWriter contentWriter = fileFolderService.getWriter(textDocument);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setLocale(Locale.ENGLISH);
        contentWriter.putContent(documentContent);
        ContentReader contentReader = fileFolderService.getReader(textDocument);

        NodeRef document = fileFolderService.create(rootNodeRef, documentName, ContentModel.TYPE_CONTENT).getNodeRef();
        contentWriter = fileFolderService.getWriter(document);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(mimetype);
        contentWriter.setLocale(Locale.ENGLISH);

        if (contentService.isTransformable(contentReader, contentWriter))
        {
            contentService.transform(contentReader, contentWriter);
        }

        fileFolderService.delete(textDocument);

        return document;
    }

    private void assertRendiions(List<CMISRendition> receivedRenditions, List<CMISRendition> expectedRenditions)
    {
        assertNotNull(receivedRenditions);
        expectedRenditions = removeReplication(expectedRenditions);
        assertTrue(receivedRenditions.size() == expectedRenditions.size());

        for (CMISRendition rendition : receivedRenditions)
        {
            assertNotNull(rendition);
            assertNotNull(rendition.getStreamId());
        }

        Collections.sort(receivedRenditions, renditionsComparator);
        Collections.sort(expectedRenditions, renditionsComparator);

        for (int i = 0; i < expectedRenditions.size(); ++i)
        {
            assertRendition(receivedRenditions.get(i), expectedRenditions.get(i));
        }
    }

    private void assertRendition(CMISRendition receivedRendition, CMISRendition expectedRendition)
    {
        assertEquals(expectedRendition.getStreamId(), receivedRendition.getStreamId());
        assertEquals(expectedRendition.getKind(), receivedRendition.getKind());
        assertEquals(expectedRendition.getMimeType(), receivedRendition.getMimeType());
        assertEquals(expectedRendition.getWidth(), receivedRendition.getWidth());
        assertEquals(expectedRendition.getHeight(), receivedRendition.getHeight());
    }

    private List<CMISRendition> removeReplication(List<CMISRendition> renditions)
    {
        return new ArrayList<CMISRendition>(new HashSet<CMISRendition>(renditions));
    }

    private Comparator<CMISRendition> renditionsComparator = new Comparator<CMISRendition>()
    {
        public int compare(CMISRendition rendition1, CMISRendition rendition2)
        {
            return rendition1.getStreamId().compareTo(rendition2.getStreamId());
        }
    };

    private List<CMISRendition> getRenditionsByKind(CMISRenditionKind kind)
    {
        return getRenditions(kind, null);
    }

    private List<CMISRendition> getRenditionsByMimetype(String mimetype)
    {
        return getRenditions(null, mimetype);
    }

    private List<CMISRendition> getRenditions(CMISRenditionKind kind, String mimetype)
    {
        List<CMISRendition> result = new ArrayList<CMISRendition>();

        List<CMISRendition> allRenditions = new ArrayList<CMISRendition>(documentRenditions);
        allRenditions.add(icon16Rendition);
        allRenditions.add(icon32Rendition);
        for (CMISRendition rendition : allRenditions)
        {
            if (isRenditionSatisfyConditions(rendition, kind, mimetype))
            {
                result.add(rendition);
            }
        }
        return result;
    }

    private boolean isRenditionSatisfyConditions(CMISRendition rendition, CMISRenditionKind kind, String mimetype)
    {
        if (kind != null)
        {
            if (!kind.equals(rendition.getKind()))
            {
                return false;
            }
        }
        if (mimetype != null)
        {
            if (mimetype.endsWith("/*"))
            {
                String baseMimetype = getBaseType(mimetype);
                if (!rendition.getMimeType().startsWith(baseMimetype))
                {
                    return false;
                }
            }
            else if (!mimetype.equals(rendition.getMimeType()))
            {
                return false;
            }
        }
        return true;
    }

    private String[] getDifferentMimetypes()
    {
        List<CMISRendition> allRenditions = new ArrayList<CMISRendition>(documentRenditions);
        allRenditions.add(icon16Rendition);
        allRenditions.add(icon32Rendition);
        Set<String> mimetypes = new HashSet<String>();
        for (CMISRendition rendition : allRenditions)
        {
            mimetypes.add(rendition.getMimeType());
        }
        return mimetypes.toArray(new String[0]);
    }

    private String getBaseType(String mimetype)
    {
        String baseMymetype = mimetype;
        int subTypeIndex = mimetype.indexOf("/");
        if (subTypeIndex > 0 || subTypeIndex < mimetype.length())
        {
            baseMymetype = mimetype.substring(0, subTypeIndex);
        }
        return baseMymetype;
    }

    private String createFilter(CMISRenditionKind[] kinds, String[] mimetypes)
    {
        StringBuilder filter = new StringBuilder();
        if (kinds != null)
        {
            for (CMISRenditionKind kind : kinds)
            {
                filter.append(kind.getLabel());
                filter.append(",");
            }
        }
        if (mimetypes != null)
        {
            for (String mimetype : mimetypes)
            {
                filter.append(mimetype);
                filter.append(",");
            }
        }
        filter.delete(filter.length() - 1, filter.length());
        return filter.toString();
    }

}
