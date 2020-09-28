/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.dictionary;

import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;

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
@AlfrescoPublicApi
public interface Constraint
{
    /**
     * Returns the 'type' of the constraint, this is the identifier given to 
     * constraint in the configuration.
     *  
     * @return The type
     */
    public String getType();
    
    /**
     * @return the human-readable constraint title (optional)
     */
    public String getTitle();
    
    /**
     * Gets the constraint name.
     * 
     * @return the constraint name.
     */
    public String getShortName();

    /**
     * Returns the parameters passed to the instance of the constraint.
     * 
     * @return Map of parameters or an empty <tt>Map</tt> if none exist
     */
    public Map<String, Object> getParameters();
    
    /**
     * Initializes the constraint with appropriate values, which will depend
     * on the implementation itself.  This method can be implemented as a
     * once-off, i.e. reinitialization does not have to be supported.
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
