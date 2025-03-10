<#--
 #%L
 Alfresco Records Management Module
 %%
 Copyright (C) 2005 - 2025 Alfresco Software Limited
 %%
 This file is part of the Alfresco software.
 -
 If the software was purchased under a paid Alfresco license, the terms of
 the paid license agreement will prevail.  Otherwise, the software is
 provided under the following open source license terms:
 -
 Alfresco is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 -
 Alfresco is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 -
 You should have received a copy of the GNU Lesser General Public License
 along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 #L%
-->
<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>

   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/images/task-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      Record has been rejected
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${args.rejectDate?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hello <b><i>${args.recordCreator}</i></b>,</p>

                                             <p><b><i>${args.rejectedPerson}</i></b> has rejected the following record with this reason:</p>

                                             <p>${args.rejectReason}</p>

                                             <table cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
                                                <tr>
                                                   <td>
                                                      <table cellpadding="0" cellspacing="0" border="0">
                                                         <tr>
                                                            <td valign="top">
                                                               <img src="${shareUrl}/rm/components/documentlibrary/images/record-64.png" alt="" width="64" height="64" border="0" style="padding-right: 10px;" />
                                                            </td>
                                                            <td>
                                                               <table cellpadding="2" cellspacing="0" border="0">
                                                                  <tr>
                                                                     <td><b>${args.recordId} ${args.recordName}</b></td>
                                                                  </tr>
                                                                  <tr>
                                                                     <td>Click on this link to view the record:</td>
                                                                  </tr>
                                                                  <tr>
                                                                     <td>
                                                                        <a href="${shareUrl}/page/site/${args.site}/document-details?nodeRef=${args.record.storeType}://${args.record.storeId}/${args.record.id}">
                                                                        ${shareUrl}/page/site/${args.site}/document-details?nodeRef=${args.record.storeType}://${args.record.storeId}/${args.record.id}</a>
                                                                     </td>
                                                                  </tr>
                                                               </table>
                                                            </td>
                                                         </tr>
                                                      </table>
                                                    </td>
                                                </tr>
                                                <tr><td><div style="border-top: 1px solid #aaaaaa; margin:12px;"></div></td></tr>
                                             </table>

                                             <p>Sincerely,<br />
                                             Alfresco ${productName!""}</p>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 To find out more about Alfresco ${productName!""} visit <a href="http://www.alfresco.com">http://www.alfresco.com</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>