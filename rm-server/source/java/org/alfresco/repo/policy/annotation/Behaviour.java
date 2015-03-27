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
package org.alfresco.repo.policy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;

/**
 * Behaviour method annotation.
 * 
 * @author Roy Wetherall
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Behaviour
{
    /** lookup name of the behaviour, if none specified behaviour is not recorded in the registry */
    String name() default "";
    
    /** kind of behaviour */
    BehaviourKind kind();
    
    /** qualified name of policy */
    String policy() default "";
    
    /** indicates whether this is a service behaviour or not */
    boolean isService() default false;
    
    /** qualified name of type/aspect */
    String type() default "";
    
    /** qualified name of association */
    String assocType() default "cm:contains";
    
    /** notification frequency */
    NotificationFrequency notificationFrequency() default NotificationFrequency.EVERY_EVENT;
}
