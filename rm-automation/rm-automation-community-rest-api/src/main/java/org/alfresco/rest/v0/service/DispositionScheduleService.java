/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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


import java.util.HashMap;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.utility.data.DataUser;
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
    private DataUser dataUser;

    /**
     * Helper method for adding a retain after period step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param period
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
     * Helper method for adding a cut off after period step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param period
     */
    public void addCutOffAfterPeriodStep(String categoryName, String period)
    {
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off after a period step");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, cutOffStep);
    }

    /**
     * Helper method for adding a destroy with ghosting after period 
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param period
     */
    public void addDestroyWithGhostingAfterPeriodStep(String categoryName, String period)
    {
        HashMap<RETENTION_SCHEDULE, String> destroyStep = new HashMap<>();
        destroyStep.put(RETENTION_SCHEDULE.NAME, "destroy");
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, period);
        destroyStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Destroy after a period step");
        destroyStep.put(RETENTION_SCHEDULE.RETENTION_GHOST, "on");
        recordCategoriesAPI.addDispositionScheduleSteps(dataUser.getAdminUser().getUsername(),
                dataUser.getAdminUser().getPassword(), categoryName, destroyStep);
    }

    /**
     * Helper method for adding a cut off after an event occurs step
     *
     * @param categoryName the category in whose schedule the step will be added
     * @param events
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
     * Helper method to create retention schedule with general fields for the given category as admin
     * and apply it to the records
     *
     * @param categoryName
     * @param appliedToRecords
     */
    public void createCategoryRetentionSchedule(String categoryName, Boolean appliedToRecords)
    {
        recordCategoriesAPI.createRetentionSchedule(dataUser.getAdminUser().getUsername(),
                    dataUser.getAdminUser().getPassword(), categoryName);
        String retentionScheduleNodeRef = recordCategoriesAPI.getDispositionScheduleNodeRef(
                    dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(), categoryName);

        HashMap<RETENTION_SCHEDULE, String> retentionScheduleGeneralFields = new HashMap<>();
        retentionScheduleGeneralFields.put(RETENTION_SCHEDULE.RETENTION_AUTHORITY, "Authority");
        retentionScheduleGeneralFields.put(RETENTION_SCHEDULE.RETENTION_INSTRUCTIONS, "Instructions");
        recordCategoriesAPI.setRetentionScheduleGeneralFields(dataUser.getAdminUser().getUsername(),
                    dataUser.getAdminUser().getPassword(), retentionScheduleNodeRef, retentionScheduleGeneralFields,
                    appliedToRecords);

    }
}
