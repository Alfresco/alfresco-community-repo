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
 * @since 2.4.a
 */
public interface ClassifiedContentModel
{
    /** Namespace details */
    String CLF_URI = "http://www.alfresco.org/model/classifiedcontent/1.0";
    String CLF_PREFIX = "clf";

    Serializable[] LEVELS_KEY = new String[] { "org.alfresco", "module.org_alfresco_module_rm", "classification.levels" };
    Serializable[] REASONS_KEY = new String[] { "org.alfresco", "module.org_alfresco_module_rm", "classification.reasons" };
    Serializable[] EXEMPTION_CATEGORIES_KEY = new String[] { "org.alfresco", "module.org_alfresco_module_rm", "classification.exemptionCategories" };

    /** Classified aspect */
    QName ASPECT_CLASSIFIED = QName.createQName(CLF_URI, "classified");
    QName PROP_INITIAL_CLASSIFICATION = QName.createQName(CLF_URI, "initialClassification");
    QName PROP_CURRENT_CLASSIFICATION = QName.createQName(CLF_URI, "currentClassification");
    QName PROP_CLASSIFICATION_AGENCY  = QName.createQName(CLF_URI, "classificationAgency");
    QName PROP_CLASSIFIED_BY = QName.createQName(CLF_URI, "classifiedBy");
    QName PROP_CLASSIFICATION_REASONS = QName.createQName(CLF_URI, "classificationReasons");
    QName PROP_DOWNGRADE_DATE = QName.createQName(CLF_URI, "downgradeDate");
    QName PROP_DOWNGRADE_EVENT = QName.createQName(CLF_URI, "downgradeEvent");
    QName PROP_DOWNGRADE_INSTRUCTIONS = QName.createQName(CLF_URI, "downgradeInstructions");
    QName PROP_DECLASSIFICATION_DATE = QName.createQName(CLF_URI, "declassificationDate");
    QName PROP_DECLASSIFICATION_EVENT = QName.createQName(CLF_URI, "declassificationEvent");
    QName PROP_DECLASSIFICATION_EXEMPTIONS = QName.createQName(CLF_URI, "declassificationExemptions");

    QName PROP_LAST_RECLASSIFY_BY = QName.createQName(CLF_URI, "lastReclassifyBy");
    QName PROP_LAST_RECLASSIFY_AT = QName.createQName(CLF_URI, "lastReclassifyAt");
    QName PROP_LAST_RECLASSIFY_REASON = QName.createQName(CLF_URI, "lastReclassifyReason");
    QName PROP_LAST_RECLASSIFICATION_ACTION = QName.createQName(CLF_URI, "lastReclassificationAction");

    /** Security Clearance aspect. */
    QName ASPECT_SECURITY_CLEARANCE = QName.createQName(CLF_URI, "securityClearance");
    QName PROP_CLEARANCE_LEVEL = QName.createQName(CLF_URI, "clearanceLevel");

    /** Classified Rendition aspect. */
    QName ASPECT_CLASSIFIED_RENDITION = QName.createQName(CLF_URI, "classifiedRendition");
    QName ASSOC_CLASSIFIED_RENDITION  = QName.createQName(CLF_URI, "classifiedRendition");
}