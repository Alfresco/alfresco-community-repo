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
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Fileplan component properties
 * @author Kristijan Conkas
 * @since 2.6
 */
@Component
@Scope(value = "prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertiesModel
{
    // TODO: handling individual properties is tedious and error prone, how about @JsonGetter + @JsonSetter?
    
    @JsonProperty("rma:vitalRecordIndicator")
    private boolean vitalRecord;
    
    @JsonProperty("cm:title")
    private String title;
    
    @JsonProperty("rma:holdReason")
    private String holdReason;
    
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
}
