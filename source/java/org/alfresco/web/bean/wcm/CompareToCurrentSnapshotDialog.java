/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.wcm;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for compareToCurrentSnapshot dialog
 *
 * @author ValerySh
 */
public class CompareToCurrentSnapshotDialog extends CompareSnapshotDialog
{

    private static final long serialVersionUID = 5483551383286687197L;
    /** description for dialog */
    private static final String MSG_COMPARE_TO_CURRENT_DESCRIPTION = "snapshot_compare_to_current_description";

    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(CompareToPreviousSnapshotDialog.class);

    /*
     * (non-Javadoc)
     *
     * @see org.alfresco.web.bean.wcm.CompareSnapshot#getComparedNodes()
     */
    public List<Map<String, String>> getComparedNodes()
    {
        if (finished)
        {
            finished = false;
            return null;
        }
        finished = true;
        return WCMCompareUtils.getComparedNodes(getAvmSyncService(), version, storeRoot, -1, storeRoot, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.alfresco.web.bean.wcm.CompareSnapshot#getDescription()
     */
    protected String getDescription()
    {
        return MSG_COMPARE_TO_CURRENT_DESCRIPTION;
    }

}
