package com.example.emsoDaemon;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

import static com.example.emsoDaemon.PidConstants.pidDecimalToHexMap;

@Component
public class daemonProcess {

    private static final Logger log = LoggerFactory.getLogger(daemonProcess.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String jdbcConnectionUrl = "jdbc:postgresql://localhost/emso";
    private static final String user = "postgres";
    private static final String password = "emso@123";

    private JSONArray registeredVehicles = new JSONArray();
    private Connection conn;
    private JSONObject obdTimestamps = new JSONObject();
    private JSONObject staleObdTimeStamps = new JSONObject();
    private JSONObject response;

    /***
     * Method to connect to the postgres DB.
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

    /***
     * This method returns the list of a vehicles that are registered.
     * @param conn
     * @return JSONArray
     * @throws SQLException
     */
    private JSONArray returnListOfVehicles(Connection conn) throws SQLException {
        JSONArray registeredVehicles = new JSONArray();

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = null;
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM vehicle_data.vehicles");
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                    entry.put(rsmd.getColumnName(i), columnValue);
                }
                System.out.println("");
                registeredVehicles.put(entry);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }

        return registeredVehicles;
    }

    /***
     * This is aeneral helper method to get the JSON response from the url.
     * @param url
     * @return JSONObject
     * @throws IOException
     */
    private JSONObject getJSONResponse(String url) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = httpClient.execute(request);
        BufferedReader bufferedReader = new BufferedReader
                (new InputStreamReader(
                        response.getEntity().getContent()));
        StringBuilder responseStringBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = bufferedReader.readLine()) != null)
            responseStringBuilder.append(inputStr);

        JSONObject responseJson = new JSONObject();
        try {
             responseJson = new JSONObject(responseStringBuilder.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return responseJson;
    }

    /***
     * This method is used to poll for the list of channels that are available.
     * @return List of Strings
     * @throws IOException
     */
    private List<String> getChannels() throws IOException {
        String channelsUrl = "http://129.128.32.112:8080/api/channels";
        JSONObject response = getJSONResponse(channelsUrl);
        List<String> channels = new ArrayList<>();

        if (response.has("channels"))
        {
            JSONArray channelArray = response.getJSONArray("channels");
            IntStream
                    .range(0, channelArray.length())
                    .mapToObj(channelArray::getJSONObject)
                    .forEach( obj -> channels.add(obj.getString("devid")));

        }

        return channels;
    }

    /***
     * This method verifies if the channels available are all legible by cross-verifying with the registered vehicles.
     * @param channels
     * @param registeredVehicles
     */
    private void verifyChannels(List<String> channels, JSONArray registeredVehicles) {
        channels.forEach(channel -> {
            if (!registeredVehicles.toString().contains(channel)) {
                System.out.println("*************************Security breach! Unknown OBD device identified with id: "+ channel + "*********************");
            }
        });
    }

    /***
     * This method writes the data pulled from freematics to a file dump for crossverification purposes.
     * @param response
     * @param currentUnixTimestamp
     */
    private void writeResponseToFileDump(JSONObject response, long currentUnixTimestamp)
    {
        try {
            FileWriter myWriter = new FileWriter("C:/Users/emlgroup/Desktop/polldumps/" + currentUnixTimestamp + ".txt");
            myWriter.write(response.toString());
            myWriter.close();
        } catch (IOException e) {
            System.out.println("Response File Dump Failed.");
            e.printStackTrace();
        }
    }

    /***
     * This method is used to pull the data from the channels verified.
     * @param channels
     * @return JSONArray
     */
    private JSONArray pullChannels(List<String> channels) {
        JSONArray channelsPulledData = new JSONArray();
        channels.forEach(channel -> {
            Long timeStamp = Long.valueOf(0);
            if (obdTimestamps.has(channel))
            {
                timeStamp = obdTimestamps.getLong(channel);
            }
            String pullUrl = "http://129.128.32.112:8080/api/pull/" + channel + "?ts=" + Long.toString(timeStamp);
            try {
                response = getJSONResponse(pullUrl);
                JSONObject stats = response.optJSONObject("stats");
                JSONArray live = response.getJSONArray("live");
                JSONArray data = response.getJSONArray("data");

                obdTimestamps.put(channel, stats.getLong("devtick")); // but we gotta use the current unix timestamp for this instead

                JSONObject locationData = new JSONObject();
                locationData.put("latitude", Types.NULL);
                locationData.put("longitude", Types.NULL);
                locationData.put("altitude", Types.NULL);
                IntStream
                        .range(0, live.length())
                        .mapToObj(live::getJSONArray)
                        .forEach(jsonArray -> {
                            if (jsonArray.getInt(0) == 10) {
                                locationData.put("latitude", jsonArray.getBigDecimal(1));
                            } else if (jsonArray.getInt(0) == 11) {
                                locationData.put("longitude", jsonArray.getBigDecimal(1));
                            } else if (jsonArray.getInt(0) == 12) {
                                locationData.put("altitude", jsonArray.getBigDecimal(1));
                            }
                        });

                // putting the data for this channel in returning array
                JSONObject location = new JSONObject();
                location.put("locationData", locationData);
                JSONObject sensorData = new JSONObject();
                sensorData.put("sensorData", data);
                JSONObject finalObj = new JSONObject();
                finalObj.put("location", location);
                finalObj.put("data", sensorData);
                JSONObject pulledChannelData = new JSONObject();
                pulledChannelData.put("channelName", channel);
                pulledChannelData.put("payload", finalObj);
                channelsPulledData.put(pulledChannelData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return channelsPulledData;
    }

    /***
     * This method is used to process the sensor data that is received.
     * @param sensorData
     * @return
     */
    private JSONArray processSensorData(JSONArray sensorData) {
        IntStream
                .range(0, sensorData.length())
                .mapToObj(sensorData::getJSONArray)
                .forEach(arr -> {
                    arr.put(1, pidDecimalToHexMap.containsKey(arr.getInt(1)) ? pidDecimalToHexMap.get(arr.getInt(1)) : arr.getInt(1) );
                });

        return sensorData;
    }

    /***
     * This method is used to verify if the data that is received is actually valid data or not.
     * @param sensorData
     * @param channel
     * @return
     */
    private boolean verifyDataFormatted(JSONArray sensorData, String channel)
    {
        if (sensorData.length() == 0)
        {
            return true;
        }
        Integer ts = new JSONArray(sensorData).getJSONArray(0).getInt(0);
        if (staleObdTimeStamps.has(channel) && staleObdTimeStamps.get(channel).equals(ts)) {
            return true;

        }
        staleObdTimeStamps.put(channel, ts);
        return false;
    }

    /***
     * This method decouples multiples rows of data that is received from freematics in a single call as chunked data.
     * @param sensorData
     * @return
     */
    private JSONArray decoupleData(JSONArray sensorData)
    {
        List<Integer> indices = new ArrayList<>();
        Integer dataCoupledCount = 1;
        Integer i=0;
        Integer currentDataTS = sensorData.getJSONArray(0).getInt(0);
        while(i < sensorData.length()) {
            if (currentDataTS != sensorData.getJSONArray(i).getInt(0))
            {
                indices.add(i);
                ++dataCoupledCount;
                currentDataTS = sensorData.getJSONArray(i).getInt(0);
            }
            i++;
        }

        ArrayList sensorDataArray = new ArrayList<JSONArray>();
        if (sensorData != null) {
            for (int k=0; k<sensorData.length(); k++) {
                sensorDataArray.add(sensorData.get(k));
            }
        }

        indices.add(sensorData.length());
        System.out.println(dataCoupledCount);
        JSONArray decoupledSensorData = new JSONArray();
        Integer j = 0;
        Integer startIndex = 0;
        while(j < dataCoupledCount) {
            decoupledSensorData.put(j, sensorDataArray.subList(startIndex, indices.get(j)));
            startIndex = indices.get(j);
            ++j;
        }

        return decoupledSensorData;
    }

    /***
     * This method is used to enter the data retrieved into the DB.
     * @param pulledChannelsData
     * @param conn
     * @throws SQLException
     */
    private void EntryChannelDataIntoDB(JSONArray pulledChannelsData, Connection conn) throws SQLException {
        long currentUnixTimeStamp = Instant.now().getEpochSecond();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        IntStream
                .range(0, pulledChannelsData.length())
                .mapToObj(pulledChannelsData::getJSONObject)
                .forEach(obj-> {
                    try {
                        String channelName = obj.getString("channelName");
                        ResultSet idQueryResultSet = stmt.executeQuery("SELECT id FROM vehicle_data.vehicles WHERE obd_id='" + channelName + "'");
                        if(idQueryResultSet.next())
                        {
                            String vehicleId = idQueryResultSet.getString(1);

                            JSONObject locationData = obj.getJSONObject("payload").getJSONObject("location").getJSONObject("locationData");
                            JSONArray sensorData = obj.getJSONObject("payload").getJSONObject("data").getJSONArray("sensorData");
                            if (verifyDataFormatted(sensorData, channelName))
                            {
                                return;
                            }

                            JSONArray decoupledSensorData = decoupleData(sensorData);
                            writeResponseToFileDump(response, currentUnixTimeStamp);

                            int arrayloop;
                            for (arrayloop = 0; arrayloop < decoupledSensorData.length(); arrayloop++)
                            {
                                PGobject sensorDataFormatted = new PGobject();
                                sensorDataFormatted.setType("json");
                                sensorDataFormatted.setValue(decoupledSensorData.get(arrayloop).toString());
                                String insertQuery = "INSERT INTO vehicle_data.location (vehicle_id, created_at, lat, long, alt, data) " +
                                        "VALUES ('" + vehicleId + "', '" + currentUnixTimeStamp + "', '" + locationData.getBigDecimal("latitude") +
                                        "' , '" + locationData.getBigDecimal("longitude") + "', '"
                                        + locationData.getBigDecimal("altitude")+ "', '" + sensorDataFormatted + "')";
                                int result = stmt.executeUpdate(insertQuery);
                                if (result>0)
                                {
                                    System.out.println("DB Updated with rows: " + result + " at channel: " + channelName);
                                }
                            }
                            idQueryResultSet.close();
                        }
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        log.error(e.getMessage());
                    }
                });
    }

    @PostConstruct
    public void init() throws SQLException, IOException {
        conn = connectToDB();
        registeredVehicles = returnListOfVehicles(conn);
    }

    /***
     * This method is used to run the entire daemon process cyclically.
     * @throws IOException
     * @throws SQLException
     */
    @Scheduled(fixedRate = 4000)
    public void reportCurrentTime() throws IOException, SQLException {
//        log.info("The time is now {}", dateFormat.format(new Date()));
        List<String> channels = getChannels();
        verifyChannels(channels, registeredVehicles);
        JSONArray pulledChannelsData = pullChannels(channels);
        EntryChannelDataIntoDB(pulledChannelsData, conn);
    }
}
