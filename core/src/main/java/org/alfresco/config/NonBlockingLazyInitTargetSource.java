/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.config;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.beans.BeansException;

/**
 * A non-blocking version of LazyInitTargetSource.
 * 
 * @author dward
 */
public class NonBlockingLazyInitTargetSource extends AbstractBeanFactoryBasedTargetSource
{

    private static final long serialVersionUID = 4509578245779492037L;
    private Object target;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public Object getTarget() throws BeansException
    {
        this.lock.readLock().lock();
        try
        {
            if (this.target != null)
            {
                return this.target;
            }
        }
        finally
        {
            this.lock.readLock().unlock();
        }
        this.lock.writeLock().lock();
        try
        {
            if (this.target == null)
            {
                this.target = getBeanFactory().getBean(getTargetBeanName());
            }
            return this.target;
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }
}
