
import java.io.*;
import java.sql.*;

public class Prog3 {

    public static Connection getConnection(String oracleURL, String username, String password) {
        // load the (Oracle) JDBC driver by initializing its base
        // class, 'oracle.jdbc.OracleDriver'.
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("*** ClassNotFoundException:  "
                    + "Error loading Oracle JDBC driver.  \n"
                    + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        }

        // make and return a database connection to the user's
        // Oracle database
        Connection dbconn = null;
        try {
            dbconn = DriverManager.getConnection(oracleURL, username, password);
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
        String username = null, // Oracle DBMS username
                password = null, // Oracle DBMS password
                oracleURL = null;

        if (args.length == 3) { // get username/password from cmd line args
            oracleURL = args[0];
            username = args[1];
            password = args[2];
        } else {
            System.out.println("\nUsage: java Prog3 <oracleURL> <username> <password>\n"
                    + "\t<oracleURL> Oracle database URL\n"
                    + "\t<username> Oracle DBMS username\n"
                    + "\t<password> Oracle password (not system)");
            System.exit(-1);
        }
        return Prog3.getConnection(oracleURL, username, password);
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
        Statement stmt = null;
        ResultSet answer = null;

        try {

            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);

            if (answer != null) {
                System.out.println(Prog3.resultSetToString(answer));
            }

            stmt.close();

        } catch (SQLException e) {

            System.err.println("*** SQLException:  "
                    + "Could not fetch query results.");
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
            System.out.print("Enter SQL (or 'exit'): ");
            try {
                query = br.readLine();
                if (query.equalsIgnoreCase("exit"))
                    break;
                Prog3.executeQuery(query, dbconn);
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