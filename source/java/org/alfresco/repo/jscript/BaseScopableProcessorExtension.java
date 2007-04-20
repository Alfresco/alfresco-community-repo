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

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.mozilla.javascript.Scriptable;

/**
 * Abstract base class for a script implementation that requires a script execution scope.
 * 
 * The scope is local to the currently executing script and therefore a ThreadLocal is required.
 * 
 * @author Kevin Roast
 */
public class BaseScopableProcessorExtension extends BaseProcessorExtension implements Scopeable
{
    private static ThreadLocal<Scriptable> scope = new ThreadLocal<Scriptable>();
    
    /**
     * Set the Scriptable global scope
     * 
     * @param script relative global scope
     */
    public void setScope(Scriptable scope)
    {
        BaseScopableProcessorExtension.scope.set(scope);
    }
    
    /**
     * @return script global scope
     */
    public Scriptable getScope()
    {
        return BaseScopableProcessorExtension.scope.get();
    }
}
