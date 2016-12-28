/**
 * 
 */
package com.oroprise.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.oroprise.bo.Connection;
import com.oroprise.bo.MeterReading;
import com.oroprise.repository.MeterReadingRepository;
import com.oroprise.service.CreateMeterReadingService;
import com.oroprise.util.HeaderUtil;
import com.oroprise.vo.MeterReadingStatus;
import com.oroprise.vo.MeterReadingVo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author danis
 *
 */
@RestController
@Slf4j
@RequestMapping(value = "/api")
public class MeterReadingResource {
	
	@Autowired
	private MeterReadingRepository meterReadingRepository;
	
	@Autowired
	private CreateMeterReadingService createMeterReadingService;
	
	/**
     * POST  /meterreadings : Creates new Meter Readings
     *
     * @param meterReadingVos list of meterReadingVos to create
     * @return the ResponseEntity with status 201 (Created) and with body the new meterReadings created, or with status 400 (Bad Request) if the meter readings validation failed
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
	@RequestMapping(value = "/meterreadings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<MeterReadingStatus>> createMeterReadings(@RequestBody List<MeterReadingVo> meterReadingVos) throws URISyntaxException {
		log.debug("Enter createMeterReadings");
		
		List<MeterReadingStatus> result = createMeterReadingService.execute(meterReadingVos);
		
		log.debug("Exit createMeterReadings");
		
		return ResponseEntity.created(new URI("/api/meterreadings/")).body(result);
	}
	
	/**
     * GET  /meterreadings : get all the meterReadings.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of meterReadings in body
     */
    @RequestMapping(value = "/meterreadings",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MeterReading> getAllMeterReadings() {
        log.debug("REST request to get all meterReadings");
        List<MeterReading> meterReadings = meterReadingRepository.findAll();
        return meterReadings;
    }
    
    /**
     * GET  /meterReadings/:profilename/:connectionid : get the meter reading by connection.
     *
     * @param connection the id of the meter reading to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the MeterReading, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/meterreadings/{profilename}/{connectionid}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeterReading> getProfile(@PathVariable(name="profilename") String profileName, @PathVariable(name="connectionid") String connectionId) {
        log.debug(String.format("REST request to get profile : {} %s %s", profileName, connectionId));
        MeterReading meterReading = meterReadingRepository.findOne(new Connection(profileName, connectionId));
        return Optional.ofNullable(meterReading)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * DELETE  /meterreadings : delete the meterReadings.
     *
     * @param name the name of the meterReadings to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/meterreadings",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteMeterReading(@RequestBody Connection connection) {
        log.debug("REST request to delete meterReadings : {}", connection.toString());
        meterReadingRepository.delete(connection);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("meterReading", connection.toString())).build();
    }
}
