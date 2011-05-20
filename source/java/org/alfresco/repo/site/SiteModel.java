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
package org.alfresco.repo.site;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * Site models constants
 * 
 * @author Roy Wetherall
 */
public interface SiteModel
{
    /** Site Model */
    public static final String SITE_MODEL_URL = "http://www.alfresco.org/model/site/1.0";
    public static final String SITE_MODEL_PREFIX = "st";
    public static final String SITE_CUSTOM_PROPERTY_URL = "http://www.alfresco.org/model/sitecustomproperty/1.0";
    public static final String SITE_CUSTOM_PROPERTY_PREFIX = "stcp";
    
    /** Site */
    public static final QName TYPE_SITES = QName.createQName(SITE_MODEL_URL, "sites");
    public static final QName TYPE_SITE = QName.createQName(SITE_MODEL_URL, "site");
    public static final QName PROP_SITE_PRESET = QName.createQName(SITE_MODEL_URL, "sitePreset");
    public static final QName PROP_SITE_VISIBILITY = QName.createQName(SITE_MODEL_URL, "siteVisibility");
    
    /** Site Container */
    public static final QName ASPECT_SITE_CONTAINER = QName.createQName(SITE_MODEL_URL, "siteContainer");
    public static final QName PROP_COMPONENT_ID = QName.createQName(SITE_MODEL_URL, "componentId");
    
    /** Site Permission */
    public static final String SITE_MANAGER = "SiteManager";
    public static final String SITE_COLLABORATOR = "SiteCollaborator";
    public static final String SITE_CONTRIBUTOR = "SiteContributor";
    public static final String SITE_CONSUMER = "SiteConsumer";

    /**
     * Convenience collection of default site permissions
     * @since 3.5.0
     */
    public static final List<String> STANDARD_PERMISSIONS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            new String[] {SITE_MANAGER, SITE_COLLABORATOR, SITE_CONTRIBUTOR, SITE_CONSUMER} ));
}