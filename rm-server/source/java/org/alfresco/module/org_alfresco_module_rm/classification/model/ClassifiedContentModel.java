/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.model;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * Classified content model interface.
 * <p>
 * Helper containing reusable information about the classified content model.
 *
 * @author Roy Wetherall
 * @since 3.0
 */
public interface ClassifiedContentModel
{
    /** Namespace details */
    String CLF_URI = "http://www.alfresco.org/model/classifiedcontent/1.0";
    String CLF_PREFIX = "clf";

    Serializable[] LEVELS_KEY = new String[] { "org.alfresco", "module.org_alfresco_module_rm", "classification.levels" };
    Serializable[] REASONS_KEY = new String[] { "org.alfresco", "module.org_alfresco_module_rm", "classification.reasons" };

    /** Classified aspect */
    QName ASPECT_CLASSIFIED = QName.createQName(CLF_URI, "classified");
    QName PROP_INITIAL_CLASSIFICATION = QName.createQName(CLF_URI, "initialClassification");
    QName PROP_CURRENT_CLASSIFICATION = QName.createQName(CLF_URI, "currentClassification");
    QName PROP_CLASSIFICATION_AUTHORITY = QName.createQName(CLF_URI, "classificationAuthority");
    QName PROP_CLASSIFICATION_REASONS = QName.createQName(CLF_URI, "classificationReasons");

    /** Security Clearance aspect. */
    QName ASPECT_SECURITY_CLEARANCE = QName.createQName(CLF_URI, "securityClearance");
    QName PROP_CLEARANCE_LEVEL = QName.createQName(CLF_URI, "clearanceLevel");
}