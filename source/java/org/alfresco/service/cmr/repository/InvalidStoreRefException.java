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
 * Thrown when an operation cannot be performed because the <b>store</b> reference
 * no longer exists.
 * 
 * @author Derek Hulley
 */
public class InvalidStoreRefException extends AbstractStoreException
{
    private static final long serialVersionUID = 3258126938479409463L;

    public InvalidStoreRefException(StoreRef storeRef)
    {
        super(storeRef);
    }

    public InvalidStoreRefException(String msg, StoreRef storeRef)
    {
        super(msg, storeRef);
    }
}
