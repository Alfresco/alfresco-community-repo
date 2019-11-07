/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.Map;

/**
 * Request synchronous transforms. Used in refactoring deprecated code, which called Legacy transforms, so that it will
 * first try a Local transform, falling back to Legacy if not available.
 *
 * @author adavis
 */
@Deprecated
public interface SynchronousTransformClient
{
    /**
     * Works out if it is possible to transform content of a given source mimetype and size into a
     * target mimetype given a list of actual transform option names and values (Strings) plus the data contained in the
     * Transformer objects registered with this class.
     *
     * @param sourceNodeRef the NodeRef of the source content. Optional as it is only used in debug messages.
     * @param sourceMimetype the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if negative.
     * @param contentUrl The url of the content. Optional as it is only used in debug messages.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     * results to avoid having to work out if a given transformation is supported a second time. The sourceMimetype
     * and sourceSizeInBytes may still change. In the case of ACS this is the rendition name.
     * @return {@code}true{@code} if it is supported.
     */
    @Deprecated
    boolean isSupported(NodeRef sourceNodeRef, String sourceMimetype, long sourceSizeInBytes, String contentUrl,
                        String targetMimetype,  Map<String, String> actualOptions, String transformName);

    /**
     * Helper method to call {@link #isSupported(NodeRef, String, long, String, String, Map, String)}. Uses the
     * {@code nodeService} and {@code sourceNodeRef} to work out the {@code sourceMimetype}, {@code sourceSizeInBytes}
     * and {@code contentUrl}.
     * @param sourceNodeRef the NodeRef of the source content.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     * results to avoid having to work out if a given transformation is supported a second time. The sourceMimetype
     * and sourceSizeInBytes may still change. In the case of ACS this is the rendition name.
     * @param nodeService to access the sourceNodeRef content property.
     * @return {@code}true{@code} if it is supported.
     */
    @Deprecated
    default boolean isSupported(NodeRef sourceNodeRef, String targetMimetype,
                                Map<String, String> actualOptions, String transformName, NodeService nodeService)
    {
        boolean supported = false;
        ContentData contentData = (ContentData) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CONTENT);
        if (contentData != null && contentData.getContentUrl() != null)
        {
            String sourceMimetype = contentData.getMimetype();
            long sourceSizeInBytes = contentData.getSize();
            String contentUrl = contentData.getContentUrl();
            supported = isSupported(sourceNodeRef, sourceMimetype, sourceSizeInBytes, contentUrl,
                    targetMimetype, actualOptions, transformName);
        }
        return supported;
    }

    /**
     * Requests a synchronous transform. Not used for renditions.
     * The call to this method <b>MUST</b> be proceeded by a successful call to
     * {@link #isSupported(NodeRef, String, long, String, String, Map, String)} in the <b>SAME</b> Thread.
     * @param reader of the source content
     * @param writer to the target node's content
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     * results to avoid having to work out if a given transformation is supported a second time. The sourceMimetype
     * and sourceSizeInBytes may still change. In the case of ACS this is the rendition name.
     * @param sourceNodeRef the source node
     */
    @Deprecated
    void transform(ContentReader reader, ContentWriter writer, Map<String, String> actualOptions,
                   String transformName, NodeRef sourceNodeRef) throws Exception;
}
