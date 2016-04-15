package org.alfresco.service.cmr.model;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * The type for a file folder 
 * 
 * @author andyh
 *
 */
@AlfrescoPublicApi
public enum FileFolderServiceType
{
    FILE, FOLDER, SYSTEM_FOLDER, INVALID;
}
