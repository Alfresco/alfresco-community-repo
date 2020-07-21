/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.service.cmr.dictionary;

import java.util.Collection;

/**
 * @author Jamal Kaabi-Mofrad
 */
public interface CustomModelDefinition extends ModelDefinition
{

    /**
     * Whether the model is active or not
     *
     * @return true if the model is active, false otherwise
     */
    public boolean isActive();

    /**
     * Returns the model description
     *
     * @return the model description
     */
    public String getDescription();

    /**
     * Returns a {@link Collection} of the model {@link TypeDefinition}s
     *
     * @return an unmodifiable collection of the model types definitions, or an empty collection
     */
    public Collection<TypeDefinition> getTypeDefinitions();

    /**
     * Returns a {@link Collection} of the model {@link AspectDefinition}s
     *
     * @return an unmodifiable collection of the model aspects definitions, or an empty collection
     */
    public Collection<AspectDefinition> getAspectDefinitions();

    /**
     * Returns a {@link Collection} of the model defined {@link ConstraintDefinition}s
     *
     * @return an unmodifiable collection of the model constraint definitions, or an empty collection
     */
    public Collection<ConstraintDefinition> getModelDefinedConstraints();
}
