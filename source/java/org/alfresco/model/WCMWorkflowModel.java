/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Gavin Cornwell
 */
public interface WCMWorkflowModel
{
    // submission workflow properties and aspects
    static final QName PROP_FROM_PATH = QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, "fromPath");
    static final QName PROP_LABEL = QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, "label");
    static final QName PROP_LAUNCH_DATE = QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, "launchDate");
    static final QName PROP_AUTO_DEPLOY = QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, "autoDeploy");
    static final QName PROP_WEBAPP = QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, "webapp");
    static final QName ASSOC_WEBPROJECT = QName.createQName(NamespaceService.WCMWF_MODEL_1_0_URI, "webproject");
}
