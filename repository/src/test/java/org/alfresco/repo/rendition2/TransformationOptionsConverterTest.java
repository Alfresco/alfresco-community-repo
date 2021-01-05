/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.service.cmr.repository.CropSourceOptions;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_IMAGE_JPEG;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_IMAGE_PNG;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TransformationOptionsConverterTest
{
    /**
     * The toString of a basic TransformationOptions. Used in testing where the conversion back to the original does not
     * result in the same Java sub class, as there are no meaningful properties set.
     */
    private static final String TO_STRING_OF_UNSET_TRANSFORMATION_OPTIONS = "{maxSourceSizeKBytes=-1, pageLimit=-1, use=null, timeoutMs=-1, " +
            "maxPages=-1, contentReaderNodeRef=null, sourceContentProperty=null, readLimitKBytes=-1, " +
            "contentWriterNodeRef=null, targetContentProperty=null, includeEmbedded=null, readLimitTimeMs=-1}";

    private TransformationOptionsConverter converter;

    private static final String[] DISCARD_OPTIONS = new String[]
            {
                "autoOrient", "false",
                "maintainAspectRatio", "true",
                "allowEnlargement", "true"
            };

    @Before
    public void setUp() throws Exception
    {
        converter = new TransformationOptionsConverter();
        converter.setMaxSourceSizeKBytes("-1");
        converter.setReadLimitTimeMs("-1");
        converter.setReadLimitKBytes("-1");
        converter.setPageLimit("-1");
        converter.setMaxPages("-1");
    }

    private void assertConverterToMapAndBack(TransformationOptions oldOptions, String sourceMimetype,
                                             String targetMimetype, String expectedOldOptionsToString,
                                             String expectedArgs)
    {
        String sortedOldOptions = getSortedOptions(oldOptions, sourceMimetype, targetMimetype);
        assertEquals("oldOptions was not set up correctly", expectedOldOptionsToString, oldOptions.toString());

        Map<String, String> newOptions = converter.getOptions(oldOptions, sourceMimetype, targetMimetype);
        Map<String, String> newOptionsWithoutDiscards = discardNoopOptions(newOptions);
        String sortedNewOptions = getSortedOptions(newOptionsWithoutDiscards);
        assertEquals("Conversion to a map appears to be wrong", expectedArgs, sortedNewOptions);

        TransformationOptions backToOldOptions = converter.getTransformationOptions("null", newOptions);
        String sortedBackToOldOptions = getSortedOptions(backToOldOptions, sourceMimetype, targetMimetype);

        assertEquals("Having converted twice the toString is different", expectedOldOptionsToString, backToOldOptions.toString());
        assertEquals("Having converted twice the map is different", sortedOldOptions, sortedBackToOldOptions);
    }

    private String getSortedOptions(TransformationOptions options, String sourceMimetype, String targetMimetype)
    {
        Map<String, String> map = converter.getOptions(options, sourceMimetype, targetMimetype);
        return getSortedOptions(map);
    }

    private static String getSortedOptions(Map<String, String> options)
    {
        final List<String> list = new ArrayList<>();
        options.entrySet().forEach(e->
        {
            if (e.getValue() != null)
            {
                list.add(e.getKey() + '=' + e.getValue() + ' ');
            }
        });
        return getSortedOptions(list);
    }

    public static Map<String, String> discardNoopOptions(Map<String, String> options1)
    {
        // Discards options that are ignored by the transformer if passed in.
        Map<String, String> options2 = new HashMap<>(options1);
        for (int i = 0; i < DISCARD_OPTIONS.length; i+=2)
        {
            options2.remove(DISCARD_OPTIONS[i], DISCARD_OPTIONS[i+1]);
        }
        return options2;
    }

    private static String getSortedOptions(String[] args)
    {
        final List<String> list = new ArrayList<>();
        for (int i=0; i<args.length; i+=2)
        {
            if (args[i+1] != null)
            {
                list.add(args[i] + "=" + args[i + 1] + ' ');
            }
        }
        return getSortedOptions(list);
    }

    private static String getSortedOptions(List<String> list)
    {
        StringBuilder sb = new StringBuilder();
        Collections.sort(list);
        list.forEach(a->sb.append(a));
        return sb.toString();
    }

    @Test
    public void testCompositeReformatAndResizeRendition()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setHeight(30);
        imageResizeOptions.setWidth(20);
        oldOptions.setResizeOptions(imageResizeOptions);
        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
        pagedSourceOptions.setStartPageNumber(1);
        pagedSourceOptions.setEndPageNumber(1);
        oldOptions.addSourceOptions(pagedSourceOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_JPEG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=" +
                        "ImageResizeOptions [width=20, height=30, maintainAspectRatio=true, percentResize=false, " +
                        "resizeToThumbnail=false, allowEnlargement=true], autoOrient=true], " +
                        "sourceOptions={ PagedSourceOptionsPagedSourceOptions {1, 1}} ]",
                "alphaRemove=true " +
                        "autoOrient=true " +
                        "endPage=0 " +
                        "resizeHeight=30 " +
                        "resizeWidth=20 " +
                        "startPage=0 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptions()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=true]]",
                "autoOrient=true " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsNoAutoOrient()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false]]",
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsAlphaRemove()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);

        // The target mimetype is JPEG, which sets the alphaRemove
        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_JPEG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false]]",
                "alphaRemove=true " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsCrop()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        CropSourceOptions cropOptions = new CropSourceOptions();
        oldOptions.addSourceOptions(cropOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false], " +
                        "sourceOptions={ CropSourceOptionsCropSourceOptions " +
                        "[height=-1, width=-1, xOffset=0, yOffset=0, isPercentageCrop=false, gravity=null]} ]",
                "cropXOffset=0 " +
                        "cropYOffset=0 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsCropGravity()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        CropSourceOptions cropOptions = new CropSourceOptions();
        cropOptions.setGravity("North");
        oldOptions.addSourceOptions(cropOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false], " +
                        "sourceOptions={ CropSourceOptionsCropSourceOptions " +
                        "[height=-1, width=-1, xOffset=0, yOffset=0, isPercentageCrop=false, gravity=North]} ]",
                "cropGravity=North " +
                        "cropXOffset=0 " +
                        "cropYOffset=0 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsCropWidthHeight()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        CropSourceOptions cropOptions = new CropSourceOptions();
        cropOptions.setWidth(30);
        cropOptions.setHeight(48);
        oldOptions.addSourceOptions(cropOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false], " +
                        "sourceOptions={ CropSourceOptionsCropSourceOptions " +
                        "[height=48, width=30, xOffset=0, yOffset=0, isPercentageCrop=false, gravity=null]} ]",
                "cropHeight=48 " +
                        "cropWidth=30 " +
                        "cropXOffset=0 " +
                        "cropYOffset=0 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsCropPercentage()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        CropSourceOptions cropOptions = new CropSourceOptions();
        cropOptions.setPercentageCrop(true);
        oldOptions.addSourceOptions(cropOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false], " +
                        "sourceOptions={ CropSourceOptionsCropSourceOptions " +
                        "[height=-1, width=-1, xOffset=0, yOffset=0, isPercentageCrop=true, gravity=null]} ]",
                "cropPercentage=true " +
                        "cropXOffset=0 " +
                        "cropYOffset=0 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsCropOffset()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        CropSourceOptions cropOptions = new CropSourceOptions();
        cropOptions.setXOffset(20);
        cropOptions.setYOffset(59);
        oldOptions.addSourceOptions(cropOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, resizeOptions=null, autoOrient=false], " +
                        "sourceOptions={ CropSourceOptionsCropSourceOptions " +
                        "[height=-1, width=-1, xOffset=20, yOffset=59, isPercentageCrop=false, gravity=null]} ]",
                "cropXOffset=20 " +
                        "cropYOffset=59 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResize()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=-1, height=-1, maintainAspectRatio=true, " +
                        "percentResize=false, resizeToThumbnail=false, allowEnlargement=true], autoOrient=false]]",
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResizeNoEnlargement()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(false);
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=-1, height=-1, maintainAspectRatio=true, " +
                        "percentResize=false, resizeToThumbnail=false, allowEnlargement=false], autoOrient=false]]",
                "allowEnlargement=false " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResizeNoAspectRatio()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setMaintainAspectRatio(false);
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=-1, height=-1, maintainAspectRatio=false, " +
                        "percentResize=false, resizeToThumbnail=false, allowEnlargement=true], autoOrient=false]]",
                "maintainAspectRatio=false " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResizeNoEnlargementOrAspectRatio()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(false);
        imageResizeOptions.setMaintainAspectRatio(false);
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=-1, height=-1, maintainAspectRatio=false, " +
                        "percentResize=false, resizeToThumbnail=false, allowEnlargement=false], autoOrient=false]]",
                "allowEnlargement=false " +
                        "maintainAspectRatio=false " +
                "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResizeThumbnnail()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(false);
        imageResizeOptions.setMaintainAspectRatio(false);
        imageResizeOptions.setResizeToThumbnail(true);
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=-1, height=-1, maintainAspectRatio=false, " +
                        "percentResize=false, resizeToThumbnail=true, allowEnlargement=false], autoOrient=false]]",
                "allowEnlargement=false " +
                        "maintainAspectRatio=false " +
                        "thumbnail=true " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResizeWidthHeight()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(false);
        imageResizeOptions.setMaintainAspectRatio(false);
        imageResizeOptions.setWidth(18);
        imageResizeOptions.setHeight(15);
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=18, height=15, maintainAspectRatio=false, " +
                        "percentResize=false, resizeToThumbnail=false, allowEnlargement=false], autoOrient=false]]",
                "allowEnlargement=false " +
                        "maintainAspectRatio=false " +
                        "resizeHeight=15 " +
                        "resizeWidth=18 " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsResizePercent()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(false);
        imageResizeOptions.setMaintainAspectRatio(false);
        imageResizeOptions.setPercentResize(true);
        oldOptions.setResizeOptions(imageResizeOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=-1, height=-1, maintainAspectRatio=false, " +
                        "percentResize=true, resizeToThumbnail=false, allowEnlargement=false], autoOrient=false]]",
                "allowEnlargement=false " +
                        "maintainAspectRatio=false " +
                        "resizePercentage=true " +
                        "timeout=-1 "
        );
    }

    @Test
    // Checks we do what was in the legacy ImageMagickContentTransformerWorker
    public void testImageTransformationOptionsPage()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        oldOptions.setAutoOrient(false);
        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
        pagedSourceOptions.setStartPageNumber(1);
        pagedSourceOptions.setEndPageNumber(1);
        oldOptions.addSourceOptions(pagedSourceOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=null, autoOrient=false], " +
                        "sourceOptions={ PagedSourceOptionsPagedSourceOptions {1, 1}} ]",
                "endPage=0 startPage=0 " +
                        "timeout=-1 "
        );
    }

    @Test
    // The converter does handle SWFTransformationOptions, but the only use of these is the webpreview rendition that
    // is no longer used as there are o transformers for it.
    public void testSWFTransformationOptionsPage()
    {
        SWFTransformationOptions oldOptions = new SWFTransformationOptions();
        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
        pagedSourceOptions.setStartPageNumber(1);
        pagedSourceOptions.setEndPageNumber(1);
        oldOptions.addSourceOptions(pagedSourceOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "{maxSourceSizeKBytes=-1, use=null, contentReaderNodeRef=null, readLimitKBytes=-1, " +
                        "contentWriterNodeRef=null, pageLimit=-1, flashVersion=9, timeoutMs=-1, maxPages=-1, " +
                        "sourceContentProperty=null, targetContentProperty=null, includeEmbedded=null, readLimitTimeMs=-1}",
                "flashVersion=9 " +
                        "timeout=-1 "
        ); // SWFTransformationOptions are not used by ImageMagickContentTransformerWorker
    }

    @Test
    // Check conversion to TransformationOptions from options found in rendition definitions
    public void testRenditionImagepreview()
    {
        String sourceMimetype = null;
        String targetMimetype = null;

        Map<String, String> newOptions = new HashMap<>();
        newOptions.put("thumbnail", "true");
        newOptions.put("resizeWidth","960");
        newOptions.put("autoOrient", "true");
        newOptions.put("resizeHeight", "960");
        newOptions.put("allowEnlargement", "true");
        newOptions.put("maintainAspectRatio", "true");
        String sortedNewOptions = getSortedOptions(newOptions);

        TransformationOptions oldOptions = converter.getTransformationOptions("null", newOptions);
        String sortedOldOptions = getSortedOptions(oldOptions, sourceMimetype, targetMimetype);
        if (sortedOldOptions.endsWith("timeout=-1 "))
        {
            sortedOldOptions = sortedOldOptions.substring(0, sortedOldOptions.length()-"timeout=-1 ".length());
        }

        assertEquals("Maps are different", sortedNewOptions, sortedOldOptions);
    }

    // ImageTransformationOptions [commandOptions=, resizeOptions=ImageResizeOptions [width=20, height=30, maintainAspectRatio=true, percentResize=false,
    //                            resizeToThumbnail=false, allowEnlargement=true], autoOrient=true], sourceOptions={ PagedSourceOptionsPagedSourceOptions {1, 1}} ]
    @Test
    public void testResize()
    {
        ImageTransformationOptions oldOptions = new ImageTransformationOptions();
        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(false);
        imageResizeOptions.setWidth(20);
        imageResizeOptions.setHeight(30);
        oldOptions.setResizeOptions(imageResizeOptions);
        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
        pagedSourceOptions.setStartPageNumber(1);
        pagedSourceOptions.setEndPageNumber(1);
        oldOptions.addSourceOptions(pagedSourceOptions);

        assertConverterToMapAndBack(oldOptions, MIMETYPE_IMAGE_JPEG, MIMETYPE_IMAGE_PNG,
                "ImageTransformationOptions [commandOptions=, " +
                        "resizeOptions=ImageResizeOptions [width=20, height=30, maintainAspectRatio=true, " +
                        "percentResize=false, resizeToThumbnail=false, allowEnlargement=false], autoOrient=true], " +
                        "sourceOptions={ PagedSourceOptionsPagedSourceOptions {1, 1}} ]",
                "allowEnlargement=false " +
                        "autoOrient=true " +
                        "endPage=0 " +
                        "resizeHeight=30 " +
                        "resizeWidth=20 " +
                        "startPage=0 " +
                        "timeout=-1 "
        );
    }
}