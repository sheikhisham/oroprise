package com.oroprise.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Ordering;
import com.oroprise.bo.Connection;
import com.oroprise.bo.Fraction;
import com.oroprise.bo.MeterReading;
import com.oroprise.bo.MeterRecord;
import com.oroprise.bo.Month;
import com.oroprise.bo.Profile;
import com.oroprise.repository.MeterReadingRepository;
import com.oroprise.repository.ProfileRepository;
import com.oroprise.vo.MeterReadingStatus;
import com.oroprise.vo.MeterReadingVo;

import lombok.extern.slf4j.Slf4j;

@Service
public class CreateMeterReadingService {

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private MeterReadingRepository meterReadingRepository;

	/**
	 *  Takes list of MeterReadingVo
	 *  Step1 : Group the MeterReadingVos by profileNames
	 *  Step2 : Validate profile names and add invalid data to error list
	 *  Step3 : Group the MeterReadingVos by profileName->connectionId
	 *  Step4 : Order the MeterReadings by Month
	 *  Step5 : Validate MeterReadings, greater than previous month
	 *  Step6 : Validate Consumption data, add failed data to error list
	 *  Step7 : Build MeterReadingVos of distinct connection to MeterReading POJO
	 *  Step8 : Persist Data that had passes all validations
	 *  
	 *  Returns List<MeterReadingStatus> having all connections and status (SUCCESS/FAILURE) 
	 * 
	 * @param meterReadingVos
	 * @return
	 */
	public List<MeterReadingStatus> execute(List<MeterReadingVo> meterReadingVos) {
		List<MeterReadingStatus> result = new ArrayList<>();

		// Step1 : group by profilename
		Map<String, List<MeterReadingVo>> profileGroup = meterReadingVos.parallelStream()
				.collect(groupingBy(MeterReadingVo::getProfileName, toList()));

		for (Entry<String, List<MeterReadingVo>> e : profileGroup.entrySet()) {
			String profileName = e.getKey();
			// Step2 : validate profile existence
			Profile profile = profileRepository.findOne(profileName);
			if (null == profile) {
				// profile NOTFOUND error response added
				result.add(new MeterReadingStatus(profileName, null, "FAILURE, Profile NOT FOUND"));
				continue;
			}

			// Step3 : group by ConnectionId
			Map<String, List<MeterReadingVo>> connectionGroup = e.getValue().stream()
					.collect(groupingBy(MeterReadingVo::getConnectionId, toList()));
			for (Entry<String, List<MeterReadingVo>> c : connectionGroup.entrySet()) {
				String connectionId = c.getKey();
				// validate whether 12 readings are present
				if(c.getValue().size() != 12) {
					// connection readings invalid order error response added
					result.add(new MeterReadingStatus(profileName, connectionId,
							"FAILURE, Readings Insufficient ie., not all months data found"));
					continue;
				}
				
				// Step4 : Order the MeterReadings by Month
				List<Long> cReadings = c.getValue().stream().sorted(MeterReadingVo.METER_MONTH_COMPARATOR)
						.collect(mapping(MeterReadingVo::getReading, toList()));
				boolean isReadingsAsc = Ordering.natural().isOrdered(cReadings);
				// Step5 : validate reading value greater than previous one
				if (!isReadingsAsc) {
					// connection readings invalid order error response added
					result.add(new MeterReadingStatus(profileName, connectionId,
							"FAILURE, Readings Invalid ie., not in ascending Order"));
					continue;
				}

				Fraction[] fractions = profile.getFractions().toArray(new Fraction[profile.getFractions().size()]);
				// Step 6 & Step7 : build MeterReading POJO, TODO validate consumption
				MeterReading meterReading = new MeterReading();
				meterReading.setConnection(new Connection(profileName, connectionId));
				Month[] months = Month.values();
				List<MeterRecord> meterRecords = new ArrayList<>();
				// starting reading
				long prevReading = 0;
				// reading in DEC month, considering starting reading in each year would be 0
				long thisYearConsumption = cReadings.get(11);
				boolean isConsumptionDataValid = true;
				for (int i = 0; i < 12; i++) {
					long thisMonthReading = cReadings.get(i);
					long consumption = thisMonthReading - prevReading;
					double fraction = fractions[i].getValue();
					long allowedConsumptionFrom = (long) (fraction * thisYearConsumption);
					long allowedConsumptionTill = allowedConsumptionFrom + (long) (allowedConsumptionFrom * 0.25);
					
					if(consumption > allowedConsumptionTill || consumption < allowedConsumptionFrom) {
						result.add(new MeterReadingStatus(profileName, connectionId,
								String.format("consumption: %d, allowedConsumption: %d to %d, ie., AllowedMeterReadingRange for %s: %d to %d, totalyearconsumption: %d", consumption, allowedConsumptionFrom, allowedConsumptionTill, months[i], prevReading+allowedConsumptionFrom, prevReading+allowedConsumptionTill , thisYearConsumption)));
						isConsumptionDataValid = false;
						break;
					}

					MeterRecord record = new MeterRecord(months[i], thisMonthReading);
					meterRecords.add(record);
					prevReading = thisMonthReading;
				}
				meterReading.setMeterRecords(meterRecords);
				
				if(!isConsumptionDataValid) {
					continue;
				}

				// Step8 : save readings
				meterReadingRepository.save(meterReading);
				result.add(new MeterReadingStatus(profileName, connectionId, "SUCCESS"));
			}
		}
		
		return result;
	}
}
