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
package org.alfresco.service.cmr.dictionary;


/**
 * The interface for classes that implement constraints on property values.
 * <p>
 * Implementations of the actual constraint code should must not synchronize
 * or in any other way block threads.  Concurrent access of the evaluation
 * method is expected, but will always occur after initialization has completed.
 * <p>
 * Attention to performance is <u>crucial</u> for all implementations as
 * instances of this class are heavily used.
 * <p>
 * The constraint implementations can provide standard setter methods that will
 * be populated by bean setter injection.  Once all the available properties have
 * been set, the contraint will be initialized.
 * 
 * @author Derek Hulley
 */
public interface Constraint
{
    /**
     * Initializes the constraint with appropriate values, which will depend
     * on the implementation itself.  This method can be implemented as a
     * once-off, i.e. reinitialization does not have to be supported.
     * 
     * @param parameters constraint parameters
     */
    public void initialize();
    
    /**
     * Evaluates a property value according to the implementation and initialization
     * parameters provided.
     * 
     * @param value the property value to check
     * 
     * @throws ConstraintException if the value doesn't pass all constraints
     */
    public void evaluate(Object value);
}
