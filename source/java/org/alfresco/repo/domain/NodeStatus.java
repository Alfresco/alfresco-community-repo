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
package org.alfresco.repo.domain;

/**
 * Interface for persistent <b>node status</b> objects.
 * <p>
 * The node status records the liveness and change times of a node.  It follows
 * that a <b>node</b> might not exist (have been deleted) when the 
 * <b>node status</b> still exists.
 * 
 * @author Derek Hulley
 */
public interface NodeStatus
{
    /**
     * @return Returns the unique key for this node status
     */
    public NodeKey getKey();

    /**
     * @param key the unique key
     */
    public void setKey(NodeKey key);
    
    public String getChangeTxnId();
    
    public void setChangeTxnId(String txnId);
    
    public void setDeleted(boolean deleted);
    
    public boolean isDeleted();
}
