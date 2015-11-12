/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */
package org.alfresco.traitextender;

import java.util.List;


public abstract class Extender
{
    private static Extender instance;
    
    public static class  Configuration {
        private boolean enabled;
        private List<String> disbaledBundles;
        
        
    }

    public static synchronized Extender getInstance()
    {
        if (instance == null)
        {
            instance = new ExtenderImpl();
        }
        return instance;
    }

    public abstract void start(ExtensionBundle bundle);
   
    public abstract void stop(ExtensionBundle bundle);
    
    public abstract void stopAll();

    public abstract <E, M extends Trait> E getExtension(Extensible anExtensible, ExtensionPoint<E, M> point);
    
    public abstract void register(ExtensionPoint<?, ?> point, ExtensionFactory<?> factory);
    
    public abstract void unregister(ExtensionPoint<?, ?> point);
    
}
