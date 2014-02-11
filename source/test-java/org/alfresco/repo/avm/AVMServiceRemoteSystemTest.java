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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.test_category.LegacyCategory;
import org.alfresco.util.NameMatcher;
import org.junit.experimental.categories.Category;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Remote system tests of AVM (AVMSyncService & AVMService) - requires running repo
 */
@Category(LegacyCategory.class)
public class AVMServiceRemoteSystemTest extends AVMServiceLocalTest
{
    private final static String ADMIN_UN = "admin";
    private final static String ADMIN_PW = "admin";
    
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            // remote (non-embedded) test setup
            fContext = new FileSystemXmlApplicationContext("config/alfresco/remote-avm-test-context.xml");
            fService = (AVMRemote)fContext.getBean("avmRemote");
            fSyncService = (AVMSyncService)fContext.getBean("avmSyncService");
            excluder = (NameMatcher) fContext.getBean("globalPathExcluder");
            
            AuthenticationService authService = (AuthenticationService)fContext.getBean("authenticationService");
            authService.authenticate(ADMIN_UN, ADMIN_PW.toCharArray());
            String ticket = authService.getCurrentTicket();
            ((ClientTicketHolder)fContext.getBean("clientTicketHolder")).setTicket(ticket);
        }
        
        super.setUp();
    }
}
