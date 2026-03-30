import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * File: CreateTable.java
 * Author: Gabe Venegas
 * Course: CSC 460
 * Desc: CSV scrubber for highwayrail data, inserted to db via JDBC.
 * We expect a certain format of each line (tuple) in CSV:
 * 
 * railroad_code                VARCHAR2(100)
 * incident_number              VARCHAR2(100)
 * grade_crossing_id            VARCHAR2(100)
 * date                         DATE
 * time                         TIME
 * state_name                   VARCHAR2(100)
 * highway_user                 VARCHAR2(100)
 * temperature                  INT
 * visibility                   VARCHAR2(100)
 * weather_condition            VARCHAR2(100)
 * number_of_locomotive_units   INT
 * number_of_cars               INT
 */
public class CreateTable {

    private static String csvToSql(String csvRow) {

        DateTimeFormatter formatTimeCSV = DateTimeFormatter.ofPattern("hh:mm a");
        DateTimeFormatter formatTimeSQL = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Split line for parse
        String[] parts = csvRow.split(",");
        
        // Validate & transform each field
        for (int i = 0; i < parts.length; i++) {

            // if empty, set NULL
            if (parts[i].isEmpty()) {
                parts[i] = "NULL";
            }
            // date
            else if (i == 3) {
                parts[i] = "'" + LocalDate.parse(parts[3]).toString() + "'";
            }
            // time
            else if (i == 4) {
                parts[i] = "'" + LocalTime.parse(parts[4], formatTimeCSV).format(formatTimeSQL) + "'";
            }
            // (int) tempurature, num locomotive units, num cars
            else if (Arrays.asList(7, 10, 11).contains(i)) {
                parts[i] = Integer.toString(Integer.parseInt(parts[i]));
            }
            // (string) everything else
            else {
                parts[i] = "'" + parts[i] + "'";
            }
        }

        // Assemble & return tuple
        return "(" + String.join(", ", parts) + ")";
    }

    public static void initTable(String schemaName, String tableName, Connection dbconn) throws SQLException {

        // Ensure schema exists
        try (Statement stmt = dbconn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        }
        System.out.println("Success \"" + schemaName + "\" schema creation!");

        // Ensure table exists
        try (Statement stmt = dbconn.createStatement()) {
            String createSql = "CREATE TABLE IF NOT EXISTS " + schemaName + ".\"" + tableName + "\" "
                    + "("
                    + "railroad_code VARCHAR2(100),"
                    + "incident_number VARCHAR2(100),"
                    + "grade_crossing_id VARCHAR2(100),"
                    + "date DATE,"
                    + "time TIME,"
                    + "state_name VARCHAR2(100),"
                    + "highway_user VARCHAR2(100),"
                    + "temperature INT,"
                    + "visibility VARCHAR2(100),"
                    + "weather_condition VARCHAR2(100),"
                    + "number_of_locomotive_units INT,"
                    + "number_of_cars INT"
                    + ")";
            stmt.execute(createSql);
        }
        System.out.println("Success " + schemaName + ".\"" + tableName + "\" table creation!");

    }

    public static void populateTable(String schemaName, String tableName, String csvpath, Connection dbconn) throws SQLException {

        final String insertSql = "INSERT INTO " + schemaName + ".\"" + tableName + "\" "
        + "(railroad_code, incident_number, grade_crossing_id, date, time, state_name, highway_user, temperature, visibility, weather_condition, number_of_locomotive_units, number_of_cars)"
        + " VALUES ";
        
        String line = "";
        int lineCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvpath));
            Statement stmt = dbconn.createStatement()) {
            
            dbconn.setAutoCommit(false);
            
            br.readLine(); // skip headers
            
            while ((line = br.readLine()) != null) {

                stmt.addBatch(insertSql + CreateTable.csvToSql(line));

                // batch insertions every 500 lines
                if (lineCount % 500 == 0) {
                    stmt.executeBatch();
                    dbconn.commit();
                }
            }

            // commit last batch
            stmt.executeBatch();
            dbconn.commit();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            dbconn.setAutoCommit(true);
        }
    }
    
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
        try (Connection dbconn = DriverManager.getConnection(dbURL)) {

            CreateTable.initTable(schemaName, tableName, dbconn);
            CreateTable.populateTable(schemaName, tableName, csvpath, dbconn);

        } catch (SQLException e) {

            System.err.println("*** SQLException:");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }

    }
}
