/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.jscript;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.mozilla.javascript.Scriptable;

/**
 * Abstract base class for a script implementation that requires a script execution scope.
 * 
 * The scope is local to the currently executing script and therefore a ThreadLocal is required.
 * 
 * @author Kevin Roast
 */
public class BaseScopableProcessorExtension extends BaseProcessorExtension implements Scopeable
{
    private static ThreadLocal<Scriptable> scope = new ThreadLocal<Scriptable>();
    
    /**
     * Set the Scriptable global scope
     * 
     * @param script relative global scope
     */
    public void setScope(Scriptable scope)
    {
        BaseScopableProcessorExtension.scope.set(scope);
    }
    
    /**
     * @return script global scope
     */
    public Scriptable getScope()
    {
        return BaseScopableProcessorExtension.scope.get();
    }
}
