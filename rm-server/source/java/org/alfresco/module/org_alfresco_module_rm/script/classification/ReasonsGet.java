/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationService;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.JSONConversionComponent;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get the classification reasons.
 *
 * @author tpage
 * @since 3.0
 */
public class ReasonsGet extends AbstractRmWebScript
{
    /** Constants */
    private static final String REASONS = "reasons";

    /** Classification service */
    private ClassificationService classificationService;

    /** JSON conversion component */
    private JSONConversionComponent jsonConversionComponent;

    /**
     * Gets the JSON conversion component
     *
     * @return The JSON conversion component
     */
    protected JSONConversionComponent getJsonConversionComponent()
    {
        return this.jsonConversionComponent;
    }

    /**
     * Sets the classification service
     *
     * @param classificatonService The classification service
     */
    public void setClassificationService(ClassificationService classificationService)
    {
        this.classificationService = classificationService;
    }

    /**
     * Sets the JSON conversion component
     *
     * @param jsonConversionComponent The JSON conversion component
     */
    public void setJsonConversionComponent(JSONConversionComponent jsonConversionComponent)
    {
        this.jsonConversionComponent = jsonConversionComponent;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(REASONS, classificationService.getClassificationReasons());
        return result;
    }
}
