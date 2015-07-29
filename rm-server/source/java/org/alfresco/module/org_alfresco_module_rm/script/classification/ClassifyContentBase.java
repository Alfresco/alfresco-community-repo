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

import static org.alfresco.util.WebScriptUtils.getJSONArrayFromJSONObject;
import static org.alfresco.util.WebScriptUtils.getJSONArrayValue;
import static org.alfresco.util.WebScriptUtils.getRequestContentAsJsonObject;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.extensions.webscripts.Status.STATUS_BAD_REQUEST;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Base class for classify content actions
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public abstract class ClassifyContentBase extends AbstractRmWebScript
{
    /** Constants */
    public static final String CLASSIFICATION_LEVEL_ID = "classificationLevelId";
    public static final String CLASSIFIED_BY = "classifiedBy";
    public static final String CLASSIFICATION_AGENCY = "classificationAgency";
    public static final String CLASSIFICATION_REASONS = "classificationReasons";
    public static final String DOWNGRADE_DATE = "downgradeDate";
    public static final String DOWNGRADE_EVENT = "downgradeEvent";
    public static final String DOWNGRADE_INSTRUCTIONS = "downgradeInstructions";
    public static final String DECLASSIFICATION_DATE = "declassificationDate";
    public static final String DECLASSIFICATION_EVENT = "declassificationEvent";
    public static final String DECLASSIFICATION_EXEMPTIONS = "declassificationExemptions";

    /** The service responsible for classifying content. */
    private ContentClassificationService contentClassificationService;

    /**
     * Get the service responsible for classifying content.
     *
     * @return the contentClassificationService
     */
    protected ContentClassificationService getContentClassificationService()
    {
        return this.contentClassificationService;
    }

    /**
     * Set the service responsible for classifying content.
     *
     * @param contentClassificationService The service responsible for classifying content.
     */
    public void setContentClassificationService(ContentClassificationService contentClassificationService)
    {
        this.contentClassificationService = contentClassificationService;
    }

    /**
     * Abstract method which does the action of either
     * classifying a content or editing a classified content
     *
     * @param classificationAspectProperties The properties to use when classifying the content.
     * @param document The classified content which will be edited.
     */
    protected abstract void doClassifyAction(ClassificationAspectProperties classificationAspectProperties, NodeRef document);

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject jsonObject = getRequestContentAsJsonObject(req);
        String classificationLevelId = getStringValueFromJSONObject(jsonObject, CLASSIFICATION_LEVEL_ID);
        String classifiedBy = getStringValueFromJSONObject(jsonObject, CLASSIFIED_BY);
        String classificationAgency = getStringValueFromJSONObject(jsonObject, CLASSIFICATION_AGENCY, false, false);
        Set<String> classificationReasonIds = getClassificationReasonIds(jsonObject);
        String downgradeDate = getStringValueFromJSONObject(jsonObject, DOWNGRADE_DATE, false, false);
        String downgradeEvent = getStringValueFromJSONObject(jsonObject, DOWNGRADE_EVENT, false, false);
        String downgradeInstructions = getStringValueFromJSONObject(jsonObject, DOWNGRADE_INSTRUCTIONS, false, false);
        String declassificationDate = getStringValueFromJSONObject(jsonObject, DECLASSIFICATION_DATE, false, false);
        String declassificationEvent = getStringValueFromJSONObject(jsonObject, DECLASSIFICATION_EVENT, false, false);
        Set<String> exemptionCategoryIds = getExemptionCategoryIds(jsonObject);

        ClassificationAspectProperties propertiesDTO = new ClassificationAspectProperties();
        propertiesDTO.setClassificationLevelId(classificationLevelId);
        propertiesDTO.setClassifiedBy(classifiedBy);
        propertiesDTO.setClassificationAgency(classificationAgency);
        propertiesDTO.setClassificationReasonIds(classificationReasonIds);
        propertiesDTO.setDowngradeDate(parseDate(downgradeDate));
        propertiesDTO.setDowngradeEvent(downgradeEvent);
        propertiesDTO.setDowngradeInstructions(downgradeInstructions);
        propertiesDTO.setDeclassificationDate(parseDate(declassificationDate));
        propertiesDTO.setDeclassificationEvent(declassificationEvent);
        propertiesDTO.setExemptionCategoryIds(exemptionCategoryIds);

        NodeRef document = parseRequestForNodeRef(req);

        doClassifyAction(propertiesDTO, document);

        Map<String, Object> model = new HashMap<>(1);
        model.put(SUCCESS, true);

        return model;
    }

    /**
     * Helper method used to get the classification reason ids and exemption category ids
     *
     * @param jsonObject The json object representing the request body
     * @param key The key
     * @return {@link Set}<{@link String}> ids
     */
    private Set<String> getIds(JSONObject jsonObject, String key)
    {
        Set<String> ids = new HashSet<>();

        JSONArray jsonArray = getJSONArrayFromJSONObject(jsonObject, key);
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject id = (JSONObject) getJSONArrayValue(jsonArray, i);
            ids.add(getStringValueFromJSONObject(id, ID));
        }

        return ids;
    }

    /**
     * Helper method to get the classification reason ids
     *
     * @param jsonObject The json object representing the request body
     * @return {@link Set}<{@link String}> classification ids
     */
    private Set<String> getClassificationReasonIds(JSONObject jsonObject)
    {
        return getIds(jsonObject, CLASSIFICATION_REASONS);
    }

    /**
     * Helper method to get the exemption category ids
     *
     * @param jsonObject The json object representing the request body
     * @return {@link Set}<{@link String}> exemption category ids
     */
    private Set<String> getExemptionCategoryIds(JSONObject jsonObject)
    {
        Set<String> exemptionCategoryIds = new HashSet<>();

        if (jsonObject.has(DECLASSIFICATION_EXEMPTIONS))
        {
            exemptionCategoryIds.addAll(getIds(jsonObject, DECLASSIFICATION_EXEMPTIONS));
        }

        return exemptionCategoryIds;
    }

    /**
     * Parses the given string as date
     *
     * @param date The {@link String} which will be parsed
     * @return The parsed date, if the given date is blank then <code>null</code> will be returned.
     */
    private Date parseDate(String date)
    {
        Date parsedDate = null;

        // FIXME: "null" check
        if (isNotBlank(date) && !date.equalsIgnoreCase("null"))
        {
            try
            {
                parsedDate = DateUtils.parseDate(date, ISO_DATE_FORMAT.getPattern());
            }
            catch (ParseException error)
            {
                throw new WebScriptException(STATUS_BAD_REQUEST, "The given date '" + date + "' could not be parsed.");
            }
        }

        return parsedDate;
    }
}
