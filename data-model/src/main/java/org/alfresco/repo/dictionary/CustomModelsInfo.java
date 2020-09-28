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

package org.alfresco.repo.dictionary;

/**
 * A simple POJO to encapsulate the custom models' statistics information.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelsInfo
{
    private int numberOfActiveModels;
    private int numberOfActiveTypes;
    private int numberOfActiveAspects;

    public int getNumberOfActiveModels()
    {
        return this.numberOfActiveModels;
    }

    public void setNumberOfActiveModels(int numberOfActiveModels)
    {
        this.numberOfActiveModels = numberOfActiveModels;
    }

    public int getNumberOfActiveTypes()
    {
        return this.numberOfActiveTypes;
    }

    public void setNumberOfActiveTypes(int numberOfActiveTypes)
    {
        this.numberOfActiveTypes = numberOfActiveTypes;
    }

    public int getNumberOfActiveAspects()
    {
        return this.numberOfActiveAspects;
    }

    public void setNumberOfActiveAspects(int numberOfActiveAspects)
    {
        this.numberOfActiveAspects = numberOfActiveAspects;
    }
}
