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

import java.io.Serializable;

/**
 * A record of a node that is new and in which repository it resides.
 * @author britt
 */
public class NewInRepositoryImpl implements NewInRepository, Serializable
{
    private static final long serialVersionUID = 1905996612150732182L;

    /**
     * The Repository.
     */
    private Repository fRepository;
    
    /**
     * The Node.
     */
    private AVMNode fNode;
    
    /**
     * Default constructor.
     */
    public NewInRepositoryImpl()
    {
    }

    /**
     * Make a new one.
     * @param repository The repository.
     * @param node The AVMNode that is new.
     */
    public NewInRepositoryImpl(Repository repository, AVMNode node)
    {
        fRepository = repository;
        fNode = node;
    }

    /**
     * @return the fNode
     */
    public AVMNode getNode()
    {
        return fNode;
    }

    /**
     * @param node the fNode to set
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }

    /**
     * @return the fRepository
     */
    public Repository getRepository()
    {
        return fRepository;
    }

    /**
     * @param repository the fRepository to set
     */
    public void setRepository(Repository repository)
    {
        fRepository = repository;
    }
}

