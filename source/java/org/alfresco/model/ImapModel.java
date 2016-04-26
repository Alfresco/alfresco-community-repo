package org.alfresco.model;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.service.namespace.QName;

/**
 * IMAP Model Constants
 * 
 * @author Mike Shavnev
 */
@AlfrescoPublicApi
public interface ImapModel
{
    static final String IMAP_MODEL_1_0_URI = "http://www.alfresco.org/model/imap/1.0";

    static final QName ASPECT_IMAP_FOLDER_NONSUBSCRIBED = QName.createQName(IMAP_MODEL_1_0_URI, "nonSubscribed");
    static final QName ASPECT_IMAP_FOLDER_NONSELECTABLE = QName.createQName(IMAP_MODEL_1_0_URI, "nonSelectable");
    static final QName ASPECT_IMAP_FOLDER = QName.createQName(IMAP_MODEL_1_0_URI, "imapFolder");

    static final QName ASPECT_IMAP_CONTENT = QName.createQName(IMAP_MODEL_1_0_URI, "imapContent");
    static final QName PROP_MESSAGE_FROM = QName.createQName(IMAP_MODEL_1_0_URI, "messageFrom");
    static final QName PROP_MESSAGE_TO = QName.createQName(IMAP_MODEL_1_0_URI, "messageTo");
    static final QName PROP_MESSAGE_CC = QName.createQName(IMAP_MODEL_1_0_URI, "messageCc");
    static final QName PROP_MESSAGE_SUBJECT = QName.createQName(IMAP_MODEL_1_0_URI, "messageSubject");
    static final QName PROP_MESSAGE_ID = QName.createQName(IMAP_MODEL_1_0_URI, "messageId");
    static final QName PROP_THREAD_INDEX = QName.createQName(IMAP_MODEL_1_0_URI, "threadIndex");
    static final QName ASSOC_IMAP_ATTACHMENT = QName.createQName(IMAP_MODEL_1_0_URI, "attachment");
    static final QName ASSOC_IMAP_ATTACHMENTS_FOLDER = QName.createQName(IMAP_MODEL_1_0_URI, "attachmentsFolder");

    static final QName ASPECT_FLAGGABLE = QName.createQName(IMAP_MODEL_1_0_URI, "flaggable");
    static final QName PROP_FLAG_ANSWERED = QName.createQName(IMAP_MODEL_1_0_URI, "flagAnswered");
    static final QName PROP_FLAG_DELETED = QName.createQName(IMAP_MODEL_1_0_URI, "flagDeleted");
    static final QName PROP_FLAG_DRAFT = QName.createQName(IMAP_MODEL_1_0_URI, "flagDraft");
    static final QName PROP_FLAG_SEEN = QName.createQName(IMAP_MODEL_1_0_URI, "flagSeen");
    static final QName PROP_FLAG_RECENT = QName.createQName(IMAP_MODEL_1_0_URI, "flagRecent");
    static final QName PROP_FLAG_FLAGGED = QName.createQName(IMAP_MODEL_1_0_URI, "flagFlagged");

    static final QName TYPE_IMAP_BODY = QName.createQName(IMAP_MODEL_1_0_URI, "imapBody");

    static final QName TYPE_IMAP_ATTACH = QName.createQName(IMAP_MODEL_1_0_URI, "imapAttach");
    static final QName PROP_ATTACH_ID = QName.createQName(IMAP_MODEL_1_0_URI, "attachID");

    static final QName PROP_UIDVALIDITY = QName.createQName(IMAP_MODEL_1_0_URI, "uidValidity");
    static final QName PROP_MAXUID = QName.createQName(IMAP_MODEL_1_0_URI, "maxUid");
    static final QName PROP_CHANGE_TOKEN = QName.createQName(IMAP_MODEL_1_0_URI, "changeToken");

    
    static final QName ASPECT_IMAP_PREFERENCES = QName.createQName(IMAP_MODEL_1_0_URI, "imapPreferences");
    static final QName ASSOC_IMAP_UNSUBSCRIBED = QName.createQName(IMAP_MODEL_1_0_URI, "imapUnsubscribed");
    
    static final QName ASPECT_IMAP_MESSAGE_HEADERS = QName.createQName(IMAP_MODEL_1_0_URI, "messageHeaders");
    static final QName PROP_MESSAGE_HEADERS = QName.createQName(IMAP_MODEL_1_0_URI, "messageHeaders");
    
    static final String MESSAGE_HEADER_TO_PERSIST_SPLITTER = ":";

}
