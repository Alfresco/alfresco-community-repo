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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.security.permissions.impl.hibernate.PermissionReference;
import org.alfresco.repo.security.permissions.impl.hibernate.PermissionReferenceImpl;
import org.alfresco.service.namespace.NamespaceService;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * The roles defined in permissionsDefinition.xml moved from <b>cm:folder</b> to <b>cm:cmobject</b>.
 * This effects the data stored in the <b>node_perm_entry</b> table.
 * <p>
 * JIRA: {@link http://www.alfresco.org/jira/browse/AR-344 AR-344}
 * 
 * @author Derek Hulley
 */
public class PermissionDataPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.updatePermissionData.result";
    
    private HibernateHelper helper;
    
    public PermissionDataPatch()
    {
        helper = new HibernateHelper();
    }
    
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.helper.setSessionFactory(sessionFactory);
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        List<String> createdNames = helper.createPermissionReferences();
        int updatedEntries = helper.updatePermissionEntries();
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, createdNames, updatedEntries);
        // done
        return msg;
    }

    private static class HibernateHelper extends HibernateDaoSupport
    {
        private static final String TYPE_NAME_OLD = "folder";
        private static final String TYPE_NAME_NEW = "cmobject";
        private static final String[] NAMES = new String[] {"Coordinator", "Contributor", "Editor", "Guest"};
        private static final String QUERY_UPDATE_PERM_ENTRY_TYPENAME = "permission.patch.UpdatePermissionEntryTypeName";
        
        public List<String> createPermissionReferences()
        {
            List<String> createdNames = new ArrayList<String>(4);
            for (String name : NAMES)
            {
                // create permission references as required, double checking for their existence first
                PermissionReference ref = new PermissionReferenceImpl();
                ref.setTypeUri(NamespaceService.CONTENT_MODEL_1_0_URI);
                ref.setTypeName(TYPE_NAME_NEW);
                ref.setName(name);

                // it acts as its own key
                PermissionReference found = (PermissionReference) getHibernateTemplate().get(
                        PermissionReferenceImpl.class,
                        ref);

                if (found == null)
                {
                    // it was not found, so create it
                    getHibernateTemplate().save(ref);
                    createdNames.add(name);
                }
            }            
            return createdNames;
        }
        
        public int updatePermissionEntries()
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    // flush any outstanding entities
                    session.flush();
                    
                    Query query = session.getNamedQuery(HibernateHelper.QUERY_UPDATE_PERM_ENTRY_TYPENAME);
                    query.setString("typeNameNew", TYPE_NAME_NEW)
                         .setString("typeNameOld", TYPE_NAME_OLD);
                    int updateCount = query.executeUpdate();
                    return new Integer(updateCount);
                }
            };
            Integer updateCount = (Integer) getHibernateTemplate().execute(callback);
            // done
            return updateCount.intValue();
        }
    }
}
