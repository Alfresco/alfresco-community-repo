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
package org.alfresco.repo.imap;
