/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
 */
package org.alfresco.license;

import java.security.Principal;
import java.util.Date;

import org.alfresco.service.license.LicenseDescriptor;
import org.joda.time.DateMidnight;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import de.schlichtherle.license.LicenseContent;


/**
 * License Descriptor
 * 
 * @author davidc
 */
public class LicenseContentDescriptor implements LicenseDescriptor
{
    private LicenseContent licenseContent = null;

    /**
     * Construct
     * 
     * @param licenseContent
     */
    public LicenseContentDescriptor(LicenseContent licenseContent)
    {
        this.licenseContent = licenseContent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getIssued()
     */
    public Date getIssued()
    {
        return licenseContent.getIssued();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getValidUntil()
     */
    public Date getValidUntil()
    {
        return licenseContent.getNotAfter();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getSubject()
     */
    public String getSubject()
    {
        return licenseContent.getSubject();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getHolder()
     */
    public Principal getHolder()
    {
        return licenseContent.getHolder();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getIssuer()
     */
    public Principal getIssuer()
    {
        return licenseContent.getIssuer();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getDays()
     */
    public Integer getDays()
    {
        Integer days = null;
        Date validUntil = getValidUntil();
        if (validUntil != null)
        {
            Date issued = getIssued();
            days = new Integer(calcDays(issued, validUntil));
        }
        return days;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseDescriptor#getRemainingDays()
     */
    public Integer getRemainingDays()
    {
        Integer days = null;
        Date validUntil = getValidUntil();
        if (validUntil != null)
        {
            Date now = new Date();
            days = new Integer(calcDays(now, validUntil));
        }
        return days;
    }

    /**
     * Calculate number of days between start and end date
     * 
     * @param start  start date
     * @param end  end date
     * @return  number days between
     */
    private int calcDays(Date start, Date end)
    {
        DateMidnight startMidnight = new DateMidnight(start);
        DateMidnight endMidnight = new DateMidnight(end);
        
        int days;
        if (endMidnight.isBefore(startMidnight))
        {
            Interval interval = new Interval(endMidnight, startMidnight);
            Period period = interval.toPeriod(PeriodType.days());
            days = 0 - period.getDays();
        }
        else
        {
            Interval interval = new Interval(startMidnight, endMidnight);
            Period period = interval.toPeriod(PeriodType.days());
            days = period.getDays();
        }
        return days;
    }

}
