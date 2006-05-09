/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm.hibernate;


/**
 * A Plain File Node.  Contains a possibly shared Content object.
 * @author britt
 */
public interface PlainFileNodeBean extends FileNodeBean
{
    /**
     * Set the Content object for this.
     */
    public void setContent(ContentBean content);
    
    /**
     * Get the Content object for this.
     * @return The Content object.
     */
    public ContentBean getContent();
}
