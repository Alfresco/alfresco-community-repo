/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.alfresco.rest.model.RestByUserModel;
import org.alfresco.rest.rm.community.model.common.Path;
import org.alfresco.utility.model.TestModel;

/**
 * POJO for transfer child
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransferChild extends TestModel
{
    /*************************/
    /** Mandatory parameters */
    /*************************/
    @JsonProperty(required = true)
    private String createdAt;

    @JsonProperty(required = true)
    private RestByUserModel createdByUser;

    @JsonProperty(required = true)
    private String modifiedAt;

    @JsonProperty(required = true)
    private RestByUserModel modifiedByUser;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String nodeType;

    @JsonProperty(required = true)
    private String parentId;

    /************************/
    /** Optional parameters */
    /************************/
    @JsonProperty
    private TransferChildProperties properties;

    @JsonProperty
    private Boolean isRecord;

    @JsonProperty
    private Boolean isRecordFolder;

    @JsonProperty
    private List<String> aspectNames;

    @JsonProperty
    private Boolean isCompleted;

    @JsonProperty
    private Boolean isClosed;

    @JsonProperty
    private List<String> allowableOperations;

    @JsonProperty
    private Path path;
}
