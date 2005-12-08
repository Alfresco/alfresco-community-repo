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
 * Listens for notifications w.r.t. content.  This includes receiving notifications
 * of the opening and closing of the content streams.
 * 
 * @author Derek Hulley
 */
public interface ContentStreamListener
{
    /**
     * Called when the stream associated with a reader or writer is closed
     * 
     * @throws ContentIOException
     */
    public void contentStreamClosed() throws ContentIOException;
}
