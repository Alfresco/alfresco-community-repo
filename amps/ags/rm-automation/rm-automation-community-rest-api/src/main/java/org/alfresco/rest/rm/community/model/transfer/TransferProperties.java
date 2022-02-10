/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.rm.community.model.transfer;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_IDENTIFIER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_OWNER;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ROOT_NODE_REF;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_PDF_INDICATOR;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TRANSFER_LOCATION;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_ACCESSION_INDICATOR;

import org.alfresco.rest.rm.community.model.common.Owner;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * POJO for Transfer properties
 *
 * @author Dinuta Silviu
 * @since 2.6
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransferProperties extends TestModel
{
    /*************************/
    /** Mandatory parameters */
    /*************************/
    @JsonProperty (required = true, value = PROPERTIES_IDENTIFIER)
    private String identifier;

    /************************/
    /** Optional parameters */
    /************************/
    @JsonProperty (PROPERTIES_ROOT_NODE_REF)
    private String rootNodeRef;

    @JsonProperty (PROPERTIES_OWNER)
    private Owner owner;

    @JsonProperty (PROPERTIES_PDF_INDICATOR)
    private Boolean pdfIndicator;

    @JsonProperty (PROPERTIES_TRANSFER_LOCATION)
    private String transferLocation;

    @JsonProperty (PROPERTIES_ACCESSION_INDICATOR)
    private Boolean accessionIndicator;
}
