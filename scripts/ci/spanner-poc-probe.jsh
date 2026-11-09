import java.sql.*;

int exitCode = 1;
String jdbcUrl = System.getenv("POC_JDBC_URL");
String username = System.getenv("POC_DB_USERNAME");
String password = System.getenv("POC_DB_PASSWORD");
String sqlProbe = System.getenv("POC_SQL_PROBE");
DriverManager.setLoginTimeout(15);

try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
    DatabaseMetaData metadata = connection.getMetaData();
    System.out.println("JDBC_DRIVER=" + metadata.getDriverVersion());
    System.out.println("DATABASE_PRODUCT=" + metadata.getDatabaseProductName());
    System.out.println("DATABASE_VERSION=" + metadata.getDatabaseProductVersion());
    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT 1")) {
        if (!result.next() || result.getInt(1) != 1) {
            throw new AssertionError("SELECT 1 failed");
        }
        System.out.println("CONNECTIVITY=PASS");
    }
    if (sqlProbe != null && !sqlProbe.isBlank()) {
        System.out.println("SQL_PROBE=" + sqlProbe);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sqlProbe);
        }
        System.out.println("SQL_PROBE_RESULT=PASS");
    }
    exitCode = 0;
} catch (SQLException failure) {
    System.out.println("SQLSTATE=" + failure.getSQLState());
    System.out.println("VENDOR_CODE=" + failure.getErrorCode());
    failure.printStackTrace();
} catch (Throwable failure) {
    failure.printStackTrace();
}

/exit exitCode