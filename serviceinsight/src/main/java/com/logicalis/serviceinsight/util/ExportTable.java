package com.logicalis.serviceinsight.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * This Class reads a tab-delimited csv file that represents an export from the
 * Chronos Export_Table and churns out SQL insert statements as a ready-to-run
 * bash script on a CentOS machine that has freetds installed on it.
 * 
 * See the following Confluence entry for more info on using freetds/TSQL on a CentOS
 * server like this:
 *
 * https://confluence.logicaliscloud.com/confluence/display/SI/Integration+with+Chronos
 *
 * @author poneil
 */
public class ExportTable {

    static String STATIC_HEADER = "tsql -S devlab -U ServiceInsight -D Service_Insight_Export -P s3rv1c3 <<EOF";
    static String STATIC_INSERT = "insert into Export_Table (UID, Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name,"
            + " Task_Description, Subtask_Description, Hours, Num_CIs, Name, Team, Payrollcode, Labor_Type) values (";

    public static void main(String[] args) {
        String filename = args[0];
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename));
            String row;
            int rowcounter = 0;
            int govalue = 1000;
            System.out.println(STATIC_HEADER);
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split("\t");
                StringBuilder builder = new StringBuilder(STATIC_INSERT);
                int counter = 0;
                for (String val : data) {
                    switch (counter) {
                        case 0: // UID
                            builder.append(val).append(", ");
                            break;
                        case 1: // Ticket
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 2: // Date
                            builder.append("'").append(val).append("', ");
                            break;
                        case 3: // Customer_ID
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 4: // Customer
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 5: // CI_ID
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 6: // CI_Name
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 7: // Service_Name
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 8: // Task_Description
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 9: // Subtask_Description
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 10: // Hours
                            builder.append(valueOrNull(val)).append(", ");
                            break;
                        case 11: // Num_CIs
                            builder.append(valueOrNull(val)).append(", ");
                            break;
                        case 12: // Name
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 13: // Team
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 14: // Payrollcode
                            builder.append(formattedValueOrNull(val)).append(", ");
                            break;
                        case 15: // Labor_Type
                            builder.append(formattedValueOrNull(val)).append("");
                            break;
                    }
                    counter++;
                }
                builder.append(");");
                System.out.println(builder.toString());
                rowcounter++;
                if (rowcounter - govalue == 0) {
                    System.out.println("GO");
                    govalue += 1000;
                }
            }
            System.out.println("GO");
            System.out.println("exit");
            System.out.println("EOF");
            csvReader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExportTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExportTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean empty(String value) {
        return StringUtils.isBlank(value);
    }

    private static String valueOrNull(String value) {
        return (StringUtils.isBlank(value) ? "null" : value);
    }

    private static String formattedValueOrNull(String value) {
        return (StringUtils.isBlank(value) ? "null" : "'" + value + "'");
    }
}
