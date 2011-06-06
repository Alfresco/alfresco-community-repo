script:
{
    model.conn = cmis.getConnection(url.templateArgs["conn"]);
    model.conn.close();
}
