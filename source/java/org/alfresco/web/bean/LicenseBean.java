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
package org.alfresco.web.bean;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Date;

import javax.faces.context.FacesContext;

import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.web.app.Application;

/**
 * Backing Bean for the License Management pages.
 * 
 * @author David Caruana
 */
public class LicenseBean
{
   
   /** The DescriptorService to be used by the bean */
   private DescriptorService descriptorService;
   
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param descriptorService      The DescriptorService to set.
    */
   public void setDescriptorService(DescriptorService descriptorService)
   {
      this.descriptorService = descriptorService;
   }


   /**
    * Gets the License Description
    * 
    * @return  license description
    */
   public String getLicenseDescription()
   {
       String description = "";
       
       LicenseDescriptor descriptor = descriptorService.getLicenseDescriptor();
       if (descriptor != null)
       {
           String subject = descriptor.getSubject();
           String holder = getHolderOrganisation(descriptor.getHolder());
           Date issued = descriptor.getIssued();
           Date validUntil = descriptor.getValidUntil();
           
           if (validUntil == null)
           {
               description = Application.getMessage(FacesContext.getCurrentInstance(), "admin_unlimited_license");
               description = MessageFormat.format(description, new Object[] { subject, holder, issued });
           }
           else
           {
               int days = descriptor.getDays();
               int remainingDays = descriptor.getRemainingDays();
               description = Application.getMessage(FacesContext.getCurrentInstance(), "admin_limited_license");
               description = MessageFormat.format(description, new Object[] { subject, holder, issued, days, validUntil, remainingDays });
           }
       }
       else
       {
           description = Application.getMessage(FacesContext.getCurrentInstance(), "admin_invalid_license");
       }
       
       return description;
   }

   /**
    * Get Organisation from Principal
    * 
    * @param holderPrincipal
    * @return  organisation
    */
   private String getHolderOrganisation(Principal holderPrincipal)
   {
       String holder = null;
       if (holderPrincipal != null)
       {
           holder = holderPrincipal.getName();
           if (holder != null)
           {
               String[] properties = holder.split(",");
               for (String property : properties)
               {
                   String[] parts = property.split("=");
                   if (parts[0].equals("O"))
                   {
                       holder = parts[1];
                   }
               }
           }
       }
       
       return holder;
   }
   
}
