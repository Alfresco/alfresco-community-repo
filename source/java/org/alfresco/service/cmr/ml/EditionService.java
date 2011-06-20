/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
    @Auditable(parameters = {"translationNodeRef", "versionProperties"})
    NodeRef createEdition(NodeRef translationNodeRef, Map<String, Serializable> versionProperties);

    /**
     * Get editions of an existing <b>cm:mlContainer</b>.
     *
     * @param mlContainer               An existing <b>cm:mlContainer</b>
     * @return                          The Version History of the mlContainer
     */
    @Auditable(parameters = {"mlContainer"})
    VersionHistory getEditions(NodeRef mlContainer);

    /**
     * Get the different <b>cm:mlDocument</b> transalation version histories of a specific edition of a <b>cm:mlContainer</b>
     *
     * @param mlContainerEdition            An existing version of a mlContainer
     * @return                              The list of <b>cm:mlDocument</b> transalation versions of the edition
     */
    @Auditable(parameters = {"mlContainerEdition"})
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
    @Auditable(parameters = {"version"})
    Map<QName, Serializable> getVersionedMetadatas(Version version);

 }
