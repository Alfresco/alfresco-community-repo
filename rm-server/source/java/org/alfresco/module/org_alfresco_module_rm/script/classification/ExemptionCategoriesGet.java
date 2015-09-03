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
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get the exemption categories.
 *
 * @author tpage
 * @since 2.4.a
 */
public class ExemptionCategoriesGet extends AbstractRmWebScript
{
    /** The exemption category key for the map. */
    private static final String EXEMPTION_CATEGORIES = "exemptionCategories";

    /** The classification scheme service. */
    private ClassificationSchemeService classificationSchemeService;

    /**
     * Sets the classification scheme service.
     *
     * @param classificationSchemeService The classification scheme service.
     */
    public void setClassificationSchemeService(ClassificationSchemeService classificationSchemeService)
    {
        this.classificationSchemeService = classificationSchemeService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(EXEMPTION_CATEGORIES, classificationSchemeService.getExemptionCategories());
        return result;
    }
}
