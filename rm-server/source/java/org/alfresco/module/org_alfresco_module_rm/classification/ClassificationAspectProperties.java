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
package org.alfresco.module.org_alfresco_module_rm.classification;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * A data transfer object for properties from the classification aspect.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class ClassificationAspectProperties
{
    /** The security clearance needed to access the content. */
    private String classificationLevelId;

    /** Free-form text identifying who classified the content. */
    private String classifiedBy;

    /** The name of the agency responsible for the classification of this content. */
    private String classificationAgency;

    /** A non-empty set of ids of reasons for classifying the content in this way. */
    private Set<String> classificationReasonIds = new HashSet<>();

    /** If provided, this is the date of the next downgrade evaluation. */
    private Date downgradeDate;

    /** If provided, this is the event at which the next downgrade evaluation will take place. */
    private String downgradeEvent;

    /** If a downgrade date or event is given then this must be provided too with the instructions for the evaluation. */
    private String downgradeInstructions;

    /** If provided, this is the date of the next declassification evaluation. */
    private Date declassificationDate;

    /** If provided, this is the event at which the next declassification evaluation will take place. */
    private String declassificationEvent;

    /** An optional list of exemption category ids. */
    private Set<String> exemptionCategoryIds = new HashSet<>();

    /** This needs to be provided if the classification level is changed */
    private String lastReclassifyBy;

    /** This needs to be provided if the classification level is changed */
    private String lastReclassifyReason;

    /** @return The security clearance needed to access the content. */
    public String getClassificationLevelId()
    {
        return classificationLevelId;
    }

    /** @param classificationLevelId The security clearance needed to access the content. */
    public void setClassificationLevelId(String classificationLevelId)
    {
        this.classificationLevelId = classificationLevelId;
    }

    /** @return Free-form text identifying who classified the content. */
    public String getClassifiedBy()
    {
        return classifiedBy;
    }

    /** @param classifiedBy Free-form text identifying who classified the content. */
    public void setClassifiedBy(String classifiedBy)
    {
        this.classifiedBy = classifiedBy;
    }

    /** @return The name of the agency responsible for the classification of this content. */
    public String getClassificationAgency()
    {
        return classificationAgency;
    }

    /** @param classificationAgency The name of the agency responsible for the classification of this content. */
    public void setClassificationAgency(String classificationAgency)
    {
        this.classificationAgency = classificationAgency;
    }

    /** @return A non-empty set of ids of reasons for classifying the content in this way. */
    public Set<String> getClassificationReasonIds()
    {
        return classificationReasonIds;
    }

    /**
     * Store an immutable copy of the given set of classification reason ids.
     *
     * @param classificationReasonIds A non-empty set of ids of reasons for classifying the content in this way.
     */
    public void setClassificationReasonIds(Set<String> classificationReasonIds)
    {
        this.classificationReasonIds = ImmutableSet.copyOf(classificationReasonIds);
    }

    /** @return If provided, this is the date of the next downgrade evaluation. */
    public Date getDowngradeDate()
    {
        return downgradeDate;
    }

    /** @param downgradeDate If provided, this is the date of the next downgrade evaluation. */
    public void setDowngradeDate(Date downgradeDate)
    {
        this.downgradeDate = downgradeDate;
    }

    /** @return If provided, this is the event at which the next downgrade evaluation will take place. */
    public String getDowngradeEvent()
    {
        return downgradeEvent;
    }

    /** @param downgradeEvent If provided, this is the event at which the next downgrade evaluation will take place. */
    public void setDowngradeEvent(String downgradeEvent)
    {
        this.downgradeEvent = downgradeEvent;
    }

    /** @return If a downgrade date or event is given then this must be provided too with the instructions for the evaluation. */
    public String getDowngradeInstructions()
    {
        return downgradeInstructions;
    }

    /** @param downgradeInstructions If a downgrade date or event is given then this must be provided too with the instructions for the evaluation. */
    public void setDowngradeInstructions(String downgradeInstructions)
    {
        this.downgradeInstructions = downgradeInstructions;
    }

    /** @return If provided, this is the date of the next declassification evaluation. */
    public Date getDeclassificationDate()
    {
        return declassificationDate;
    }

    /** @param declassificationDate If provided, this is the date of the next declassification evaluation. */
    public void setDeclassificationDate(Date declassificationDate)
    {
        this.declassificationDate = declassificationDate;
    }

    /** @return If provided, this is the event at which the next declassification evaluation will take place. */
    public String getDeclassificationEvent()
    {
        return declassificationEvent;
    }

    /** @param declassificationEvent If provided, this is the event at which the next declassification evaluation will take place. */
    public void setDeclassificationEvent(String declassificationEvent)
    {
        this.declassificationEvent = declassificationEvent;
    }

    /** @return This is an optional list of exemption category ids. */
    public Set<String> getExemptionCategoryIds()
    {
        return exemptionCategoryIds;
    }

    /**
     * Store an immutable copy of the given set of exemption category ids.
     *
     * @param exemptionCategoryIds This is an optional list of exemption category ids.
     */
    public void setExemptionCategoryIds(Set<String> exemptionCategoryIds)
    {
        this.exemptionCategoryIds = ImmutableSet.copyOf(exemptionCategoryIds);
    }

    /**
     * The user name (free form value) who reclassified the previously classified content
     *
     * @return the lastReclassifyBy The user name who reclassified the content
     */
    public String getLastReclassifyBy()
    {
        return this.lastReclassifyBy;
    }

    /**
     * The user name (free form value) who is reclassifying the previously classified content
     *
     * @param lastReclassifyBy the lastReclassifyBy to set
     */
    public void setLastReclassifyBy(String lastReclassifyBy)
    {
        this.lastReclassifyBy = lastReclassifyBy;
    }

    /**
     * The reclassification reason
     *
     * @return the lastReclassifyReason The reason for reclassification
     */
    public String getLastReclassifyReason()
    {
        return this.lastReclassifyReason;
    }

    /**
     * The reason for reclassification
     *
     * @param lastReclassifyReason the lastReclassifyReason to set
     */
    public void setLastReclassifyReason(String lastReclassifyReason)
    {
        this.lastReclassifyReason = lastReclassifyReason;
    }
}
