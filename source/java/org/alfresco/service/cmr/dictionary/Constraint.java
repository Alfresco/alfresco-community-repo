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
