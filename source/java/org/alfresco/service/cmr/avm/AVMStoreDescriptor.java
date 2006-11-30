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

package org.alfresco.service.cmr.avm;

import java.io.Serializable;
import java.util.Date;

/**
 * A value class for Data about an AVMStore.
 * @author britt
 */
public class AVMStoreDescriptor implements Serializable
{
    private static final long serialVersionUID = -4401863082685362175L;

    /**
     * The name.
     */
    private String fName;
    
    /**
     * The creator.
     */
    private String fCreator;
    
    /**
     * The create date.
     */
    private long fCreateDate;
    
    public AVMStoreDescriptor(String name,
                                String creator,
                                long createDate)
    {
        fName = name;
        fCreator = creator;
        fCreateDate = createDate;
    }

    /**
     * @return the fCreateDate
     */
    public long getCreateDate()
    {
        return fCreateDate;
    }

    /**
     * @return the fCreator
     */
    public String getCreator()
    {
        return fCreator;
    }

    /**
     * @return the fName
     */
    public String getName()
    {
        return fName;
    }
    
    public String toString()
    {
        return "[" + fName + ":" + fCreator + ":" + new Date(fCreateDate) + "]";
    }
}
