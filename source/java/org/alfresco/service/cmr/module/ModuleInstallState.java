package org.alfresco.service.cmr.module;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Enum used to indicate the install state of a module.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public enum ModuleInstallState
{
    /** The state of the module is unknown */
    UNKNOWN,
    /** The module is installed */
    INSTALLED,
    /** The module is disabled */
    DISABLED,
    /** The module has been uninstalled */
    UNINSTALLED;
}
