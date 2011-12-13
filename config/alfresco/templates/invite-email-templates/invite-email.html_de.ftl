<html>
   <#assign inviterPersonRef=args["inviterPersonRef"]/>
   <#assign inviterPerson=companyhome.nodeByReference[inviterPersonRef]/>
   <#assign inviteePersonRef=args["inviteePersonRef"]/>
   <#assign inviteePerson=companyhome.nodeByReference[inviteePersonRef]/>

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
                                                   <img src="${shareUrl}/res/components/site-finder/images/site-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      Sie wurden aufgefordert, der '${args["siteName"]}' Site beizutreten
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hallo ${inviteePerson.properties["cm:firstName"]!""},</p>
                                             
                                             <p>${inviterPerson.properties["cm:firstName"]!""} ${inviterPerson.properties["cm:lastName"]!""} 
                                             hat Sie eingeladen, als ${args["inviteeSiteRole"]} an der Site  ${args["siteName"]} teilzunehmen.</p>
                                             
                                             <p>Klicken Sie auf diesen Link, um die Einladung von ${inviterPerson.properties["cm:firstName"]!""}' anzunehmen:<br />
                                             <br /><a href="${args["acceptLink"]}">${args["acceptLink"]}</a></p>
                                             
                                             <#if args["inviteeGenPassword"]?exists>
                                             <p>Es wurde ein Konto für Sie erstellt. Ihre Anmeldedaten lauten:<br />
                                             <br />Benutzername: ${args["inviteeUserName"]}
                                             <br />Passwort: ${args["inviteeGenPassword"]}
                                             </p>
                                             
                                             <p>Wir legen Ihnen nahe, das Passwort bei der ersten Anmeldung zu ändern.
                                             Gehen Sie dazu zu <b>Mein Profil</b>, und wählen Sie <b>Passwort ändern</b> aus.</p>
                                             </#if>
                                             
                                             <p>Klicken Sie auf den folgenden Link, wenn Sie die Einladung von ${inviterPerson.properties["cm:firstName"]!""}’ nicht annehmen möchten:<br />
                                             <br /><a href="${args["rejectLink"]}">${args["rejectLink"]}</a></p>
                                             
                                             <p>Mit freundlichen Grüßen<br />
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
                                 Weitere Informationen zu Alfresco ${productName!""} finden Sie unter <a href="http://www.alfresco.com">http://www.alfresco.com</a>
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