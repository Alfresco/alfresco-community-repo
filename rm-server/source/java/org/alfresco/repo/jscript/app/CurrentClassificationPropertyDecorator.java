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
package org.alfresco.repo.jscript.app;

import static org.alfresco.util.ParameterCheck.mandatory;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Current classification property decorator
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class CurrentClassificationPropertyDecorator extends BasePropertyDecorator
{
    /** Constants */
    protected static final String ID = "id";
    protected static final String LABEL = "label";

    /** Classification scheme service */
    private ClassificationSchemeService classificationSchemeService;

    /**
     * @return the classificationSchemeService
     */
    protected ClassificationSchemeService getClassificationSchemeService()
    {
        return this.classificationSchemeService;
    }

    /**
     * @param classificationSchemeService the classificationSchemeService to set
     */
    public void setClassificationSchemeService(ClassificationSchemeService classificationSchemeService)
    {
        this.classificationSchemeService = classificationSchemeService;
    }

    /**
     * @see org.alfresco.repo.jscript.app.PropertyDecorator#decorate(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public JSONAware decorate(QName propertyName, NodeRef nodeRef, Serializable value)
    {
        mandatory("value", value);
        JSONObject jsonObj = new JSONObject();
        String currentClassificationId = value.toString();
        jsonObj.put(ID, currentClassificationId);
        jsonObj.put(LABEL, getClassificationSchemeService().getClassificationLevelById(currentClassificationId).getDisplayLabel());
        return jsonObj;
    }
}
