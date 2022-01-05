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
package org.alfresco.rest.rm.community.model.rules;

/**
 * Action values.
 */
public enum ActionsOnRule
{
    COMPLETE_RECORD("declareRecord"),
    REOPEN_RECORD("undeclareRecord"),
    OPEN_RECORD_FOLDER("openRecordFolder"),
    CLOSE_RECORD_FOLDER("closeRecordFolder"),
    FILE_TO("fileTo"),
    COPY_TO("copyTo"),
    MOVE_TO("moveTo"),
    LINK_TO("linkTo"),
    REJECT("reject"),
    REQUEST_INFORMATION("requestInfo"),
    COMPLETE_EVENT("completeEvent"),
    ADD_RECORD_TYPES("addRecordTypes"),
    EXECUTE_SCRIPT("executeScript"),
    SEND_EMAIL("sendEmail"),
    SET_PROPERTY_VALUE_COLL_SITE("set-property-value"),
    SET_PROPERTY_VALUE_RM("setPropertyValue"),
    HIDE_RECORD("hide-record"),
    DECLARE_VERSION_AS_RECORD("declare-as-version-record"),
    DECLARE_AS_RECORD("create-record"),
    WORM_LOCK("wormLock");

    private String actionValue;

    ActionsOnRule(String value)
    {
        this.actionValue = value;
    }

    public String getActionValue()
    {
        return actionValue;
    }
}
