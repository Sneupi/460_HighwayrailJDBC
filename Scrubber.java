import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * File: Scrubber.java
 * Author: Gabe Venegas
 * Course: CSC 460
 * Desc: CSV scrubber for train data, prior to JDBC db insertion,
 * where we expect a certain format of each line in trains CSV:
 * 
 * Railroad Code: string
 * Incident Number: string
 * Grade Crossing ID: string
 * Date: date
 * Time: time
 * State Name: string
 * Highway User: string
 * Temperature: int
 * Visibility: string
 * Weather Condition: string
 * Number of Locomotive Units: int
 * Number of Cars: int
 */
public class Scrubber {
    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: Scrubber.java <csv | csv list>\n");
            return;
        }

        // Allow iteration over multiple CSV
        for (int i = 0; i < args.length; i++) {
            if (!args[i].endsWith(".csv") || !(new File(args[i]).exists())) {
                System.err.println(String.format("Invalid csv: \"%s\"\n", args[i]));
                continue;
            }
            scrub(args[i]);
        }

    }

    private static void scrub(String csvpath) throws Exception {
        // Check CSV rows for errors

        String line = "";
        int lineNum = 2; // 1-based CSV index
        int countErrs = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

        BufferedReader br = new BufferedReader(new FileReader(csvpath));
        br.readLine(); // skip headers

        while ((line = br.readLine()) != null) {
            try {
                String[] parts = line.split(",");

                if (!parts[3].isEmpty()) LocalDate.parse(parts[3]); // Date
                if (!parts[4].isEmpty()) LocalTime.parse(parts[4], formatter); // Time
                if (!parts[7].isEmpty()) Integer.parseInt(parts[7]); // Temperature
                if (!parts[10].isEmpty()) Integer.parseInt(parts[10]); // # Locomotive Units
                if (!parts[11].isEmpty()) Integer.parseInt(parts[11]); // # Cars

                lineNum++;
            } catch (Exception e) {
                System.err.println(String.format("Error: line %d: \"%s\"\n%s\n", lineNum, line, e.getMessage()));
                countErrs++;
            }
        }
        br.close();
        System.out.println(String.format("%s: Read %d lines, Encountered %d errors.\n", csvpath, lineNum, countErrs));
    }
}
