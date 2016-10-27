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
package org.alfresco.rest.model.fileplancomponents;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.RestModels;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponent;

/**
 * POJO for file plan component entry
 *
 * @author Tuna Aksoy
 * @since 1.0
 */
public class FilePlanComponentEntry extends RestModels<FilePlanComponent, FilePlanComponentEntry>
{
    @JsonProperty(value = "entry")
    FilePlanComponent filePlanComponent;

    public FilePlanComponent getFilePlanComponent()
    {
        return filePlanComponent;
    }
}
