/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management custom model qualified names
 *
 * @author Gavin Cornwell
 */
@AlfrescoPublicApi
public interface RecordsManagementCustomModel
{
    // Namespace details
    String RM_CUSTOM_URI = "http://www.alfresco.org/model/rmcustom/1.0";
    String RM_CUSTOM_PREFIX = "rmc";

    // Model
    QName RM_CUSTOM_MODEL = QName.createQName(RM_CUSTOM_URI, "rmcustom");

    // Custom constraint for Supplemental Marking List
    QName CONSTRAINT_CUSTOM_SMLIST = QName.createQName(RM_CUSTOM_URI, "smList");

    // Custom property for for Supplemental Marking List
    QName PROP_SUPPLEMENTAL_MARKING_LIST = QName.createQName(RM_CUSTOM_URI, "supplementalMarkingList");

    // Supplemental Marking List aspect
    QName ASPECT_SUPPLEMENTAL_MARKING_LIST = QName.createQName(RM_CUSTOM_URI, "customSupplementalMarkingList");

    // Custom associations aspect
    QName ASPECT_CUSTOM_ASSOCIATIONS = QName.createQName(RM_CUSTOM_URI, "customAssocs");

    // Some Custom references which are present on system startup.
    QName CUSTOM_REF_VERSIONS = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions");
    QName CUSTOM_REF_SUPERSEDES = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "supersedes");
    QName CUSTOM_REF_OBSOLETES = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "obsoletes");
    QName CUSTOM_REF_SUPPORTS = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "supports");
    QName CUSTOM_REF_CROSSREFERENCE = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "crossreference");
    QName CUSTOM_REF_RENDITION = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "rendition");
}
