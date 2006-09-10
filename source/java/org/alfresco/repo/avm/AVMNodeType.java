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

package org.alfresco.repo.avm;

/**
 * This defines the different types of FolderEntrys.
 * @author britt
 */
public interface AVMNodeType
{
    public static final int PLAIN_FILE = 0;
    public static final int LAYERED_FILE = 1;
    public static final int PLAIN_DIRECTORY = 2;
    public static final int LAYERED_DIRECTORY = 3;
    public static final int DELETED_NODE = 4;
}
