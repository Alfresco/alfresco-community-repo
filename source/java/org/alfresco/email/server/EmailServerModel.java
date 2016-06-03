package org.alfresco.email.server;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class defines the costants for Email Server Model
 * 
 * @see alfresco/model/emailServerModel.xml
 * @author Yan O
 * @since 2.2
 */
public interface EmailServerModel
{
    // Attachable aspect
    static final QName ASPECT_ATTACHED = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "attached");

    static final QName ASSOC_ATTACHMENT = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "attachment");

    // Aliasable aspect
    static final QName ASPECT_ALIASABLE = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "aliasable");

    static final QName PROP_ALIAS = QName.createQName(NamespaceService.EMAILSERVER_MODEL_URI, "alias");

}
