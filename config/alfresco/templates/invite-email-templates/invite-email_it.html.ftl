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
                                                      È stato ricevuto un invito a partecipare al '${args["siteName"]}' Sito
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Caro ${inviteePerson.properties["cm:firstName"]!""},</p>
                                             
                                             <p>${inviterPerson.properties["cm:firstName"]!""} ${inviterPerson.properties["cm:lastName"]!""} 
                                             ti ha  inviatato a partecipare al sito ${args["siteName"]} con il ruolo di ${args["inviteeSiteRole"]}.</p>
                                             
                                             <p>Fare clic sul collegamento per accettare l’invito dell'${inviterPerson.properties["cm:firstName"]!""}':<br />
                                             <br /><a href="${args["acceptLink"]}">${args["acceptLink"]}</a></p>
                                             
                                             <#if args["inviteeGenPassword"]?exists>
                                             <p>Account creato. I dettagli di accesso sono:<br />
                                             <br />Nome utente: ${args["inviteeUserName"]}
                                             <br />Password: ${args["inviteeGenPassword"]}
                                             </p>
                                             
                                             <p>Si consiglia di cambiare la password quando si effettua l'eccesso per la prima volta.
                                             È possibile effettuare questa operazione andando a <b>Il mio profilo</b> e selezionando <b>Cambia password</b>.</p>
                                             </#if>
                                             
                                             <p>Per rifiutare l’invito dell’${inviterPerson.properties["cm:firstName"]!""}, fare clic su questo collegamento:<br />
                                             <br /><a href="${args["rejectLink"]}">${args["rejectLink"]}</a></p>
                                             
                                             <p>Cordiali saluti,<br />
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
                                 Per ulteriori informazioni su Alfresco ${productName!""} visitare il sito Web <a href="http://www.alfresco.com">http://www.alfresco.com</a>
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