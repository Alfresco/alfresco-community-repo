/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.imap;

/**
 * @author Mike Shavnev
 */
public interface AlfrescoImapConst
{

    public static final char HIERARCHY_DELIMITER = '/';
    public static final String NAMESPACE_PREFIX = "#";
    public static final String USER_NAMESPACE = "#mail";
    public static final String INBOX_NAME = "INBOX";

    public static final String BODY_TEXT_PLAIN_NAME = "Body.txt";
    public static final String BODY_TEXT_HTML_NAME = "Body.html";
    public static final String MESSAGE_PREFIX = "Message_";
    public static final String EML_EXTENSION = ".eml";
    // Separator for user enties in flag and subscribe properties
    public static final String USER_SEPARATOR = ";";

    /**
     * Defines {@link AlfrescoImapFolder} view mode as archive mode. Used for Email Archive View.
     */
    public static final String MODE_ARCHIVE = "archive";
    /**
     * Defines {@link AlfrescoImapFolder} view mode as virtual mode. Used for IMAP Virtualised View.
     */
    public static final String MODE_VIRTUAL = "virtual";
    /**
     * Defines {@link AlfrescoImapFolder} view mode as mixed mode. Used for IMAP Mixed View.
     */
    public static final String MODE_MIXED = "mixed";


    // Default content model email message templates
    public static final String CLASSPATH_TEXT_PLAIN_TEMPLATE = "/alfresco/templates/imap/imap_message_text_plain.ftl";
    public static final String CLASSPATH_TEXT_HTML_TEMPLATE = "/alfresco/templates/imap/imap_message_text_html.ftl";

    public static final String DICTIONARY_TEMPLATE_PREFIX = "emailbody";
    public static final String PREF_IMAP_FAVOURITE_SITES = "org.alfresco.share.sites.imap.favourites";

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

}
