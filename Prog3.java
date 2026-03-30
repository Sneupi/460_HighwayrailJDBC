
import java.io.*;
import java.sql.*;

public class Prog3 {

    // Get number of reports (row count) per year (table)
    public static void executeA(Connection dbconn, String schema) throws SQLException {

        String sql = "SELECT '1980' AS table_year, COUNT(*) AS row_count FROM " + schema + ".\"1980\" "
                + "UNION "
                + "SELECT '1995' AS table_year, COUNT(*) AS row_count FROM " + schema + ".\"1995\" "
                + "UNION "
                + "SELECT '2010' AS table_year, COUNT(*) AS row_count FROM " + schema + ".\"2010\" "
                + "UNION "
                + "SELECT '2025' AS table_year, COUNT(*) AS row_count FROM " + schema + ".\"2025\" ";

        Prog3.executeQuery(sql, dbconn);
    }

    // Display incident count by state (top 10, descending) for given year
    public static void executeB(Connection dbconn, String schema, String year) throws SQLException {

        String sql = "SELECT state_name, COUNT(*) AS incident_count FROM " + schema + ".\"" + year + "\" "
                + "GROUP BY state_name "
                + "ORDER BY incident_count DESC "
                + "LIMIT 10";

        Prog3.executeQuery(sql, dbconn);
    }

    // Display largest % drop in incident count by state (top 5, descending) between given years
    public static void executeC(Connection dbconn, String schema, String year1, String year2) throws SQLException {

        String sql = "SELECT y1.state_name, "
                + "     y1.incident_count AS incidents_" + year1 + ", "
                + "     y2.incident_count AS incidents_" + year2 + ", "
                + "     ROUND(100.0 * ((y1.incident_count - y2.incident_count) / (1.0 * y1.incident_count)), 2) AS percent_drop "
                + "FROM (SELECT state_name, COUNT(*) AS incident_count FROM " + schema + ".\"" + year1
                + "\" GROUP BY state_name) y1 "
                + "JOIN (SELECT state_name, COUNT(*) AS incident_count FROM " + schema + ".\"" + year2
                + "\" GROUP BY state_name) y2 "
                + "ON y1.state_name = y2.state_name "
                + "ORDER BY percent_drop DESC "
                + "LIMIT 5";

        Prog3.executeQuery(sql, dbconn);
    }

    // Display largest % drop in incident count by field (top 10, descending) between given years
    public static void executeD(Connection dbconn, String schema, String year1, String year2, String field) throws SQLException {
        
        String sql = "SELECT y1." + field + ", "
                + "     y1.incident_count AS incidents_" + year1 + ", "
                + "     y2.incident_count AS incidents_" + year2 + ", "
                + "     ROUND(100.0 * ((y1.incident_count - y2.incident_count) / (1.0 * y1.incident_count)), 2) AS percent_drop "
                + "FROM (SELECT " + field + ", COUNT(*) AS incident_count FROM " + schema + ".\"" + year1
                + "\" GROUP BY " + field + ") y1 "
                + "JOIN (SELECT " + field + ", COUNT(*) AS incident_count FROM " + schema + ".\"" + year2
                + "\" GROUP BY " + field + ") y2 "
                + "ON y1." + field + " = y2." + field + " "
                + "ORDER BY percent_drop DESC "
                + "LIMIT 10";

        Prog3.executeQuery(sql, dbconn);
    }

    public static String resultSetToString(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        StringBuilder sb = new StringBuilder();

        // Print column headers
        for (int i = 1; i <= columnCount; i++) {
            sb.append(meta.getColumnLabel(i));
            if (i < columnCount)
                sb.append("\t");
        }
        sb.append("\n\n");

        // Iterate through rows
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                int type = meta.getColumnType(i);

                // Let JDBC figure out type mapping
                Object value = rs.getObject(i);

                // Handle SQL NULL
                if (rs.wasNull()) {
                    sb.append("NULL");
                } else {
                    sb.append(value);
                }

                // Delimit cols
                if (i < columnCount)
                    sb.append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void executeQuery(String query, Connection dbconn) throws SQLException {

        try (Statement stmt = dbconn.createStatement();
                ResultSet answer = stmt.executeQuery(query)) {

            if (answer != null) {
                System.out.println(Prog3.resultSetToString(answer));
            }

        }
    }

    public static void executeUpdate(String sql, Connection dbconn) throws SQLException {
        try (Statement stmt = dbconn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            System.out.println("Rows updated: " + rowsAffected);

        }
    }

    public static void main(String[] args) {

        String dbURL = null, driverClass = null;
        Connection dbconn = null;

        // Check args

        if (args.length == 2) { // get credentials from user args
            driverClass = args[0];
            dbURL = args[1];
        } else {
            System.out.println("\nUsage: java Prog3 <driverClass> <dbURL>\n\n"
                    + "\t<driverClass> : JDBC driver classname\n"
                    + "\t                (e.g. \"oracle.jdbc.OracleDriver\")\n\n"
                    + "\t<dbURL>       : Database URL\n"
                    + "\t                (e.g. \"jdbc:oracle:thin:YOUR_USERNAME/YOUR_PASSWORD@HOST:PORT:oracle\")\n");
            System.exit(-1);
        }

        // Check valid drivers

        try {
            Class.forName(driverClass);
            dbconn = DriverManager.getConnection(dbURL);

        } catch (ClassNotFoundException e) {
            System.err.println("*** ClassNotFoundException:  "
                    + "Error loading JDBC driver \"" + driverClass + "\"\n"
                    + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Unable to get connection to DB: " + dbURL);
        }

        // Begin CLI loop

        String query;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Enter a command (or 'help', 'exit'): ");

            try {

                query = br.readLine();

                if (query.equalsIgnoreCase("exit"))
                    break;

                else if (query.equalsIgnoreCase("help"))
                    System.out.println(
                                    "\n\t--------------------------------------------------------------\n\n"
                                    + "\tNOTE:\n"
                                    + "\tIn Oracle <schema> is user (student) which the tables are under\n\n"
                                    + "\t(e.g. <schema>.TABLE_NAME)\n\n"
                                    + "\t--------------------------------------------------------------\n\n"
                                    + "\t?a <schema>                         - Display incident count by year\n\n"
                                    + "\t?b <schema> <year>                  - Display incident count by state \n"
                                    + "\t                                      (top 10, descend) for given year\n\n"
                                    + "\t?c <schema> <year1> <year2>         - Display largest % drop in \n"
                                    + "\t                                      incident count by state (top 5, \n"
                                    + "\t                                      descend) between given years\n\n"
                                    + "\t?d <schema> <year1> <year2> <field> - Display largest % drop in \n"
                                    + "\t                                      incident count by field (top 10, \n"
                                    + "\t                                      descend) between given years\n\n"
                                    + "\t--------------------------------------------------------------\n\n"
                                    + "\t(All other commands will be treated as SQL statements)\n\n"
                                    + "\t--------------------------------------------------------------\n");

                else if (query.toLowerCase().startsWith("?a")) {
                    String schema = query.split("\\s+")[1];
                    Prog3.executeA(dbconn, schema);

                } else if (query.toLowerCase().startsWith("?b")) {
                    String schema = query.split("\\s+")[1];
                    String year = query.split("\\s+")[2];
                    Prog3.executeB(dbconn, schema, year);

                } else if (query.toLowerCase().startsWith("?c")) {
                    String schema = query.split("\\s+")[1];
                    String year1 = query.split("\\s+")[2];
                    String year2 = query.split("\\s+")[3];
                    Prog3.executeC(dbconn, schema, year1, year2);

                } else if (query.toLowerCase().startsWith("?d")) {
                    String schema = query.split("\\s+")[1];
                    String year1 = query.split("\\s+")[2];
                    String year2 = query.split("\\s+")[3];
                    String field = query.split("\\s+")[4];
                    Prog3.executeD(dbconn, schema, year1, year2, field);

                } else if (query.toLowerCase().startsWith("select"))
                    Prog3.executeQuery(query, dbconn);

                else
                    Prog3.executeUpdate(query, dbconn);

            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("\n*** SQLException:");
                System.err.println("    Message:   " + e.getMessage());
                System.err.println("    SQLState:  " + e.getSQLState());
                System.err.println("    ErrorCode: " + e.getErrorCode() + "\n");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Array out of bounds (..possibly missing a command arg?)");
            }
        }

        try {
            br.close();
            dbconn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}