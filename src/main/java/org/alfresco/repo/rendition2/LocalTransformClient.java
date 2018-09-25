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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CropSourceOptions;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TemporalSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.alfresco.repo.rendition2.RenditionDefinition2.ALLOW_ENLARGEMENT;
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
import static org.alfresco.repo.rendition2.RenditionDefinition2.OFFSET;
import static org.alfresco.repo.rendition2.RenditionDefinition2.PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_HEIGHT;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_PERCENTAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.RESIZE_WIDTH;
import static org.alfresco.repo.rendition2.RenditionDefinition2.START_PAGE;
import static org.alfresco.repo.rendition2.RenditionDefinition2.THUMBNAIL;
import static org.alfresco.repo.rendition2.RenditionDefinition2.WIDTH;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_MAX_PAGES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_PAGE_LIMIT;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS;
import static org.alfresco.service.cmr.repository.TransformationOptionLimits.OPT_TIMEOUT_MS;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * Requests rendition transforms take place using transforms available on the local machine. The transform and
 * consumption of the resulting content is linked into a single operation that will take place at some point in
 * the future on the local machine.
 *
 * @author adavis
 */
public class LocalTransformClient extends AbstractTransformClient implements TransformClient
{
    private static Log logger = LogFactory.getLog(LocalTransformClient.class);

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
        WIDTH, HEIGHT,
        THUMBNAIL, RESIZE_WIDTH, RESIZE_HEIGHT, RESIZE_PERCENTAGE,
        ALLOW_ENLARGEMENT, MAINTAIN_ASPECT_RATIO
    }));

    private static Set<String> IMAGE_OPTIONS = new HashSet<>();
    static
    {
        IMAGE_OPTIONS.addAll(PAGED_OPTIONS);
        IMAGE_OPTIONS.addAll(CROP_OPTIONS);
        IMAGE_OPTIONS.addAll(TEMPORAL_OPTIONS);
        IMAGE_OPTIONS.addAll(RESIZE_OPTIONS);
    }

    private static Set<String> PDF_OPTIONS = new HashSet<>(Arrays.asList(new String[]
        {
            PAGE, WIDTH, HEIGHT, ALLOW_ENLARGEMENT, MAINTAIN_ASPECT_RATIO
        }));

    private static Set<String> FLASH_OPTIONS = new HashSet<>(Arrays.asList(new String[]
    {
        FLASH_VERSION
    }));

    private static Set<String> LIMIT_OPTIONS = new HashSet<>(Arrays.asList(new String[]
    {
        OPT_TIMEOUT_MS, OPT_READ_LIMIT_TIME_MS,
        OPT_MAX_SOURCE_SIZE_K_BYTES, OPT_READ_LIMIT_K_BYTES,
        OPT_MAX_PAGES, OPT_PAGE_LIMIT
    }));

    private TransactionService transactionService;

    private ContentService contentService;

    private RenditionService2Impl renditionService2;

    private ExecutorService executorService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setRenditionService2(RenditionService2Impl renditionService2)
    {
        this.renditionService2 = renditionService2;
    }

    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "renditionService2", renditionService2);
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
    }

    @Override
    public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype, long size, String contentUrl)
    {
        String targetMimetype = renditionDefinition.getTargetMimetype();
        String renditionName = renditionDefinition.getRenditionName();
        Map<String, String> options = renditionDefinition.getTransformOptions();

        TransformationOptions transformationOptions = getTransformationOptions(renditionName, options);
        transformationOptions.setSourceNodeRef(sourceNodeRef);

        ContentTransformer transformer = contentService.getTransformer(contentUrl, sourceMimetype, size, targetMimetype, transformationOptions);
        if (transformer == null)
        {
            String message = "Unsupported rendition " + renditionName + " from " + sourceMimetype + " size: " + size;
            logger.debug(message);
            throw new UnsupportedOperationException(message);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Rendition of " + renditionName + " from " + sourceMimetype + " will use " + transformer.getName());
        }
    }

    @Override
    public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentUrlHashCode)
    {
        executorService.submit(() ->
        {
            AuthenticationUtil.runAs((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    try
                    {
                        String targetMimetype = renditionDefinition.getTargetMimetype();
                        String renditionName = renditionDefinition.getRenditionName();
                        Map<String, String> options = renditionDefinition.getTransformOptions();

                        TransformationOptions transformationOptions = getTransformationOptions(renditionName, options);
                        transformationOptions.setSourceNodeRef(sourceNodeRef);

                        ContentReader reader = LocalTransformClient.this.contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
                        if (null == reader || !reader.exists())
                        {
                            throw new IllegalArgumentException("The supplied sourceNodeRef "+sourceNodeRef+" has no content.");
                        }

                        ContentWriter writer = contentService.getTempWriter();
                        writer.setMimetype(targetMimetype);
                        contentService.transform(reader, writer, transformationOptions);

                        InputStream inputStream = writer.getReader().getContentInputStream();
                        renditionService2.consume(sourceNodeRef, inputStream, renditionDefinition, sourceContentUrlHashCode);
                    }
                    catch (Exception e)
                    {
                        if (logger.isDebugEnabled())
                        {
                            String renditionName = renditionDefinition.getRenditionName();
                            logger.debug("Rendition of "+renditionName+" failed", e);
                        }
                        renditionService2.failure(sourceNodeRef, renditionDefinition, sourceContentUrlHashCode);
                        throw e;
                    }
                    return null;
                }), user);
        });
    }

    /**
     * @deprecated as we do not plan to use TransformationOptions moving forwards as local transformations will also
     * use the same options as the Transform Service.
     */
    @Deprecated
    static TransformationOptions getTransformationOptions(String renditionName, Map<String, String> options)
    {
        TransformationOptions transformationOptions = null;
        Set<String> optionNames = options.keySet();

        Set<String> subclassOptionNames = new HashSet<>(optionNames);
        subclassOptionNames.removeAll(LIMIT_OPTIONS);
        subclassOptionNames.remove(INCLUDE_CONTENTS);
        if (!subclassOptionNames.isEmpty())
        {
            if (FLASH_OPTIONS.containsAll(subclassOptionNames))
            {
                SWFTransformationOptions opts = new SWFTransformationOptions();
                transformationOptions = opts;
                opts.setFlashVersion(options.get(FLASH_VERSION));
            }
            else if (IMAGE_OPTIONS.containsAll(subclassOptionNames) ||  PDF_OPTIONS.containsAll(subclassOptionNames))
            {
                ImageTransformationOptions opts = new ImageTransformationOptions();
                transformationOptions = opts;

                if (containsAny(subclassOptionNames, RESIZE_OPTIONS))
                {
                    ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
                    opts.setResizeOptions(imageResizeOptions);
                    ifSet(options, WIDTH, (v) -> imageResizeOptions.setWidth(Integer.parseInt(v)));
                    ifSet(options, RESIZE_WIDTH, (v) -> imageResizeOptions.setWidth(Integer.parseInt(v)));
                    ifSet(options, HEIGHT, (v) -> imageResizeOptions.setHeight(Integer.parseInt(v)));
                    ifSet(options, RESIZE_HEIGHT, (v) -> imageResizeOptions.setHeight(Integer.parseInt(v)));
                    ifSet(options, THUMBNAIL, (v) ->imageResizeOptions.setResizeToThumbnail(Boolean.parseBoolean(v)));
                    ifSet(options, RESIZE_PERCENTAGE, (v) ->imageResizeOptions.setPercentResize(Boolean.parseBoolean(v)));
                    ifSet(options, ALLOW_ENLARGEMENT, (v) ->imageResizeOptions.setAllowEnlargement(Boolean.parseBoolean(v)));
                    ifSet(options, MAINTAIN_ASPECT_RATIO, (v) ->imageResizeOptions.setMaintainAspectRatio(Boolean.parseBoolean(v)));
                }

                ifSet(options, AUTO_ORIENT, (v) ->opts.setAutoOrient(Boolean.parseBoolean(v)));

                boolean containsPaged = containsAny(subclassOptionNames, PAGED_OPTIONS);
                boolean containsCrop = containsAny(subclassOptionNames, CROP_OPTIONS);
                boolean containsTemporal = containsAny(subclassOptionNames, TEMPORAL_OPTIONS);
                if (containsPaged || containsCrop || containsTemporal)
                {
                    List<TransformationSourceOptions> sourceOptionsList = new ArrayList<>();
                    opts.setSourceOptionsList(sourceOptionsList);
                    if (containsPaged)
                    {
                        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
                        sourceOptionsList.add(pagedSourceOptions);
                        ifSet(options, START_PAGE, (v) -> pagedSourceOptions.setStartPageNumber(Integer.parseInt(v)));
                        ifSet(options, END_PAGE, (v) -> pagedSourceOptions.setEndPageNumber(Integer.parseInt(v)));
                        ifSet(options, PAGE, (v) ->
                        {
                            int i = Integer.parseInt(v);
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
                }
            }
        }

        if (transformationOptions == null)
        {
            StringJoiner sj = new StringJoiner("\n    ");
            sj.add("The RenditionDefinition2 "+renditionName +
                " contains options that cannot be mapped to TransformationOptions used by local transformers");
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
            ifSet(options, OPT_TIMEOUT_MS, (v) -> limits.setTimeoutMs(Long.parseLong(v)));
            ifSet(options, OPT_READ_LIMIT_TIME_MS, (v) -> limits.setReadLimitTimeMs(Long.parseLong(v)));
            ifSet(options, OPT_MAX_PAGES, (v) -> limits.setMaxPages(Integer.parseInt(v)));
            ifSet(options, OPT_PAGE_LIMIT, (v) -> limits.setPageLimit(Integer.parseInt(v)));
            ifSet(options, OPT_MAX_SOURCE_SIZE_K_BYTES, (v) -> limits.setMaxSourceSizeKBytes(Long.parseLong(v)));
            ifSet(options, OPT_READ_LIMIT_K_BYTES, (v) -> limits.setReadLimitKBytes(Long.parseLong(v)));
        }

        transformationOptions.setUse(renditionName);
        return transformationOptions;
    }

    private interface Setter {
        void set(String s);
    }

    private static <T> void ifSet(Map<String, String> options, String key, Setter setter)
    {
        String value = options.get(key);
        if (value != null)
        {
            setter.set(value);
        }
    }
}
