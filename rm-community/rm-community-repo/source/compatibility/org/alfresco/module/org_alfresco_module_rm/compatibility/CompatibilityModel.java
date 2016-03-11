package org.alfresco.module.org_alfresco_module_rm.compatibility;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.service.namespace.QName;

/**
 * RM 1.0 compatibility model
 *
 * @author Roy Wetherall
 */
public interface CompatibilityModel extends DOD5015Model
{
    // Record series DOD type
    QName TYPE_RECORD_SERIES = QName.createQName(DOD_URI, "recordSeries");

    // V1.0 custom property aspect names
    String NAME_CUSTOM_RECORD_PROPERTIES = "customRecordProperties";
    String NAME_CUSTOM_RECORD_FOLDER_PROPERTIES = "customRecordFolderProperties";
    String NAME_CUSTOM_RECORD_CATEGORY_PROPERTIES = "customRecordCategoryProperties";
    String NAME_CUSTOM_RECORD_SERIES_PROPERTIES = "customRecordSeriesProperties";
}
