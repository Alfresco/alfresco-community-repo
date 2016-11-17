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

import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.ENTRY;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.RestModels;

/**
 * POJO for file plan component entry
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponentEntry extends RestModels<FilePlanComponent, FilePlanComponentEntry>
{
    @JsonProperty(ENTRY)
    FilePlanComponent filePlanComponent;

    public FilePlanComponent getFilePlanComponent()
    {
        return filePlanComponent;
    }
}
