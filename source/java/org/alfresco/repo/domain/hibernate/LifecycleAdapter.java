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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

/**
 * Helper base class providing lifecycle and other support
 * 
 * @author Derek Hulley
 */
public abstract class LifecycleAdapter implements Lifecycle
{
    /** Helper */
    private Session session;
    
    /**
     * @return Returns the session that this object was used in
     */
    protected Session getSession()
    {
        return session;
    }

    /**
     * @return Returns NO_VETO always
     */
    public boolean onDelete(Session session) throws CallbackException
    {
        return NO_VETO;
    }
    
    /** NO OP */
    public void onLoad(Session session, Serializable id)
    {
        this.session = session;
    }

    /** @return Returns NO_VETO always */
    public boolean onSave(Session session) throws CallbackException
    {
        this.session = session;
        return NO_VETO;
    }

    /** @return Returns NO_VETO always */
    public boolean onUpdate(Session session) throws CallbackException
    {
        this.session = session;
        return NO_VETO;
    }
}
