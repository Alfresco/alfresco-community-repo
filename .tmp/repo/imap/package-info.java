/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
/**
 * The implementation of the Alfresco Imap Server
 * 
 * <p>
 * AlfrescoImapServer which implements the IMAP protocol.  It contains an instance of the Ice Green ImapServer and delegates imap commands to 
 * AlfrescoImapHostManager and AlfrescoImapUserManager.    AlfrescoImapHostManager in turn delegates to ImapService and AlfrescoImapUserManager uses the PersonService. 
 * 
 * <p>
 * ImapServiceImpl provides the implementation of the various IMAP commands on an alfresco repository. Also contains the transaction and security boundary.
 * 
 * <p>
 * AlfrescoImapFolder contains the implementation of IMAPFolders and contains messages.
 * 
 * @since 3.2
 * 
 */
@PackageMarker
package org.alfresco.repo.imap;
import org.alfresco.util.PackageMarker;
