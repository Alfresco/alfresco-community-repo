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
package org.alfresco.repo.action;

import org.alfresco.service.namespace.QName;

public interface ActionModel
{
    static final String ACTION_MODEL_URI            = "http://www.alfresco.org/model/action/1.0";
    static final String ACTION_MODEL_PREFIX         = "act";
    static final QName TYPE_ACTION_BASE             = QName.createQName(ACTION_MODEL_URI, "actionbase");
    static final QName TYPE_ACTION                  = QName.createQName(ACTION_MODEL_URI, "action");
    static final QName PROP_DEFINITION_NAME         = QName.createQName(ACTION_MODEL_URI, "definitionName");
    static final QName PROP_ACTION_TITLE            = QName.createQName(ACTION_MODEL_URI, "actionTitle");
    static final QName PROP_ACTION_DESCRIPTION      = QName.createQName(ACTION_MODEL_URI, "actionDescription");
    static final QName PROP_TRACK_STATUS            = QName.createQName(ACTION_MODEL_URI, "trackStatus");
    static final QName PROP_EXECUTE_ASYNCHRONOUSLY  = QName.createQName(ACTION_MODEL_URI, "executeAsynchronously");
    static final QName PROP_EXECUTION_START_DATE    = QName.createQName(ACTION_MODEL_URI, "executionStartDate");
    static final QName PROP_EXECUTION_END_DATE      = QName.createQName(ACTION_MODEL_URI, "executionEndDate");
    static final QName PROP_EXECUTION_ACTION_STATUS = QName.createQName(ACTION_MODEL_URI, "executionActionStatus");
    static final QName PROP_EXECUTION_FAILURE_MESSAGE = QName.createQName(ACTION_MODEL_URI, "executionFailureMessage");
    static final QName ASSOC_CONDITIONS             = QName.createQName(ACTION_MODEL_URI, "conditions");

    static final QName ASSOC_COMPENSATING_ACTION    = QName.createQName(ACTION_MODEL_URI, "compensatingAction");
    static final QName ASSOC_PARAMETERS             = QName.createQName(ACTION_MODEL_URI, "parameters");
    static final QName TYPE_ACTION_CONDITION        = QName.createQName(ACTION_MODEL_URI, "actioncondition");
    static final QName TYPE_COMPOSITE_ACTION_CONDITION        = QName.createQName(ACTION_MODEL_URI, "compositeactioncondition");

    static final QName TYPE_ACTION_PARAMETER        = QName.createQName(ACTION_MODEL_URI, "actionparameter");
    static final QName PROP_PARAMETER_NAME          = QName.createQName(ACTION_MODEL_URI, "parameterName");
    static final QName PROP_PARAMETER_VALUE         = QName.createQName(ACTION_MODEL_URI, "parameterValue");
    static final QName TYPE_COMPOSITE_ACTION        = QName.createQName(ACTION_MODEL_URI, "compositeaction");
    static final QName ASSOC_ACTIONS                = QName.createQName(ACTION_MODEL_URI, "actions");
    static final QName ASSOC_COMPOSITE_ACTION_CONDITION       = QName.createQName(ACTION_MODEL_URI, "compositeconditions");

    static final QName ASPECT_ACTIONS               = QName.createQName(ACTION_MODEL_URI, "actions");
    static final QName ASSOC_ACTION_FOLDER          = QName.createQName(ACTION_MODEL_URI, "actionFolder");
    
    static final QName TYPE_ACTION_SCHEDULE         = QName.createQName(ACTION_MODEL_URI, "actionSchedule");
    static final QName PROP_START_DATE              = QName.createQName(ACTION_MODEL_URI, "startDate");
    static final QName PROP_INTERVAL_COUNT          = QName.createQName(ACTION_MODEL_URI, "intervalCount");
    static final QName PROP_INTERVAL_PERIOD         = QName.createQName(ACTION_MODEL_URI, "intervalPeriod");
    static final QName PROP_LAST_EXECUTED_AT        = QName.createQName(ACTION_MODEL_URI, "lastExecutedAt");
    static final QName ASSOC_SCHEDULED_ACTION       = QName.createQName(ACTION_MODEL_URI, "scheduledAction");
    
    //static final QName ASPECT_ACTIONABLE = QName.createQName(ACTION_MODEL_URI, "actionable");
    //static final QName ASSOC_SAVED_ACTION_FOLDERS = QName.createQName(ACTION_MODEL_URI, "savedActionFolders");
    //static final QName TYPE_SAVED_ACTION_FOLDER = QName.createQName(ACTION_MODEL_URI, "savedactionfolder");
    //static final QName ASSOC_SAVED_ACTIONS = QName.createQName(ACTION_MODEL_URI, "savedActions");
    
    static final QName PROP_CONDITION_INVERT = QName.createQName(ACTION_MODEL_URI, "invert");
    static final QName PROP_CONDITION_ANDOR = QName.createQName(ACTION_MODEL_URI, "or");

    /** Action assoc name */
    public static final QName ASSOC_NAME_ACTIONS = QName.createQName(ACTION_MODEL_URI, "actions");

}
