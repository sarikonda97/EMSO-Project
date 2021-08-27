package pl.emsoSecurity.authapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.emsoSecurity.authapp.service.DBDataService;

import javax.annotation.Resource;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/data")
public class DBDataController {

    private DBDataService dbDataService;

    @Autowired
    public DBDataController(DBDataService dbDataService) {
        this.dbDataService = dbDataService;
    }

    /***
     * This api returns all the rows in the database in either JSON or CSV Format
     * @param responseType
     * @return ResponseEntity
     * @throws SQLException
     */
    @GetMapping(value = "getAllData")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> getAllData(
            @RequestParam("response_type") String responseType)
            throws SQLException
    {
        return (ResponseEntity<Resource>) dbDataService.getAllData(responseType);
    }

    /***
     * This api returns the rows in the database that have the respective OBD ID in either JSON or CSV Format
     * @param obdId
     * @param responseType
     * @return ResponseEntity
     * @throws SQLException
     */
    @GetMapping(value = "getDataByObdId")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> getDataByObdId(
            @RequestParam("obd_id") String obdId,
            @RequestParam("response_type") String responseType)
            throws SQLException
    {
        return (ResponseEntity<Resource>) dbDataService.getDataByObdId(obdId, responseType);
    }

    /***
     * This api returns the rows in the database that are within the timestamp range in either JSON or CSV Format
     * @param startTime
     * @param endTime
     * @param responseType
     * @return ResponseEntity
     * @throws SQLException
     */
    @GetMapping(value = "getDataByTimestampRange")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> getDataByTimestampRange(
            @RequestParam("start_time") String startTime,
            @RequestParam("end_time") String endTime,
            @RequestParam("response_type") String responseType)
            throws SQLException
    {
        return (ResponseEntity<Resource>) dbDataService.getDataByTimestampRange(startTime, endTime, responseType);
    }

    /***
     * This api returns the rows in the database that have the corresponding OBD ID and are in the range of the timestamps in either JSON or CSV Format
     * @param obdId
     * @param startTime
     * @param endTime
     * @param responseType
     * @return ResponseEntity
     * @throws SQLException
     */
    @GetMapping(value = "getDataByObdIdAndTimestampRange")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> getDataByObdIdAndTimestampRange(
            @RequestParam("obd_id") String obdId,
            @RequestParam("start_time") String startTime,
            @RequestParam("end_time") String endTime,
            @RequestParam("response_type") String responseType)
            throws SQLException
    {
        return (ResponseEntity<Resource>) dbDataService.getDataByObdIdAndTimestampRange(obdId, startTime, endTime, responseType);
    }
}
