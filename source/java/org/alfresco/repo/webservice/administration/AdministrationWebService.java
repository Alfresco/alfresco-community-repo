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
package org.alfresco.repo.webservice.administration;

import java.rmi.RemoteException;

import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.webservice.AbstractWebService;

/**
 * @author Roy Wetherall
 */
public class AdministrationWebService extends AbstractWebService implements
        AdministrationServiceSoapPort
{
    private TransactionComponent transactionService = null;
    
    public void setTransactionService(TransactionComponent transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#queryUsers(org.alfresco.repo.webservice.administration.UserFilter)
     */
    public UserQueryResults queryUsers(UserFilter filter)
            throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#fetchMoreUsers(java.lang.String)
     */
    public UserQueryResults fetchMoreUsers(String querySession)
            throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#getUser(java.lang.String)
     */
    public UserDetails getUser(String userName) throws RemoteException,
            AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#createUsers(org.alfresco.repo.webservice.administration.NewUserDetails[])
     */
    public UserDetails[] createUsers(NewUserDetails[] newUsers)
            throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#updateUsers(org.alfresco.repo.webservice.administration.UserDetails[])
     */
    public UserDetails[] updateUsers(UserDetails[] users)
            throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public void changePassword(String userName, String oldPassword,
            String newPassword) throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#deleteUsers(java.lang.String[])
     */
    public void deleteUsers(String[] userNames) throws RemoteException,
            AdministrationFault
    {
        // TODO Auto-generated method stub

    }

}
