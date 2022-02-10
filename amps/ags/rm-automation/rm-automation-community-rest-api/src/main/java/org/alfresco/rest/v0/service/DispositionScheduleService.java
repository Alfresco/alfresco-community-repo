/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.v0.service;


import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;

import java.util.HashMap;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for different disposition schedule actions
 *
 * @author jcule, cagache
 * @since 2.6.2
 */
@Service
public class DispositionScheduleService extends BaseAPI
{
    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;

    @Autowired
    private DataUserAIS dataUser;

    /**
     * Helper method for adding a retain after period step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param period       for what period the item will be retained
     */
    public void addRetainAfterPeriodStep(String categoryName, String period)
    {
        HashMap<RETENTION_SCHEDULE, String> retainStep = new HashMap<>();
        retainStep.put(RETENTION_SCHEDULE.NAME, "retain");
        retainStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        retainStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Retain after a period step");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, retainStep);
    }

    /**
     * Helper method for adding a cut off immediately after created date step
     *
     * @param categoryName   the category in whose schedule the step will be added
     */
    public void addCutOffImmediatelyStep(String categoryName)
    {
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, "immediately");
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off immediately step");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, cutOffStep);
    }

    /**
     * Helper method for adding a cut off after period step
     *
     * @param categoryName   the category in whose schedule the step will be added
     * @param period         the period that needs to pass from periodProperty for cut off to be available
     * @param periodProperty the property of the dispositioned item that is used to calculate the "as of" period
     */
    public void addCutOffAfterPeriodStep(String categoryName, String period, RetentionPeriodProperty periodProperty)
    {
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY, periodProperty.getPeriodProperty());
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off after a period step");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, cutOffStep);
    }

    /**
     * Helper method for adding a destroy step with ghosting immediately after CUT OFF date
     *
     * @param categoryName   the category in whose schedule the step will be added
     */
    public void addDestroyWithGhostingImmediatelyAfterCutOff(String categoryName)
    {
        addDestroyWithGhostingAfterPeriodStep(categoryName, "immediately", CUT_OFF_DATE);
    }

    /**
     * Helper method for adding a destroy step with ghosting after period
     *
     * @param categoryName   the category in whose schedule the step will be added
     * @param period         the period that needs to pass for destroy to be available
     * @param periodProperty the property of the dispositioned item that is used to calculate the "as of" period
     */
    public void addDestroyWithGhostingAfterPeriodStep(String categoryName, String period, RetentionPeriodProperty periodProperty)
    {
        HashMap<RETENTION_SCHEDULE, String> destroyStep = new HashMap<>();
        destroyStep.put(RETENTION_SCHEDULE.NAME, "destroy");
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY, periodProperty.getPeriodProperty());
        destroyStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Destroy after a period step with keep metadata");
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_GHOST, "on");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, destroyStep);
    }

    /**
     * Helper method for adding a destroy step without ghosting after period
     *
     * @param categoryName   the category in whose schedule the step will be added
     * @param period         the period that needs to pass for destroy to be available
     * @param periodProperty the property of the dispositioned item that is used to calculate the "as of" period
     */
    public void addDestroyWithoutGhostingAfterPeriodStep(String categoryName, String period,
                                                       RetentionPeriodProperty periodProperty)
    {
        HashMap<RETENTION_SCHEDULE, String> destroyStep = new HashMap<>();
        destroyStep.put(RETENTION_SCHEDULE.NAME, "destroy");
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY, periodProperty.getPeriodProperty());
        destroyStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Destroy after a period step");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, destroyStep);
    }

    /**
     * Helper method for adding a cut off after an event occurs step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param events       the events that need to occur for cut off to be available
     */
    public void addCutOffAfterEventStep(String categoryName, String events)
    {
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_EVENTS, events);
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off after event step");

        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, cutOffStep);
    }

    /**
     * Helper method for adding a transfer after an event occurs step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param location     the transfer location
     * @param events       the events that need to occur for transfer to be available
     */
    public void addTransferAfterEventStep(String categoryName, String location, String events)
    {
        HashMap<RETENTION_SCHEDULE, String> transferStep = new HashMap<>();
        transferStep.put(RETENTION_SCHEDULE.NAME, "transfer");
        transferStep.put(RETENTION_SCHEDULE.RETENTION_LOCATION, location);
        transferStep.put(RETENTION_SCHEDULE.RETENTION_EVENTS, events);
        transferStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Transfer after event step");
        transferStep.put(RETENTION_SCHEDULE.COMBINE_DISPOSITION_STEP_CONDITIONS, "false");
        transferStep.put(RETENTION_SCHEDULE.RETENTION_ELIGIBLE_FIRST_EVENT, "true");

        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, transferStep);
    }

    /**
     * Helper method for adding an accession step
     *
     * @param timeOrEvent
     * @param events
     * @param period
     * @param periodProperty
     * @param combineConditions
     */
    public void addAccessionStep(String categoryName, Boolean timeOrEvent, String events, String period,
                                 RetentionPeriodProperty periodProperty, Boolean combineConditions)
    {
        HashMap<RETENTION_SCHEDULE, String> accessionStep = new HashMap<>();
        accessionStep.put(RETENTION_SCHEDULE.NAME, "accession");
        accessionStep.put(RETENTION_SCHEDULE.COMBINE_DISPOSITION_STEP_CONDITIONS, Boolean.toString(combineConditions));
        accessionStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        accessionStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY, periodProperty.getPeriodProperty());
        if (!timeOrEvent)
        {
            accessionStep.put(RETENTION_SCHEDULE.RETENTION_ELIGIBLE_FIRST_EVENT, Boolean.toString(timeOrEvent));
        }
        accessionStep.put(RETENTION_SCHEDULE.RETENTION_EVENTS, events);
        accessionStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Accession step with time and event conditions.");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, accessionStep);
    }

    /**
     * Helper method to create retention schedule with general fields for the given category as user
     * and apply it to the records/ record folders
     *
     * @param user             the user who creates the retention schedule
     * @param categoryName     the category on which is created the retention schedule
     * @param appliedToRecords true if is applied on records, false if is applied on folders
     */
    public void createCategoryRetentionSchedule(UserModel user, String categoryName, Boolean appliedToRecords)
    {
        recordCategoriesAPI.createRetentionSchedule(user.getUsername(), user.getPassword(), categoryName);
        String retentionScheduleNodeRef = recordCategoriesAPI.getDispositionScheduleNodeRef(
                dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), categoryName);

        HashMap<RETENTION_SCHEDULE, String> retentionScheduleGeneralFields = new HashMap<>();
        retentionScheduleGeneralFields.put(RETENTION_SCHEDULE.RETENTION_AUTHORITY, "Authority");
        retentionScheduleGeneralFields.put(RETENTION_SCHEDULE.RETENTION_INSTRUCTIONS, "Instructions");
        recordCategoriesAPI.setRetentionScheduleGeneralFields(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), retentionScheduleNodeRef, retentionScheduleGeneralFields,
                appliedToRecords);
    }

    /**
     * Helper method to create retention schedule with general fields for the given category as admin
     * and apply it to the records/record folders
     *
     * @param categoryName     the category on which is created the retention schedule
     * @param appliedToRecords true if is applied on records, false if is applied on folders
     */
    public void createCategoryRetentionSchedule(String categoryName, Boolean appliedToRecords)
    {
        createCategoryRetentionSchedule(dataUser.getAdminUser(), categoryName, appliedToRecords);
    }
}
