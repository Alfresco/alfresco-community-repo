/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class offers the default implementation of a strategy for selection of
 * disposition schedule for a record when there is more than one which is applicable.
 * An example of where this strategy might be used would be in the case of a record
 * which was multiply filed.
 *
 * @author neilm
 */
public class DispositionSelectionStrategy implements RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(DispositionSelectionStrategy.class);

    /** Disposition service */
    private DispositionService dispositionService;

    /**
     * Set the disposition service
     *
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Select the disposition schedule to use given there is more than one
     *
     * @param recordFolders
     * @return
     */
    public NodeRef selectDispositionScheduleFrom(List<NodeRef> recordFolders)
    {
        if (recordFolders == null || recordFolders.isEmpty())
        {
            return null;
        }
        else
        {
            //      46 CHAPTER 2
            //      Records assigned more than 1 disposition must be retained and linked to the record folder (category) with the longest
            //      retention period.

            // Assumption: an event-based disposition action has a longer retention
            // period than a time-based one - as we cannot know when an event will occur
            // TODO Automatic events?

        	NodeRef recordFolder = null;
        	if (recordFolders.size() == 1)
        	{
        		recordFolder = recordFolders.get(0);
        	}
        	else
        	{
	            SortedSet<NodeRef> sortedFolders = new TreeSet<>(new DispositionableNodeRefComparator());
	            sortedFolders.addAll(recordFolders);
	            recordFolder = sortedFolders.first();
        	}

            DispositionSchedule dispSchedule = dispositionService.getDispositionSchedule(recordFolder);

            if (logger.isDebugEnabled())
            {
                logger.debug("Selected retention schedule: " + dispSchedule);
            }

            NodeRef result = null;
            if (dispSchedule != null)
            {
                result = dispSchedule.getNodeRef();
            }
            return result;
        }
    }

    /**
     * This class defines a natural comparison order between NodeRefs that have
     * the dispositionLifecycle aspect applied.
     * This order has the following meaning: NodeRefs with a 'lesser' value are considered
     * to have a shorter retention period, although the actual retention period may
     * not be straightforwardly determined in all cases.
     */
    class DispositionableNodeRefComparator implements Comparator<NodeRef>
    {
        public int compare(final NodeRef f1, final NodeRef f2)
        {
            // Run as admin user
            return AuthenticationUtil.runAs(new RunAsWork<Integer>()
            {
                public Integer doWork()
                {
                    return compareImpl(f1, f2);
                }
            }, AuthenticationUtil.getAdminUserName());
        }

        private int compareImpl(NodeRef f1, NodeRef f2)
        {
            // quick check to see if the node references are the same
            if (f1.equals(f2))
            {
                return 0;
            }

            // get the disposition schedules for the folders
            DispositionSchedule ds1 = dispositionService.getDispositionSchedule(f1);
            DispositionSchedule ds2 = dispositionService.getDispositionSchedule(f2);

            // make sure each folder has a disposition schedule
            if (ds1 == null && ds2 != null)
            {
                return 1;
            }
            else if (ds1 != null && ds2 == null)
            {
                return -1;
            }
            else if (ds1 == null && ds2 == null)
            {
                return 0;
            }

            // TODO this won't work correctly if we are trying to compare schedules that are record based!!
            DispositionAction da1 = dispositionService.getNextDispositionAction(f1);
            DispositionAction da2 = dispositionService.getNextDispositionAction(f2);

            if (da1 != null && da2 != null)
            {
                Date asOfDate1 = da1.getAsOfDate();
                Date asOfDate2 = da2.getAsOfDate();
                // If both record(Folder)s have asOfDates, then use these to compare
                if (asOfDate1 != null && asOfDate2 != null)
                {
                    return asOfDate1.compareTo(asOfDate2);
                }
                // If one has a date and the other doesn't, the one with the date is "less".
                // (Defined date is 'shorter' than undefined date as an undefined date means it may be retained forever - theoretically)
                else if (asOfDate1 != null || asOfDate2 != null)
                {
                    return asOfDate1 == null ? +1 : -1;
                }
                else
                {
                    // Neither has an asOfDate. (Somewhat arbitrarily) we'll use the number of events to compare now.
                    DispositionActionDefinition dad1 = da1.getDispositionActionDefinition();
                    DispositionActionDefinition dad2 = da2.getDispositionActionDefinition();
                    int eventsCount1 = 0;
                    int eventsCount2 = 0;

                    if (dad1 != null)
                    {
                        eventsCount1 = dad1.getEvents().size();
                    }
                    if (dad2 != null)
                    {
                        eventsCount2 = dad2.getEvents().size();
                    }
                    return Integer.valueOf(eventsCount1).compareTo(eventsCount2);
                }
            }

            return 0;
        }
    }
}
