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
package org.alfresco.rest.rm.community.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.alfresco.rest.rm.community.model.common.ReviewPeriod;

/**
 * Utility class for serializing @{FilePlanComponentReviewPeriod}
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class ReviewPeriodSerializer extends JsonSerializer<ReviewPeriod>
{
    /**
     * @param value The Review Period value that is being serialized.
     * @param gen Jackson utility is responsible for writing JSON
     * @param serializers Provider for getting access to other serializers and configurations registered with the ObjectMapper.
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public void serialize(ReviewPeriod value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException
    {
        //create the custom  string value for the Review Period type
       gen.writeString(new StringBuilder().append(value.getPeriodType()).append("|").append(value.getExpression()).toString());
    }
}
