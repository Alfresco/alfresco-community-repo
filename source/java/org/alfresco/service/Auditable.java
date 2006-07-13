/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service;

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
