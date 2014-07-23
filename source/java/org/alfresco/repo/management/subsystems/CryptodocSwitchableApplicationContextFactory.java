/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.management.subsystems;

import java.io.IOException;


/**
 * {@link SwitchableApplicationContextFactory} that only allows the subsystem to be switched
 * if the current subsystem is the unencrypted content store. When an attempt is made to switch away
 * from any other store (e.g. the encrypted store) then nothing happens. This is achieved by returning
 * <code>false</code> for {@link #isUpdateable(String)} calls when the current sourceBeanName is
 * that of the unencrypted content store's subsystem.
 *  
 * @author Matt Ward
 */
public class CryptodocSwitchableApplicationContextFactory extends SwitchableApplicationContextFactory
{
    private static final String SOURCE_BEAN_PROPERTY = "sourceBeanName";
    private String unencryptedContentStoreBeanName;
    
    @Override
    public boolean isUpdateable(String name)
    {
        if (name == null)
        {
            throw new IllegalStateException("Property name cannot be null");
        }
        
        boolean updateable = true;
        if (name.equals(SOURCE_BEAN_PROPERTY) && !getCurrentSourceBeanName().equals(unencryptedContentStoreBeanName))
        {
            updateable = false;
        }
        return updateable;
    }

    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new CryptoSwitchableState(sourceBeanName);
    }


    /**
     * The bean name of the unencrypted ContentStore subsystem.
     * 
     * @param unencryptedContentStoreBeanName
     */
    public void setUnencryptedContentStoreBeanName(String unencryptedContentStoreBeanName)
    {
        this.unencryptedContentStoreBeanName = unencryptedContentStoreBeanName;
    }
    
    
    
    protected class CryptoSwitchableState extends SwitchableState
    {
        protected CryptoSwitchableState(String sourceBeanName)
        {
            super(sourceBeanName);
        }

        @Override
        public void setProperty(String name, String value)
        {
            if (!isUpdateable(name))
            {
                throw new IllegalStateException("Switching to an unencrypted content store is not possible.");
            }
            super.setProperty(name, value);
        }
    }
}
