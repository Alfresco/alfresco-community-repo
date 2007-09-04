/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to defined key and parameter names for the auditing API.
 * 
 * If this annotation is present on a public service interface it will be considered for auditing. If it is not present the method will never be audited.
 * 
 * Note that the service name and method name can be found from the bean definition and the method invocation.
 * 
 * @author Andy Hind
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable
{
    enum Key 
    {
        NO_KEY, RETURN, ARG_0, ARG_1, ARG_2, ARG_3, ARG_4, ARG_5, ARG_6, ARG_7, ARG_8, ARG_9 
    }
    
    /**
     * The position of the key argument in the method list.
     * 
     * @return -1 indicates there is no key
     */
    Auditable.Key key() default Key.NO_KEY;

    /**
     * The names of the parameters
     * 
     * @return a String[] of parameter names, the default is an empty array.
     */
    String[] parameters() default {};
    
    /**
     * If a method as marked as warn, it is potentially an audit hole.
     * Typically a method returns an object which allows unaudited access.
     * 
     * This is intended to mark things that appear to expose unsafe API calls. 
     * 
     * @return
     */
    boolean warn() default false;
    
    /**
     * All method parameters are recorded by default.
     * This can be used to stop a parameter being written to the audit log.
     * It will be entered as "******".
     * 
     * @return
     */
    boolean[] recordable() default {};
    
    /**
     * Return object are recorded by default.
     * Setting this means they can never be recorded in the audit.
     * 
     * @return
     */
    boolean recordReturnedObject() default true;
}
