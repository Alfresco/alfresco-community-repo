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
package org.alfresco.repo.audit;

import java.io.InputStream;

/**
 * An audit configuration is xml content from an input stream.
 *  
 * @author Andy Hind
 */
public interface AuditConfiguration
{

   /**
    * Get the XML content for the configuration as a stream.
    * 
    * @return
    */ 
    public abstract InputStream getInputStream();

}