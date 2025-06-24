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
package org.alfresco.repo.imap;

/**
 * @author Mike Shavnev
 * @since 3.2
 */
public interface AlfrescoImapConst
{
    /**
     * @author Derek Hulley
     * @since 3.2
     */
    public static enum ImapViewMode
    {
        /**
         * Defines {@link AlfrescoImapFolder} view mode as virtual mode. Used for IMAP Virtualised View.
         * <p>
         * In the virtual mode alfresco nodes of cm:content are shown regardless of whether they are IMAP messages.
         * <p>
         * A template is used to generate a "virtual" email message from a content item that is not an email message.
         * <p>
         * Only nodes from IMAP favourite sites are shown, non favourite sites are not shown.
         */
        VIRTUAL,
        /**
         * Defines {@link AlfrescoImapFolder} view mode as mixed mode. Used for IMAP Mixed View.
         * <p>
         * In mixed mode both IMAP messages and Alfresco nodes of other types are shown. Only nodes from IMAP favourite sites are shown, non favourite sites are not shown.
         * 
         */
        MIXED,
        /**
         * Defines {@link AlfrescoImapFolder} view mode as archive mode. Used for Email Archive View.
         * <p>
         * In archive mode only IMAP messages are shown. Alfresco nodes of other types are not shown. And no nodes within sites (favourite or otherwise) are shown.
         */
        ARCHIVE
    }

    public static final char HIERARCHY_DELIMITER = '/';
    public static final String NAMESPACE_PREFIX = "#";
    public static final String USER_NAMESPACE = "#mail";
    public static final String INBOX_NAME = "INBOX";
    public static final String TRASH_NAME = "Trash";

    public static final String BODY_TEXT_PLAIN_NAME = "Body.txt";
    public static final String BODY_TEXT_HTML_NAME = "Body.html";
    public static final String MESSAGE_PREFIX = "Message_";
    public static final String EML_EXTENSION = ".eml";
    // Separator for user enties in flag and subscribe properties
    public static final String USER_SEPARATOR = ";";

    // Default content model email message templates
    public static final String CLASSPATH_ALFRESCO_TEXT_PLAIN_TEMPLATE = "/alfresco/templates/imap/emailbody_textplain_alfresco.ftl";
    public static final String CLASSPATH_SHARE_TEXT_PLAIN_TEMPLATE = "/alfresco/templates/imap/emailbody_textplain_share.ftl";

    public static final String CLASSPATH_ALFRESCO_TEXT_HTML_TEMPLATE = "/alfresco/templates/imap/emailbody_texthtml_alfresco.ftl";
    public static final String CLASSPATH_SHARE_TEXT_HTML_TEMPLATE = "/alfresco/templates/imap/emailbody_texthtml_share.ftl";

    public static final String DICTIONARY_TEMPLATE_PREFIX = "emailbody";
    public static final String PREF_IMAP_FAVOURITE_SITES = "org.alfresco.share.sites.imapFavourites";

    // AlfrescoImapMessage constants
    public static final String MIME_VERSION = "MIME-Version";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String MULTIPART_MIXED = "mixed";
    public static final String CONTENT_ID = "Content-ID";
    public static final String X_ALF_NODEREF_ID = "X-Alfresco-NodeRef-ID"; // The NodeRef id header
    public static final String X_ALF_SERVER_UID = "X-Alfresco-Server-UID"; // The unique identifier of Alfresco server
    public static final String EIGHT_BIT_ENCODING = "8bit";
    public static final String BASE_64_ENCODING = "base64";
    public static final String UTF_8 = "UTF-8";
    public static final String CHARSET_UTF8 = ";charset=utf-8";

}
