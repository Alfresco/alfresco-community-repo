/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;

/**
 * Mix class for Record POJO class
 * Mix-in annotations are: a way to associate annotations with classes
 * without modifying (target) classes themselves.
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public abstract class UnfiledContainerChildMixin
{
    /**
     * Annotation used to indicate that a property should be serialized "unwrapped"
     * Its properties are instead included as properties of its containing Object
     */
    @JsonUnwrapped
    abstract UnfiledContainerChildProperties getProperties();
}
