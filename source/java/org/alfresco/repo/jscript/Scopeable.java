/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript;

import org.mozilla.javascript.Scriptable;

/**
 * Interface contract for objects that supporting setting of the global scripting scope.
 * This is used to mark objects that are not themselves natively scriptable (i.e. they are
 * wrapped Java objects) but need to access the global scope for the purposes of JavaScript
 * object creation etc.
 * 
 * @author Kevin Roast
 */
public interface Scopeable
{
    /**
     * Set the Scriptable global scope
     * 
     * @param scope relative global scope
     */
    void setScope(Scriptable scope);
}
