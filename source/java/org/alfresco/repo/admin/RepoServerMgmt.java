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
package org.alfresco.repo.admin;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.linkvalidation.LinkValidationService;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepoServerMgmt implements RepoServerMgmtMBean
{
    private static final Log log = LogFactory.getLog(RepoServerMgmt.class);

    private TransactionServiceImpl transactionService;

    private AbstractAuthenticationService authenticationService;

    private LinkValidationService linkValidationService;

    public void setTransactionService(TransactionServiceImpl transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAuthenticationService(AbstractAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    // TODO - temporary workaround, can be removed when link validation is part of repo
    public void registerLinkValidationService(LinkValidationService linkValidationService)
    {
        this.linkValidationService = linkValidationService;
    }

    public boolean isReadOnly()
    {
        return transactionService.isReadOnly();
    }

    // Note: implementing counts as managed attributes (without params) means that
    // certain JMX consoles can monitor

    public int getTicketCountNonExpired()
    {
        return authenticationService.countTickets(true);
    }

    public int getTicketCountAll()
    {
        return authenticationService.countTickets(false);
    }

    public int getUserCountNonExpired()
    {
        return authenticationService.getUsersWithTickets(true).size();
    }

    public int getUserCountAll()
    {
        return authenticationService.getUsersWithTickets(false).size();
    }

    // Note: implement operations without boolean/Boolean parameter, due to problem with some JMX consoles (e.g. MC4J
    // 1.9 Beta)

    public String[] listUserNamesNonExpired()
    {
        Set<String> userSet = authenticationService.getUsersWithTickets(true);
        SortedSet<String> sorted = new TreeSet<String>(userSet);
        return sorted.toArray(new String[0]);
    }

    public String[] listUserNamesAll()
    {
        Set<String> userSet = authenticationService.getUsersWithTickets(false);
        SortedSet<String> sorted = new TreeSet<String>(userSet);
        return sorted.toArray(new String[0]);
    }

    public int invalidateTicketsExpired()
    {
        int count = authenticationService.invalidateTickets(true);
        log.info("Expired tickets invalidated: " + count);
        return count;
    }

    public int invalidateTicketsAll()
    {
        int count = authenticationService.invalidateTickets(false);
        log.info("All tickets invalidated: " + count);
        return count;
    }

    public void invalidateUser(String username)
    {
        authenticationService.invalidateUserSession(username);
        log.info("User invalidated: " + username);
    }

    public int getMaxUsers()
    {
        return authenticationService.getMaxUsers();
    }

    public boolean isLinkValidationDisabled()
    {
        if (linkValidationService == null)
        {
            log.error("LinkValidationService not registered");
            throw new AlfrescoRuntimeException("LinkValidationService not registered");
        }

        return linkValidationService.isLinkValidationDisabled();
    }
}
