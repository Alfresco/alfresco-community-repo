/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.model.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.service.Experimental;
import org.apache.commons.lang3.NotImplementedException;

@Experimental
public interface RestModelMapper<R, S>
{
    default R toRestModel(S serviceModel) {
        throw new NotImplementedException();
    }
    default S toServiceModel(R restModel) {
        throw new NotImplementedException();
    }
    default R toRestModel(Collection<S> serviceModels) {
        throw new NotImplementedException();
    }
    default S toServiceModel(Collection<R> restModels) {
        throw new NotImplementedException();
    }
    default List<R> toRestModels(Collection<S> serviceModels) {
        return serviceModels.stream()
                .filter(Objects::nonNull)
                .map(this::toRestModel)
                .collect(Collectors.toList());
    }
    default List<S> toServiceModels(Collection<R> restModels) {
        return restModels.stream()
                .filter(Objects::nonNull)
                .map(this::toServiceModel)
                .collect(Collectors.toList());
    }
    default List<R> toRestModels(S serviceModel) {
        throw new NotImplementedException();
    }
    default List<S> toServiceModels(R restModel) {
        throw new NotImplementedException();
    }
}
