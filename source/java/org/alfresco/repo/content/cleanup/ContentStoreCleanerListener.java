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
package org.alfresco.repo.content.cleanup;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;

/**
 * A listener that can be plugged into a
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner cleaner} to
 * move soon-to-be-deleted content to a new location.
 * 
 * @author Derek Hulley
 */
public interface ContentStoreCleanerListener
{
    public void beforeDelete(ContentReader reader) throws ContentIOException;
}
