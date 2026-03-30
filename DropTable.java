import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * File: DropTable.java
 * Author: Gabe Venegas
 * Course: CSC 460
 * Desc: Drops a table from db via JDBC, allowing reset of test env.
 */
public class DropTable {

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("\nUsage: java CreateTable <schemaName> <csvpath> <driverClass> <dbURL>\n\n"
                    + "\t<schemaName>  : Name of schema which tables are created\n"
                    + "\t                (e.g. \"YOUR_USERNAME\")\n\n"
                    + "\t<csvpath>     : A csv matching highwayrail*.csv\n"
                    + "\t                (e.g. \"highwayrail2025.csv\")\n\n"
                    + "\t<driverClass> : JDBC driver classname\n"
                    + "\t                (e.g. \"oracle.jdbc.OracleDriver\")\n\n"
                    + "\t<dbURL>       : Database URL\n"
                    + "\t                (e.g. \"jdbc:oracle:thin:YOUR_USERNAME/YOUR_PASSWORD@HOST:PORT:oracle\")\n");
            System.exit(-1);
        }

        // Get user args
        String schemaName = args[0];
        String csvpath = args[1];
        String driverClass = args[2];
        String dbURL = args[3];

        // Load the JDBC driver by init its base class
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            System.err.println("*** ClassNotFoundException:  "
                    + "Error loading JDBC driver \"" + driverClass + "\"\n"
                    + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        }

        // Check CSV valid
        if (!csvpath.matches("^highwayrail.{4}\\.csv$") || !new File(csvpath).exists()) {
            System.out.println(
                    "Invalid CSV file \"" + csvpath + "\". Should be valid file of format: \"highwayrail????.csv\"");
            System.exit(-1);
        }
        
        // Get year (tablename)
        String tableName = csvpath.substring(csvpath.length() - 8, csvpath.length() - 4);

        // Perform insertion of rows
        try (Connection dbconn = DriverManager.getConnection(dbURL);
            Statement stmt = dbconn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS " + schemaName + ".\"" + tableName + "\"" );
            System.out.println("Success \"" + schemaName + "." + tableName + "\" table dropped!");

        } catch (SQLException e) {

            System.err.println("*** SQLException:");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }

    }
}
