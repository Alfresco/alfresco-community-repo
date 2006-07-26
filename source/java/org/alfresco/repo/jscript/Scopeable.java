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
package org.alfresco.repo.jscript;

import org.mozilla.javascript.Scriptable;

/**
 * Interface contract for objects that supporting setting of the global scripting scope.
 * This is used to mark objects that are not themselves natively scriptable (i.e. they are
 * wrapped Java objects) but need to access the global scope for the purposes of JavaScript
 * object creation etc.
 * 
 * @author Kevin Roast
 */
public interface Scopeable
{
    /**
     * Set the Scriptable global scope
     * 
     * @param scope global scope
     */
    void setScope(Scriptable scope);
}
