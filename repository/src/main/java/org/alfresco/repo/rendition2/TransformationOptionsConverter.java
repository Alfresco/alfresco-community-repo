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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerOptions;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.service.cmr.repository.CropSourceOptions;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TemporalSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_PDF;
import static org.alfresco.repo.rendition2.RenditionDefinition2.ALLOW_ENLARGEMENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.ALLOW_PDF_ENLARGEMENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.ALPHA_REMOVE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.AUTO_ORIENT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_GRAVITY;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_X_OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.CROP_Y_OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.DURATION;
import static org.alfresco.repo.rendition2.RenditionDefinition2.END_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.FLASH_VERSION;
import static org.alfresco.repo.rendition2.RenditionDefinition2.HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.INCLUDE_CONTENTS;
import static org.alfresco.repo.rendition2.RenditionDefinition2.MAINTAIN_ASPECT_RATIO;
import static org.alfresco.repo.rendition2.RenditionDefinition2.MAINTAIN_PDF_ASPECT_RATIO;
import static org.alfresco.repo.rendition2.RenditionDefinition2.MAX_SOURCE_SIZE_K_BYTES;
import static org.alfresco.repo.rendition2.RenditionDefinition2.OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.START_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.THUMBNAIL;
import static org.alfresco.repo.rendition2.RenditionDefinition2.TIMEOUT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.WIDTH;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * @deprecated converts the new flat name value pair transformer options to the deprecated TransformationOptions.
 *
 * @author adavis
 */
@Deprecated
public class TransformationOptionsConverter implements InitializingBean
{
    public static final String FALSE_STRING = Boolean.FALSE.toString();
    private static Set<String> PAGED_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    PAGE, START_PAGE, END_PAGE
            }));

    private static Set<String> CROP_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    CROP_GRAVITY, CROP_WIDTH, CROP_HEIGHT, CROP_PERCENTAGE, CROP_X_OFFSET, CROP_Y_OFFSET
            }));

    private static Set<String> TEMPORAL_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    OFFSET, DURATION
            }));

    private static Set<String> RESIZE_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    WIDTH, HEIGHT, ALLOW_PDF_ENLARGEMENT, MAINTAIN_PDF_ASPECT_RATIO,

                    THUMBNAIL, RESIZE_WIDTH, RESIZE_HEIGHT, RESIZE_PERCENTAGE,
                    ALLOW_ENLARGEMENT, MAINTAIN_ASPECT_RATIO
            }));

    protected static Set<String> IMAGE_OPTIONS = new HashSet<>();
    static
    {
        IMAGE_OPTIONS.addAll(PAGED_OPTIONS);
        IMAGE_OPTIONS.addAll(CROP_OPTIONS);
        IMAGE_OPTIONS.addAll(TEMPORAL_OPTIONS);
        IMAGE_OPTIONS.addAll(RESIZE_OPTIONS);
        IMAGE_OPTIONS.add(AUTO_ORIENT);
        IMAGE_OPTIONS.add(ALPHA_REMOVE);
    }

    private static Set<String> PDF_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    PAGE, WIDTH, HEIGHT, ALLOW_PDF_ENLARGEMENT, MAINTAIN_PDF_ASPECT_RATIO
            }));

    private static Set<String> FLASH_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    FLASH_VERSION
            }));

    private static Set<String> LIMIT_OPTIONS = new HashSet<>(Arrays.asList(new String[]
            {
                    TIMEOUT, MAX_SOURCE_SIZE_K_BYTES
            }));

    private interface Setter
    {
        void set(String s);
    }

    private static Log logger = LogFactory.getLog(TransformationOptionsConverter.class);

    // The default valued in the old TransformationOptionsLimits
    private long maxSourceSizeKBytes;
    private long readLimitTimeMs;
    private long readLimitKBytes;
    private int pageLimit;
    private int maxPages;

    public void setMaxSourceSizeKBytes(String maxSourceSizeKBytes)
    {
        this.maxSourceSizeKBytes = Long.parseLong(maxSourceSizeKBytes);;
    }

    public void setReadLimitTimeMs(String readLimitTimeMs)
    {
        this.readLimitTimeMs = Long.parseLong(readLimitTimeMs);
    }

    public void setReadLimitKBytes(String readLimitKBytes)
    {
        this.readLimitKBytes = Long.parseLong(readLimitKBytes);
    }

    public void setPageLimit(String pageLimit)
    {
        this.pageLimit = Integer.parseInt(pageLimit);
    }

    public void setMaxPages(String maxPages)
    {
        this.maxPages = Integer.parseInt(maxPages);
    }

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "maxSourceSizeKBytes", maxSourceSizeKBytes);
        PropertyCheck.mandatory(this, "readLimitTimeMs", readLimitTimeMs);
        PropertyCheck.mandatory(this, "readLimitKBytes", readLimitKBytes);
        PropertyCheck.mandatory(this, "pageLimit", pageLimit);
        PropertyCheck.mandatory(this, "maxPages", maxPages);
    }

    /**
     * @deprecated as we do not plan to use TransformationOptions moving forwards as local transformations will also
     * use the same options as the Transform Service.
     */
    @Deprecated
    TransformationOptions getTransformationOptions(String renditionName, Map<String, String> options)
    {
        TransformationOptions transformationOptions = null;
        Set<String> optionNames = options.keySet();

        // The "pdf" rendition is special as it was incorrectly set up as an SWFTransformationOptions in 6.0
        // It should have been simply a TransformationOptions.
        boolean isPdfRendition = "pdf".equals(renditionName);

        Set<String> subclassOptionNames = new HashSet<>(optionNames);
        subclassOptionNames.removeAll(LIMIT_OPTIONS);
        subclassOptionNames.remove(INCLUDE_CONTENTS);
        boolean hasOptions = !subclassOptionNames.isEmpty();
        if (isPdfRendition || hasOptions)
        {
            // The "pdf" rendition used the wrong TransformationOptions subclass.
            if (isPdfRendition || FLASH_OPTIONS.containsAll(subclassOptionNames))
            {
                SWFTransformationOptions opts = new SWFTransformationOptions();
                transformationOptions = opts;
                opts.setFlashVersion(isPdfRendition ? "9" : options.get(FLASH_VERSION));
            }
            // Even though the only standard rendition to use the pdf-renderer is "pdf" there may be custom renditions
            // that use ImageTransformOptions to specify width, height etc.
            else if (IMAGE_OPTIONS.containsAll(subclassOptionNames) || PDF_OPTIONS.containsAll(subclassOptionNames))
            {
                ImageTransformationOptions opts = new ImageTransformationOptions();
                transformationOptions = opts;

                if (containsAny(subclassOptionNames, RESIZE_OPTIONS))
                {
                    ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
                    opts.setResizeOptions(imageResizeOptions);
                    // PDF
                    ifSet(options, WIDTH, (v) -> imageResizeOptions.setWidth(Integer.parseInt(v)));
                    ifSet(options, HEIGHT, (v) -> imageResizeOptions.setHeight(Integer.parseInt(v)));
                    // ImageMagick
                    ifSet(options, RESIZE_WIDTH, (v) -> imageResizeOptions.setWidth(Integer.parseInt(v)));
                    ifSet(options, RESIZE_HEIGHT, (v) -> imageResizeOptions.setHeight(Integer.parseInt(v)));
                    ifSet(options, THUMBNAIL, (v) ->imageResizeOptions.setResizeToThumbnail(Boolean.parseBoolean(v)));
                    ifSet(options, RESIZE_PERCENTAGE, (v) ->imageResizeOptions.setPercentResize(Boolean.parseBoolean(v)));
                    set(options, ALLOW_ENLARGEMENT, (v) ->imageResizeOptions.setAllowEnlargement(Boolean.parseBoolean(v == null ? "true" : v)));
                    set(options, MAINTAIN_ASPECT_RATIO, (v) ->imageResizeOptions.setMaintainAspectRatio(Boolean.parseBoolean(v == null ? "true" : v)));
                }

                // ALPHA_REMOVE can be ignored as it is automatically added in the legacy code if the sourceMimetype is jpeg
                set(options, AUTO_ORIENT, (v) ->opts.setAutoOrient(Boolean.parseBoolean(v == null ? "true" : v)));

                boolean containsPaged = containsAny(subclassOptionNames, PAGED_OPTIONS);
                boolean containsCrop = containsAny(subclassOptionNames, CROP_OPTIONS);
                boolean containsTemporal = containsAny(subclassOptionNames, TEMPORAL_OPTIONS);
                if (containsPaged || containsCrop || containsTemporal)
                {
                    List<TransformationSourceOptions> sourceOptionsList = new ArrayList<>();
                    if (containsPaged)
                    {
                        // The legacy transformer options start at page 1, where as image magick and the local
                        // transforms start at 0;
                        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
                        sourceOptionsList.add(pagedSourceOptions);
                        ifSet(options, START_PAGE, (v) -> pagedSourceOptions.setStartPageNumber(Integer.parseInt(v) + 1));
                        ifSet(options, END_PAGE, (v) -> pagedSourceOptions.setEndPageNumber(Integer.parseInt(v) + 1));
                        ifSet(options, PAGE, (v) ->
                        {
                            int i = Integer.parseInt(v) + 1;
                            pagedSourceOptions.setStartPageNumber(i);
                            pagedSourceOptions.setEndPageNumber(i);
                        });
                    }

                    if (containsCrop)
                    {
                        CropSourceOptions cropSourceOptions = new CropSourceOptions();
                        sourceOptionsList.add(cropSourceOptions);
                        ifSet(options, CROP_GRAVITY, (v) -> cropSourceOptions.setGravity(v));
                        ifSet(options, CROP_PERCENTAGE, (v) -> cropSourceOptions.setPercentageCrop(Boolean.parseBoolean(v)));
                        ifSet(options, CROP_WIDTH, (v) -> cropSourceOptions.setWidth(Integer.parseInt(v)));
                        ifSet(options, CROP_HEIGHT, (v) -> cropSourceOptions.setHeight(Integer.parseInt(v)));
                        ifSet(options, CROP_X_OFFSET, (v) -> cropSourceOptions.setXOffset(Integer.parseInt(v)));
                        ifSet(options, CROP_Y_OFFSET, (v) -> cropSourceOptions.setYOffset(Integer.parseInt(v)));
                    }

                    if (containsTemporal)
                    {
                        TemporalSourceOptions temporalSourceOptions = new TemporalSourceOptions();
                        sourceOptionsList.add(temporalSourceOptions);
                        ifSet(options, DURATION, (v) -> temporalSourceOptions.setDuration(v));
                        ifSet(options, OFFSET, (v) -> temporalSourceOptions.setOffset(v));
                    }
                    opts.setSourceOptionsList(sourceOptionsList);
                }
            }
        }
        else
        {
            // This what the "pdf" rendition should have used in 6.0 and it is not unreasonable for a custom transformer
            // and rendition to do the same.
            transformationOptions = new TransformationOptions();
        }

        if (transformationOptions == null)
        {
            StringJoiner sj = new StringJoiner("\n    ");
            sj.add("The RenditionDefinition2 "+renditionName +
                    " contains options that cannot be mapped to TransformationOptions used by local transformers. "+
                    " The TransformOptionConverter may need to be sub classed to support this conversion.");
            HashSet<String> otherNames = new HashSet<>(optionNames);
            otherNames.removeAll(FLASH_OPTIONS);
            otherNames.removeAll(IMAGE_OPTIONS);
            otherNames.removeAll(PDF_OPTIONS);
            otherNames.removeAll(LIMIT_OPTIONS);
            otherNames.forEach(sj::add);
            sj.add("---");
            optionNames.forEach(sj::add);
            throw new IllegalArgumentException(sj.toString());
        }

        final TransformationOptions opts = transformationOptions;
        ifSet(options, INCLUDE_CONTENTS, (v) ->opts.setIncludeEmbedded(Boolean.parseBoolean(v)));

        if (containsAny(optionNames, LIMIT_OPTIONS))
        {
            TransformationOptionLimits limits = new TransformationOptionLimits();
            transformationOptions.setLimits(limits);
            ifSet(options, TIMEOUT, (v) -> limits.setTimeoutMs(Long.parseLong(v)));
            limits.setMaxSourceSizeKBytes(maxSourceSizeKBytes);
            limits.setReadLimitKBytes(readLimitTimeMs);
            limits.setReadLimitTimeMs(readLimitKBytes);
            limits.setMaxPages(maxPages);
            limits.setPageLimit(pageLimit);
        }

        transformationOptions.setUse(renditionName);
        return transformationOptions;
    }

    protected <T> void set(Map<String, String> options, String key, TransformationOptionsConverter.Setter setter)
    {
        String value = options.get(key);
        setter.set(value);
    }

    protected <T> void ifSet(Map<String, String> options, String key, TransformationOptionsConverter.Setter setter)
    {
        String value = options.get(key);
        if (value != null)
        {
            setter.set(value);
        }
    }

    @Deprecated
    public Map<String, String> getOptions(TransformationOptions options)
    {
        return getOptions(options, null, null);
    }

    public Map<String, String> getOptions(TransformationOptions options, String sourceMimetype, String targetMimetype)
    {
        boolean sourceIsPdf = MIMETYPE_PDF.equals(sourceMimetype);
        Map<String, String> map = new HashMap<>();
        map.put(TIMEOUT, "-1");
        if (options != null)
        {
            if (options instanceof ImageTransformationOptions)
            {
                ImageTransformationOptions opts = (ImageTransformationOptions) options;

                // TODO We don't support this any more for security reasons, however it might be possible to
                // extract some of the well know values and add them to the newer ImageMagick transform options.
                String commandOptions = opts.getCommandOptions();
                if (commandOptions != null && !commandOptions.isBlank())
                {
                    logger.error("ImageMagick commandOptions are no longer supported for security reasons: " + commandOptions);
                }

                ImageResizeOptions imageResizeOptions = opts.getResizeOptions();
                if (imageResizeOptions != null)
                {
                    int width = imageResizeOptions.getWidth();
                    int height = imageResizeOptions.getHeight();
                    ifSet(width != -1, map, RESIZE_WIDTH, width);
                    ifSet(height != -1, map, RESIZE_HEIGHT, height);
                    ifSet(imageResizeOptions.isResizeToThumbnail(), map, THUMBNAIL, true);
                    ifSet(imageResizeOptions.isPercentResize(), map, RESIZE_PERCENTAGE, true);
                    map.put(ALLOW_ENLARGEMENT, Boolean.toString(imageResizeOptions.getAllowEnlargement()));
                    map.put(MAINTAIN_ASPECT_RATIO, Boolean.toString(imageResizeOptions.isMaintainAspectRatio()));
                }

                ifSet(MimetypeMap.MIMETYPE_IMAGE_JPEG.equalsIgnoreCase(targetMimetype), map, ALPHA_REMOVE, true);
                map.put(AUTO_ORIENT, Boolean.toString(opts.isAutoOrient()));

                Collection<TransformationSourceOptions> sourceOptionsList = opts.getSourceOptionsList();
                if (sourceOptionsList != null)
                {
                    for (TransformationSourceOptions transformationSourceOptions : sourceOptionsList)
                    {
                        if (transformationSourceOptions instanceof PagedSourceOptions)
                        {
                            PagedSourceOptions pagedSourceOptions = (PagedSourceOptions) transformationSourceOptions;

                            // The legacy transformer options start at page 1, where as image magick and the local
                            // transforms start at 0;
                            Integer startPageNumber = pagedSourceOptions.getStartPageNumber() - 1;
                            Integer endPageNumber = pagedSourceOptions.getEndPageNumber() - 1;
                            // PAGE is not an imagemagick option, but pdfRederer was incorrectly created initially using these options
                            if (startPageNumber == endPageNumber && sourceIsPdf)
                            {
                                map.put(PAGE, Integer.toString(startPageNumber));
                            }
                            else
                            {
                                map.put(START_PAGE, Integer.toString(startPageNumber));
                                map.put(END_PAGE, Integer.toString(endPageNumber));
                            }
                        }
                        else if (transformationSourceOptions instanceof CropSourceOptions)
                        {
                            CropSourceOptions cropSourceOptions = (CropSourceOptions) transformationSourceOptions;
                            String gravity = cropSourceOptions.getGravity();
                            boolean percentageCrop = cropSourceOptions.isPercentageCrop();
                            int height = cropSourceOptions.getHeight();
                            int width = cropSourceOptions.getWidth();
                            int xOffset = cropSourceOptions.getXOffset();
                            int yOffset = cropSourceOptions.getYOffset();
                            ifSet(gravity != null, map, CROP_GRAVITY, gravity);
                            ifSet(percentageCrop, map, CROP_PERCENTAGE, percentageCrop);
                            ifSet(width != -1, map, CROP_WIDTH, width);
                            ifSet(height != -1, map, CROP_HEIGHT, height);
                            map.put(CROP_X_OFFSET, Integer.toString(xOffset));
                            map.put(CROP_Y_OFFSET, Integer.toString(yOffset));
                        }
                        else if (transformationSourceOptions instanceof TemporalSourceOptions)
                        {
                            TemporalSourceOptions temporalSourceOptions = (TemporalSourceOptions) transformationSourceOptions;
                            String duration = temporalSourceOptions.getDuration();
                            String offset = temporalSourceOptions.getOffset();
                            ifSet(duration != null, map, DURATION, duration);
                            ifSet(offset != null, map, OFFSET, offset);
                        }
                        else
                        {
                            logger.error("TransformationOption sourceOptionsList contained a " +
                                    transformationSourceOptions.getClass().getName() +
                                    ". It is not know how to convert this into newer transform options.");
                        }
                    }
                }
            }
            else if (options instanceof SWFTransformationOptions)
            {
                SWFTransformationOptions opts = (SWFTransformationOptions) options;
                map.put(FLASH_VERSION, opts.getFlashVersion());
            }
            else if (options instanceof RuntimeExecutableContentTransformerOptions)
            {
                RuntimeExecutableContentTransformerOptions opts = (RuntimeExecutableContentTransformerOptions) options;
                map.putAll(opts.getPropertyValues());
            }
            else if (!options.getClass().equals(TransformationOptions.class))
            {
                throw new IllegalArgumentException("Unable to convert " +
                        options.getClass().getSimpleName() + " to new transform options held in a Map<String,String>.\n" +
                        "The TransformOptionConverter may need to be sub classed to support this conversion.");
            }
        }

        return map;
    }

    protected void ifSet(boolean condition, Map<String, String> options, String key, Object value)
    {
        if (condition)
        {
            options.put(key, value.toString());
        }
    }
}
