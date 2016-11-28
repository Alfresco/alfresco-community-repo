/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.model;

import org.alfresco.service.namespace.QName;


/**
 * QuickShare Model Constants
 * 
 * @author janv
 */
public interface QuickShareModel
{
    // Namespaces
    static final String QSHARE_MODEL_1_0_URI = "http://www.alfresco.org/model/qshare/1.0";
    
    // Aspects
    static final QName ASPECT_QSHARE = QName.createQName(QSHARE_MODEL_1_0_URI, "shared");
    
    // Properties
    static final QName PROP_QSHARE_SHAREDID = QName.createQName(QSHARE_MODEL_1_0_URI, "sharedId");
    static final QName PROP_QSHARE_SHAREDBY = QName.createQName(QSHARE_MODEL_1_0_URI, "sharedBy");
    static final QName PROP_QSHARE_EXPIRY_DATE = QName.createQName(QSHARE_MODEL_1_0_URI, "expiryDate");

}
