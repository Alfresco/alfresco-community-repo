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

import org.alfresco.api.AlfrescoPublicApi;

import java.util.Map;

/**
 * Defines a rendition in terms of name, target mimetype and transform options need to get there.
 * {@link RenditionDefinition2}s need to be registered in a {@link RenditionDefinitionRegistry2}.
 *
 * @author adavis
 */
@AlfrescoPublicApi
public interface RenditionDefinition2
{
    public static final String TIMEOUT = "timeout";
    public static final String MAX_SOURCE_SIZE_K_BYTES = "maxSourceSizeKBytes";

    // ImageMagick options

    public static final String START_PAGE = "startPage";
    public static final String END_PAGE = "endPage";

    public static final String ALPHA_REMOVE = "alphaRemove";
    public static final String AUTO_ORIENT = "autoOrient";

    public static final String CROP_GRAVITY = "cropGravity";
    public static final String CROP_WIDTH = "cropWidth";
    public static final String CROP_HEIGHT = "cropHeight";
    public static final String CROP_PERCENTAGE = "cropPercentage";
    public static final String CROP_X_OFFSET = "cropXOffset";
    public static final String CROP_Y_OFFSET = "cropYOffset";

    /** Indicates whether to resize image to a thumbnail (true or false). */
    public static final String THUMBNAIL = "thumbnail";
    public static final String RESIZE_WIDTH = "resizeWidth";
    public static final String RESIZE_HEIGHT = "resizeHeight";
    public static final String RESIZE_PERCENTAGE = "resizePercentage";

    /** Indicates whether scaling operations should scale up or down (true or false). */
    public static final String ALLOW_ENLARGEMENT = "allowEnlargement";

    /** Indicates whether the aspect ratio of the image should be maintained (true or false). */
    public static final String MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";


    // PdfRenderer options

    public static final String PAGE = "page";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    /** Indicates whether scaling operations should scale up or down (true or false). */
    public static final String ALLOW_PDF_ENLARGEMENT = "allowPdfEnlargement";

    /** Indicates whether the aspect ratio of the image should be maintained (true or false). */
    public static final String MAINTAIN_PDF_ASPECT_RATIO = "maintainPdfAspectRatio";


    // Video options

    /** Time from start. Format hh:mm:ss[.xxx] */
    public static final String OFFSET = "offset";

    /** Duration of clip. */
    public static final String DURATION = "duration";

    /** Indicates if embedded content (such as files within zips or images) should be included (true or false). */
    public static final String INCLUDE_CONTENTS = "includeContents";

    /**
     * @deprecated Will be removed when local transformations are removed, as it is only used tp select
     * SWIFTransformationOptions.
     */
    @Deprecated
    String FLASH_VERSION = "flashVersion";

    /**
     * The encoding of a Source Node is automatically added to the Transform Options if not specified.
     */
    public static final String SOURCE_ENCODING = "sourceEncoding";

    /**
     * The Source Node Ref is automatically added to the Transform Options if specified but without a value.
     */
    public static final String SOURCE_NODE_REF = "sourceNodeRef";

    /**
     * The encoding of a Target Node is automatically added to the Transform Options if not specified and the
     * transformer knows about it.
     */
    public static final String TARGET_ENCODING = "targetEncoding";

    String getRenditionName();

    String getTargetMimetype();

    Map<String, String> getTransformOptions();
}
