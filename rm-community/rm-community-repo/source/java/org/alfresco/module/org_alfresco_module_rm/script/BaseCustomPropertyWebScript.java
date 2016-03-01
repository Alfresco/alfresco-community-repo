 
package org.alfresco.module.org_alfresco_module_rm.script;

import org.alfresco.module.org_alfresco_module_rm.compatibility.CompatibilityModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all custom property webscripts.
 *
 * @author Roy Wetherall
 */
public class BaseCustomPropertyWebScript extends AbstractRmWebScript
{
    /**
     * Takes the element name and maps it to the QName of the customisable type.  The passed element name should be a prefixed
     * qname string, but to support previous versions of this API a couple of hard coded checks are made first.
     *
     * @param elementName
     * @return
     */
    protected QName mapToTypeQName(String elementName)
    {
        // Direct matching provided for backward compatibility with RM 1.0
        if ("recordFolder".equalsIgnoreCase(elementName))
        {
            return RecordsManagementModel.TYPE_RECORD_FOLDER;
        }
        else if ("record".equalsIgnoreCase(elementName))
        {
            return RecordsManagementModel.ASPECT_RECORD;
        }
        else if ("recordCategory".equalsIgnoreCase(elementName))
        {
            return RecordsManagementModel.TYPE_RECORD_CATEGORY;
        }
        else if ("recordSeries".equalsIgnoreCase(elementName))
        {
            return CompatibilityModel.TYPE_RECORD_SERIES;
        }
        else
        {
            // Try and convert the string to a qname
            return QName.createQName(elementName, getNamespaceService());
        }
    }
}