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

/**
 * Fileplan Component Types enum
 * @author Kristijan Conkas
 * @since 2.6
 */
public enum FileplanComponentType
{
    CATEGORY("rma:recordCategory"),
    FOLDER("rma:recordFolder"),
    HOLD("rma:hold"),
    UNFILED_RECORD_FOLDER("rma:unfiledRecordFolder");
    
    private String value;
    
    FileplanComponentType (String value)
    {
        this.value = value;
    }
    
    public String toString()
    {
        return this.value;
    }
}
