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
package org.alfresco.service.cmr.repository;

/**
 * Interface contract for the conversion of file name to a fully qualified icon image path for use by
 * templating and scripting engines executing within the repository context.
 * <p>
 * Generally this contract will be implemented by classes that have access to say the webserver
 * context which can be used to generate an icon image for a specific filename.
 * 
 * @author Kevin Roast
 */
public interface TemplateImageResolver
{
    /**
     * Resolve the qualified icon image path for the specified filename 
     * 
     * @param filename      The file name to resolve image path for
     * @param small         True to resolve to the small 16x16 image, else large 32x32 image
     */
    public String resolveImagePathForName(String filename, boolean small);
}
