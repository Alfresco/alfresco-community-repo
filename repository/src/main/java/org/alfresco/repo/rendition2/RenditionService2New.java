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

import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

public interface RenditionService2New
{
    /**
     * @return the {@link RenditionDefinitionRegistry2} being used by the service.
     */
    RenditionDefinitionRegistry2 getRenditionDefinitionRegistry2();

    /**
     * This method asynchronously transforms content to a target mimetype with transform options supplied in the
     * {@code transformDefinition}. A response is set on a message queue once the transform is complete or fails,
     * together with some client supplied data. The response queue and client data are also included in the
     * transformDefinition.<p>
     *
     * This method does not create a rendition node, but uses the same code as renditions to perform the transform. The
     * {@code transformDefinition} extends {@link RenditionDefinition2}, but is not stored in a
     * {@link RenditionDefinitionRegistry2}, as it is transient in nature.
     *
     * @param sourceNodeRef the node from which the content is retrieved.
     * @param transformDefinition which defines the transform, where to sent the response and some client specified data.
     * @throws UnsupportedOperationException if the transform is not supported.
     */
    @NotAuditable
    public void transform(NodeRef sourceNodeRef, TransformDefinition transformDefinition);

    /**
     * This method asynchronously renders content as specified by the {@code renditionName}. The content to be
     * rendered is provided by {@code sourceNodeRef}.
     *
     * @param sourceNodeRef the node from which the content is retrieved.
     * @param renditionName the rendition to be performed.
     * @throws UnsupportedOperationException if the transform is not supported AND the rendition has not been created before.
     */
    @NotAuditable
    public void render(NodeRef sourceNodeRef, String renditionName);

    /**
     * This method gets all the renditions of the {@code sourceNodeRef}.
     *
     * @return a list of {@link ChildAssociationRef}s which link the {@code sourceNodeRef} to the renditions.
     */
    @NotAuditable List<RenditionContentData> getRenditions(NodeRef sourceNodeRef);

    /**
     * This method gets the rendition of the {@code sourceNodeRef} identified by its name.
     *
     * @param sourceNodeRef the source node for the renditions
     * @param renditionName the renditionName used to identify a rendition.
     * @return the {@link ChildAssociationRef} which links the source node to the
     *         rendition or <code>null</code> if there is no rendition or it is not up to date.
     */
    @NotAuditable
    RenditionContentData getRenditionByName(NodeRef sourceNodeRef, String renditionName);

    /**
     * Indicates if renditions are enabled. Set using the {@code system.thumbnail.generate} value.
     */
    boolean isEnabled();

    /**
     * Indicates if storing renditions as property are enabled. Set using the {@code rendition.service.storeasproperty} value.
     */
    boolean isStoreRenditionAsPropertyEnabled();
}
