/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.fileplancomponents;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.ALLOWABLE_OPERATIONS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_COMPLETED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.RELATIVE_PATH;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for file plan component
 *
 * @author Tuna Aksoy
 * @author Rodica Sutu
 * @since 2.6
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePlanComponent
{
    @JsonProperty (required = true)
    private String id;

    @JsonProperty (required = true)
    private String parentId;

    @JsonProperty (required = true)
    private String name;

    @JsonProperty (required = true)
    private String nodeType;

    @JsonProperty (required = true)
    private Boolean isCategory;

    @JsonProperty (required = true)
    private Boolean isRecordFolder;

    @JsonProperty (required = true)
    private Boolean isFile;

    @JsonProperty
    private Boolean hasRetentionSchedule;

    @JsonProperty(value = IS_CLOSED)
    private Boolean isClosed;

    @JsonProperty(value = IS_COMPLETED)
    private Boolean isCompleted;

    @JsonProperty (required = true)
    private List<String> aspectNames;

    @JsonProperty (required = true)
    private FilePlanComponentUserInfo createdByUser;

    @JsonProperty(value = PROPERTIES)
    private FilePlanComponentProperties properties;

    @JsonProperty (value = ALLOWABLE_OPERATIONS)
    private List<String> allowableOperations;

    @JsonProperty (required = false)
    private FilePlanComponentContent content;

    @JsonProperty (value = PATH)
    private FilePlanComponentPath path;

    @JsonProperty (required = true)
    private String modifiedAt;

    @JsonProperty (required = true)
    private String createdAt;

    @JsonProperty (required = true)
    private FilePlanComponentUserInfo modifiedByUser;

    @JsonProperty (value = RELATIVE_PATH)
    private String relativePath;

}
