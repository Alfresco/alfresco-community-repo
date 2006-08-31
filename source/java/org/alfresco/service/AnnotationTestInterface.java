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

/**
 * An interface to test the use of the auditable annotation.
 * 
 * @author Andy Hind
 */
@PublicService
public interface AnnotationTestInterface
{
    @Auditable()
    public void noArgs();
    
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"one", "two"})
    public String getString(String one, String two); 
    
    @Auditable(key =  Auditable.Key.ARG_0, parameters = {"one"})
    public String getAnotherString(String one); 
}
