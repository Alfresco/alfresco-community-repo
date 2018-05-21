package org.alfresco.rest.core.v0;

public enum RMEvents
{
    ABOLISHED("abolished"),
    ALL_ALLOWANCES_GRANTED_ARE_TERMINATED("all_allowances_granted_are_terminated"),
    CASE_CLOSED("case_closed"),
    DECLASSIFICATION_REVIEW("declassification_review"),
    OBSOLETE("obsolete"),
    NO_LONGER_NEEDED("no_longer_needed"),
    STUDY_COMPLETE("study_complete");
    private String eventName;

    RMEvents(String eventName)
    {
        this.eventName = eventName;
    }

    public String getEventName()
    {
        return eventName;
    }
}
