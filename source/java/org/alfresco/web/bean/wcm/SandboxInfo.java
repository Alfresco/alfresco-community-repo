/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  Licensed under the Mozilla Public License version 1.1
*  with a permitted attribution clause. You may obtain a
*  copy of the License at:
*  
*      http://www.alfresco.org/legal/license.txt
*  
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
*  either express or implied. See the License for the specific
*  language governing permissions and limitations under the
*  License.
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    SandboxInfo.java
*----------------------------------------------------------------------------*/

package org.alfresco.web.bean.wcm;

/**
*  Provides information about a sandbox created by SandboxFactory.
*/
public final class SandboxInfo
{
    String [] store_names_;
    public SandboxInfo(String [] store_names)
    {
        store_names_ = store_names;
    }

    /**
    *  A list of names of the stores within this sandbox.
    *  The "main" store should come first in this list;
    *  any other stores should appear in the order that 
    *  they are overlaid on "main" (e.g.: any "preview" 
    *  layers should come afterward, in "lowest first" order).
    *  <p>
    *  Note: all sandboxes must have a "main" layer.
    */
    public String [] getStoreNames()    { return store_names_; }

    /**
    *  The name of the "main" store within this sandbox.
    */
    public String    getMainStoreName() { return store_names_[0]; }
}
