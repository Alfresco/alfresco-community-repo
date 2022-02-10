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

import static java.time.temporal.ChronoUnit.MINUTES;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.audit.AuditEvents;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Produces processed results from RM Audit REST API calls
 *
 * @author Claudia Agache
 * @since 3.3
 */
@Service
public class RMAuditService
{
    @Autowired
    private RMAuditAPI rmAuditAPI;

    @Autowired
    private DataUserAIS dataUser;

    /**
     * Clear the list of audit entries as admin user.
     */
    public void clearAuditLog()
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword());
    }

    /**
     * Returns a list of rm audit entries filtered by given event
     *
     * @param user       the user who requests the list of rm audit entries
     * @param auditEvent the event
     * @return the list of audit entries matching the event
     */
    public List<AuditEntry> getAuditEntriesFilteredByEvent(UserModel user, AuditEvents auditEvent)
    {
        STEP("Get the list of audit entries for the " + auditEvent.eventDisplayName + " event.");
        return rmAuditAPI.getRMAuditLog(user.getUsername(), user.getPassword(), 100, auditEvent.event);
    }

    /**
     * Checks the rm audit log contains the entry for the given event.
     *
     * @param user          the user who checks the audit log
     * @param auditEvent    the audited event
     * @param auditUser     the user who did the audited event
     * @param nodeName      the audited node name if exists or empty string
     * @param changedValues the values changed by event if exist or empty list
     */
    public void checkAuditLogForEvent(UserModel user, AuditEvents auditEvent, UserModel auditUser,
                                         String nodeName, List<Object> changedValues)
    {

        List<AuditEntry> auditEntries = getAuditEntriesFilteredByEvent(user, auditEvent);
        assertTrue("The list of events is not filtered by " + auditEvent.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(auditEvent.eventDisplayName)));
        final LocalDateTime eventTimestamp =
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).truncatedTo(MINUTES);
        assertTrue("The event details are not audited",
                auditEntries.stream().anyMatch(auditEntry -> auditEntry.getNodeName().equals(nodeName) &&
                        auditEntry.getUserName().equals(auditUser.getUsername()) &&
                        CollectionUtils.isEqualCollection(auditEntry.getChangedValues(), changedValues) &&
                        !auditEntry.getTimestamp().isEmpty() &&
                        (LocalDateTime.ofInstant(Instant.parse(auditEntry.getTimestamp()), ZoneId.systemDefault()).truncatedTo(MINUTES)
                                      .compareTo(eventTimestamp) <= 0))
                  );
    }

    /**
     * Checks the rm audit log contains the entry for the given event.
     *
     * @param user          the user who checks the audit log
     * @param auditEvent    the audited event
     * @param auditUser     the user who did the audited event
     * @param nodeName      the audited node name if exists or empty string
     * @param nodePath      the path of the audited node if exists or empty string
     * @param changedValues the values changed by event if exist or empty list
     */
    public void checkAuditLogForEvent(UserModel user, AuditEvents auditEvent, UserModel auditUser,
                                         String nodeName, String nodePath, List<Object> changedValues)
    {
        List<AuditEntry> auditEntries = getAuditEntriesFilteredByEvent(user, auditEvent);
        assertTrue("The list of events is not filtered by " + auditEvent.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(auditEvent.eventDisplayName)));
        final LocalDateTime eventTimestamp =
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).truncatedTo(MINUTES);
        assertTrue("The event details are not audited",
                auditEntries.stream().anyMatch(auditEntry -> auditEntry.getNodeName().equals(nodeName) &&
                        auditEntry.getUserName().equals(auditUser.getUsername()) &&
                        auditEntry.getPath().equals(nodePath) &&
                        CollectionUtils.isEqualCollection(auditEntry.getChangedValues(), changedValues) &&
                        !auditEntry.getTimestamp().isEmpty() &&
                        (LocalDateTime.ofInstant(Instant.parse(auditEntry.getTimestamp()), ZoneId.systemDefault()).truncatedTo(MINUTES)
                                      .compareTo(eventTimestamp) <= 0))
                  );
    }
}
