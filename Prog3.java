
import java.io.*;
import java.sql.*;

public class Prog3 {

    public static Connection getConnection(String driverClass, String dbURL) {
        // load the JDBC driver by initializing its base class.
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            System.err.println("*** ClassNotFoundException:  "
                    + "Error loading JDBC driver \""+driverClass+"\"\n"
                    + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        }

        // make and return a database connection to the user's db
        Connection dbconn = null;
        try {
            dbconn = DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return dbconn;
    }

    public static Connection getConnection(String[] args) {
        String dbURL = null, driverClass = null;

        if (args.length == 2) { // get credentials from user args
            driverClass = args[0];
            dbURL = args[1];
        } else {
            System.out.println("\nUsage: java Prog3 <driverClass> <dbURL>\n"
                    + "\t<driverClass> : JDBC driver classname (e.g. \"oracle.jdbc.OracleDriver\")\n"
                    + "\t<dbURL>       : Database URL (e.g. \"jdbc:oracle:thin:YOUR_USERNAME/YOUR_PASSWORD@HOST:PORT:oracle\")\n");
            System.exit(-1);
        }
        return Prog3.getConnection(driverClass, dbURL);
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

    public static void executeQuery(String query, Connection dbconn) {

        try (Statement stmt = dbconn.createStatement();
            ResultSet answer = stmt.executeQuery(query)) {
            
            if (answer != null) {
                System.out.println(Prog3.resultSetToString(answer));
            }

        } catch (SQLException e) {

            System.err.println("*** SQLException:  "
                    + "Could not fetch query results.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
        }
    }

    public static void executeUpdate(String sql, Connection dbconn) {
        try (Statement stmt = dbconn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            System.out.println("Rows updated: " + rowsAffected);

        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                    + "Could not process execute update.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
        }
    }

    public static void main(String[] args) {

        Connection dbconn = Prog3.getConnection(args);
        String query;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Enter SQL (or 'help', 'exit'): ");
            try {
                query = br.readLine();
                if (query.equalsIgnoreCase("exit"))
                    break;
                else if (query.equalsIgnoreCase("help"))
                    System.out.println(
                    "\n\t(All other commands will be treated as SQL queries)\n\n"
                    + "\t+t <file>           - Create Highwayrail table using\n"
                    + "\t                      CSV file \"highwayrail????.csv\"\n"
                    + "\t                      where ???? == year/tablename\n\n"
                    + "\t-t <table>          - Drop Highwayrail table by \n"
                    + "\t                     tablename (it's year)\n\n"
                    + "\t?cy                 - Display incident count by year\n\n"
                    + "\t?cs <year>          - Display incident count by state \n"
                    + "\t                      (top 10, descend) for given year\n\n"
                    + "\t?pd <year1> <year2> - Display largest % drop in \n"
                    + "\t                      incident count by state (top 5, \n"
                    + "\t                      descend) between given years\n\n"
                    + "\t?pi <year1> <year2> - Display largest % increase in \n"
                    + "\t                      incident count by state (top 5, \n"
                    + "\t                      descend) between given years\n"
                    );
                else {
                    if (query.toLowerCase().startsWith("select"))
                        Prog3.executeQuery(query, dbconn);
                    else
                        Prog3.executeUpdate(query, dbconn);
                }
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
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