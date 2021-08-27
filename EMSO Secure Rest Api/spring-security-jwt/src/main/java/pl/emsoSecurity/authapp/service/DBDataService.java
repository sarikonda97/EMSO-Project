package pl.emsoSecurity.authapp.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.emsoSecurity.authapp.constants.miscConstants;
import pl.emsoSecurity.authapp.helpers.helper;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Service
public class DBDataService {

    private static final Logger log = LoggerFactory.getLogger(DBDataService.class);
    private static final String jdbcConnectionUrl = "jdbc:postgresql://localhost/emso";
    private static final String user = "postgres";
    private static final String password = "emso@123";



    private Connection conn;

    /***
     * This method is used to connect to the Postgres Database
     * @return
     */
    private Connection connectToDB() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcConnectionUrl, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }

        return conn;
    }

    @PostConstruct
    public void init() throws SQLException, IOException {
        conn = connectToDB();
    }

    /***
     * This method converts the JSONArray data into a CSV
     * @param result
     * @return response object
     */
    private HttpEntity<? extends Object> csvHandler(
            List<List<String>> result)
    {
        ByteArrayInputStream byteArrayOutputStream;

        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // defining the CSV printer
                CSVPrinter csvPrinter = new CSVPrinter(
                        new PrintWriter(out),
                        // withHeader is optional
                        CSVFormat.DEFAULT.withHeader(miscConstants.csvHeader)
                )
        ) {
            // populating the CSV content
            List<List<String>> csvBody = helper.processResponseJson(result);

            for (List<String> record : csvBody)
                csvPrinter.printRecord(record);

            // writing the underlying stream
            csvPrinter.flush();

            byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        InputStreamResource fileInputStream = new InputStreamResource(byteArrayOutputStream);

        String csvFileName = "data.csv";

        // setting HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
        // defining the custom Content-Type
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");

        return new ResponseEntity<InputStreamResource>(
                fileInputStream,
                headers,
                HttpStatus.OK
        );
    }

    /***
     * This method is used to get all the rows in the DB
     * @param responseType
     * @return response object
     * @throws SQLException
     */
    public HttpEntity<? extends Object> getAllData(
            String responseType)
            throws SQLException
    {
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet idQueryResultSet = stmt.executeQuery("SELECT * FROM vehicle_data.location");
        ResultSetMetaData rsmd = idQueryResultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<List<String>> result = new ArrayList<>();
        while (idQueryResultSet.next()) {
            List<String> dataRow = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = idQueryResultSet.getString(i);
                dataRow.add(columnValue);
            }
            result.add(dataRow);
        }

        if ("json".equals(responseType))
        {
            return new ResponseEntity<>(
                    result,
                    HttpStatus.OK
            );
        }

        return csvHandler(result);
    }

    /***
     * This method is used to get all the data rows corresponding to a specific OBD id.
     * @param ObdId
     * @param responseType
     * @return response object
     * @throws SQLException
     */
    public HttpEntity<? extends Object> getDataByObdId(
            String ObdId,
            String responseType)
            throws SQLException
    {
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet idQueryResultSet = stmt.executeQuery("SELECT * FROM vehicle_data.location WHERE vehicle_id='" + ObdId + "'");
        ResultSetMetaData rsmd = idQueryResultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<List<String>> result = new ArrayList<>();
        while (idQueryResultSet.next()) {
            List<String> dataRow = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = idQueryResultSet.getString(i);
                dataRow.add(columnValue);
            }
            result.add(dataRow);
        }

        if ("json".equals(responseType))
        {
            return new ResponseEntity<>(
                    result,
                    HttpStatus.OK
            );
        }

        return csvHandler(result);
    }

    /***
     * This method is used to return all the data rows that correspond to a timestamp range.
     * @param startTime
     * @param endTime
     * @param responseType
     * @return response object
     * @throws SQLException
     */
    public HttpEntity<? extends Object> getDataByTimestampRange(
            String startTime,
            String endTime,
            String responseType)
            throws SQLException
    {

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet idQueryResultSet = stmt.executeQuery("SELECT * FROM vehicle_data.location WHERE cast(created_at as int) BETWEEN " + startTime + " AND " + endTime);

        ResultSetMetaData rsmd = idQueryResultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<List<String>> result = new ArrayList<>();
        while (idQueryResultSet.next()) {
            List<String> dataRow = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = idQueryResultSet.getString(i);
                dataRow.add(columnValue);
            }
            result.add(dataRow);
        }

        if ("json".equals(responseType))
        {
            return new ResponseEntity<>(
                    result,
                    HttpStatus.OK
            );
        }

        return csvHandler(result);
    }

    /***
     * This method is used to retrieve all the rows with a specific obd id and timestamp range.
     * @param ObdId
     * @param startTime
     * @param endTime
     * @param responseType
     * @return
     * @throws SQLException
     */
    public HttpEntity<? extends Object> getDataByObdIdAndTimestampRange(
            String ObdId,
            String startTime,
            String endTime,
            String responseType)
            throws SQLException
    {

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet idQueryResultSet = stmt.executeQuery("SELECT * FROM vehicle_data.location WHERE vehicle_id='" + ObdId + "' AND " + "cast(created_at as int) BETWEEN " + startTime + " AND " + endTime);

        ResultSetMetaData rsmd = idQueryResultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<List<String>> result = new ArrayList<>();
        while (idQueryResultSet.next()) {
            List<String> dataRow = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = idQueryResultSet.getString(i);
                dataRow.add(columnValue);
            }
            result.add(dataRow);
        }

        if ("json".equals(responseType))
        {
            return new ResponseEntity<>(
                    result,
                    HttpStatus.OK
            );
        }

        return csvHandler(result);
    }
}
