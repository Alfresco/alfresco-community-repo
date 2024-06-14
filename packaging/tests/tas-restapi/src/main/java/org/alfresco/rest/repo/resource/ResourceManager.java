/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.repo.resource;

import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.ResourceIntroducer;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.model.TestModel;

/**
 * Declares operations introducing new, or allowing to manage repository resources like: folders, files, categories, associations, etc.
 *
 * @param <RESOURCE>> repository resource, e.g. folder, file, category, etc.
 * @param <SPECIFIER> repository resource specifier, see {@link Specifier}
 * @param <MODIFIER>> repository resource modifier, see {@link Modifier}
 */
public interface ResourceManager<RESOURCE extends TestModel, SPECIFIER extends Specifier, MODIFIER extends Modifier<RESOURCE, ?>>
    extends ResourceIntroducer<SPECIFIER>
{
    RESOURCE get(String id);

    MODIFIER modify(RESOURCE resource);

    default MODIFIER modify(String id)
    {
        return modify(get(id));
    }

    void delete(RESOURCE resource);

    default void delete(String id)
    {
        delete(get(id));
    }
}
