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

import org.alfresco.repo.avm.hibernate.RepositoryBean;
import org.alfresco.repo.avm.impl.RepositoryImpl;

/**
 * @author britt
 *
 */
public class RepositoryFactory
{
    /**
     * The single instance of this. So far.
     */
    private static RepositoryFactory fgFactory;
    
    /**
     * The super repository.
     */
    private SuperRepository fSuper;
    
    public RepositoryFactory()
    {
        fSuper = null;
        fgFactory = this;
    }
    
    /**
     * Create a Repository instance from a bean.
     * @param bean The RepositoryBean.
     * @return A Repository instance.
     */
    public Repository createFromBean(RepositoryBean bean)
    {
        return new RepositoryImpl(fSuper, bean);
    }
    
    /**
     * Set the super repository.
     * @param superRepo
     */
    public void setSuperRepository(SuperRepository superRepo)
    {
        fSuper = superRepo;
    }
    
    /**
     * Get the single instance.
     * @return The instance.
     */
    public static RepositoryFactory GetInstance()
    {
        return fgFactory;
    }
}
