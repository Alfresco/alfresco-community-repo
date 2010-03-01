/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.search.impl.querymodel.impl;

import org.alfresco.repo.search.impl.querymodel.Constraint;

public abstract class BaseConstraint implements Constraint
{

    private Occur occur = Occur.DEFAULT;
    
    private float boost = 1.0f;
    
    public BaseConstraint()
    {
       this.occur = occur;
    }

    public Occur getOccur()
    {
      return occur;
    }
    
    public void setOccur(Occur occur)
    {
        this.occur = occur;
    }

    public float getBoost()
    {
        return boost;
    }

    public void setBoost(float boost)
    {
        this.boost = boost;
    }

}
