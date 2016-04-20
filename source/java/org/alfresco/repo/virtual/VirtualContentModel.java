
package org.alfresco.repo.virtual;

import org.alfresco.service.namespace.QName;

/**
 * Virtual Content Model Constants
 *
 * @author Bogdan Horje
 */
public interface VirtualContentModel
{
    static final String VIRTUAL_CONTENT_MODEL_1_0_URI = "http://www.alfresco.org/model/content/smartfolder/1.0";

    static final QName ASPECT_VIRTUAL = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                          "smartFolder");

    static final QName ASPECT_VIRTUAL_DOCUMENT = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                   "smartFolderChild");

    static final QName TYPE_VIRTUAL_FOLDER_TEMPLATE = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                        "smartFolderTemplate");

    static final QName PROP_ACTUAL_NODE_REF = QName.createQName(VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                                "actualNodeRef");
}
