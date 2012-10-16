------------------------------------------------------------------------------
ドキュメント名:   ${document.name}
------------------------------------------------------------------------------

   <#if document.properties.title?exists>
タイトル:   ${document.properties.title}
   <#else>
タイトル:   なし
   </#if>
   <#if document.properties.description?exists>
説明:     ${document.properties.description}
   <#else>
説明:   なし
   </#if>
作成者:   ${document.properties.creator}
作成日時: ${document.properties.created?datetime}
修正者:   ${document.properties.modifier}
修正日時: ${document.properties.modified?datetime}
サイズ:    ${document.size / 1024} KB


コンテンツリンク

コンテンツ フォルダー:   ${contextUrl}/navigate/browse${document.parent.webdavUrl}
コンテンツ URL:      ${contextUrl}${document.url}
ダウンロード URL:     ${contextUrl}${document.downloadUrl}
WebDAV URL:       ${contextUrl}${document.webdavUrl}


