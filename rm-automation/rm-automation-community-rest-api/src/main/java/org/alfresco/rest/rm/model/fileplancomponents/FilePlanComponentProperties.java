/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.model.fileplancomponents;

import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_HOLD_REASON;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VITAL_RECORD_INDICATOR;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for file plan component properties
 *
 * @author Kristijan Conkas
 * @since 1.0
 */
//FIXME: Once the fields have been added the JsonIgnoreProperties annotation should be removed
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilePlanComponentProperties
{
    @JsonProperty(PROPERTIES_VITAL_RECORD_INDICATOR)
    private boolean vitalRecord;

    @JsonProperty(PROPERTIES_TITLE)
    private String title;

    @JsonProperty(PROPERTIES_HOLD_REASON)
    private String holdReason;

    @JsonProperty(PROPERTIES_DESCRIPTION)
    private String description;

    /**
     * @return the vitalRecord
     */
    public boolean isVitalRecord()
    {
        return this.vitalRecord;
    }

    /**
     * @param vitalRecord the vitalRecord to set
     */
    public void setVitalRecord(boolean vitalRecord)
    {
        this.vitalRecord = vitalRecord;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the holdReason
     */
    public String getHoldReason()
    {
        return this.holdReason;
    }

    /**
     * @param holdReason the holdReason to set
     */
    public void setHoldReason(String holdReason)
    {
        this.holdReason = holdReason;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }
}
