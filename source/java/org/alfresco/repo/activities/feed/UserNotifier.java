package org.alfresco.repo.activities.feed;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Notifies the given user by sending their activity feed information to their email address (or potentially some other destination)
 *
 * @since 4.0
 */
public interface UserNotifier
{
	public Pair<Integer, Long> notifyUser(final NodeRef personNodeRef, String subjectText, Object[] subjectParams, Map<String, String> siteNames,
			String shareUrl, int repeatIntervalMins, String templateNodeRef);
}
