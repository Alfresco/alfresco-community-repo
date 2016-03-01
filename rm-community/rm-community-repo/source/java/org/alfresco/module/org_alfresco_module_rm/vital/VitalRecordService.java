 
package org.alfresco.module.org_alfresco_module_rm.vital;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;

/**
 * Vital Record Service.
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public interface VitalRecordService
{
    /** Period 'none' */
    Period PERIOD_NONE = new Period("none|0");

    /**
     * Setup the vital record definition for the given node.
     *
     * @param nodeRef   node reference
     */
    void setupVitalRecordDefinition(NodeRef nodeRef);

    /**
     * Gets the vital record definition details for the node.
     *
     * @param nodeRef                   node reference
     * @return VitalRecordDefinition    vital record definition details
     */
    VitalRecordDefinition getVitalRecordDefinition(NodeRef nodeRef);

    /**
     * Sets the vital record definition values for a given node.
     *
     * @param nodeRef
     * @param enabled
     * @param reviewPeriod
     * @return
     */
    VitalRecordDefinition setVitalRecordDefintion(NodeRef nodeRef, boolean enabled, Period reviewPeriod);

    /**
     * Indicates whether the record is a vital one or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if this is a vital record, false otherwise
     */
    boolean isVitalRecord(NodeRef nodeRef);

    /**
     * Initialises the vital record details.
     *
     * @param nodeRef   node reference to initialise with vital record details
     */
    //void initialiseVitalRecord(NodeRef nodeRef);
}
