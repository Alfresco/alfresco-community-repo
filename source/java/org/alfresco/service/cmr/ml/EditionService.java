/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.ml;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;

/**
 * The API to manage editions of a mlContainer. An edition is a version of a <b>mlContainer</b>
 *
 * @since 2.1
 * @author Yannick Pignot
 */
@PublicService
public interface EditionService
{
    /**
     * Create a new edition of an existing <b>cm:mlContainer</b> using any one of the
     * associated <b>cm:mlDocument</b> transalations.
     *
     * If startingTranslationNodeRef is multilingual, it will be copied. The copy will become the pivot translation
     * of the new Edition of the <b>cm:mlContainer</b>. The reference of the copy will be returned.
     *
     * @param translationNodeRef        The specific <b>cm:mlDocument</b> to use as the starting point
     *                                  of the new edition.  All other translations will be removed.
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"translationNodeRef", "versionProperties"})
    NodeRef createEdition(NodeRef translationNodeRef, Map<String, Serializable> versionProperties);

    /**
     * Get editions of an existing <b>cm:mlContainer</b>.
     *
     * @param mlContainer               An existing <b>cm:mlContainer</b>
     * @return                          The Version History of the mlContainer
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainer"})
    VersionHistory getEditions(NodeRef mlContainer);

    /**
     * Get the different <b>cm:mlDocument</b> transalation version histories of a specific edition of a <b>cm:mlContainer</b>
     *
     * @param mlContainerEdition            An existing version of a mlContainer
     * @return                              The list of <b>cm:mlDocument</b> transalation versions of the edition
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"mlContainerEdition"})
    List<VersionHistory> getVersionedTranslations(Version mlContainerEdition);

    /**
     * Get the the versioned metadata of a specific <b>cm:mlDocument</b> transalation version or a specific
     * <b>cm:mlContainer</b> version
     *
     * @see org.alfresco.repo.model.ml.MultilingualDocumentAspect.PROPERTIES_TO_VERSION the versioned metadata
     * of a <b>cm:mlDocument</b> transalation added to the usual metadata versioned for a normal node.
     *
     * @see org.alfresco.repo.model.ml.MLContainerType.PROPERTIES_TO_VERSION the versioned metadata
     * of a <b>cm:mlContainer</b> added to the usual metadata versioned for a normal node.
     *
     * @param version                       An existing version of a <b>cm:mlDocument</b> translation version or
     *                                      an existing version of a <b>cm:mlContainer</b> version.
     * @return                              The versioned metadata
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"version"})
    Map<QName, Serializable> getVersionedMetadatas(Version version);

 }
