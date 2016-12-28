package com.oroprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.oroprise.bo.Connection;
import com.oroprise.bo.Fraction;
import com.oroprise.bo.MeterReading;
import com.oroprise.bo.MeterRecord;
import com.oroprise.bo.Month;
import com.oroprise.bo.Profile;
import com.oroprise.repository.MeterReadingRepository;
import com.oroprise.repository.ProfileRepository;
import com.oroprise.service.CreateMeterReadingService;
import com.oroprise.vo.MeterReadingVo;
import com.oroprise.web.rest.MeterReadingResource;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
public class MeterReadingTests {

	private MockMvc restMeterReadingMockMVC;

	@Autowired
	private MappingJackson2HttpMessageConverter jacksonMessageConverter;

	@Autowired
	private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private MeterReadingRepository meterReadingRepository;

	@Autowired
	private CreateMeterReadingService createMeterReadingService;

	@PostConstruct
	public void setup() {
		MockitoAnnotations.initMocks(this);
		MeterReadingResource meterReadingResource = new MeterReadingResource();
		ReflectionTestUtils.setField(createMeterReadingService, "profileRepository", profileRepository);
		ReflectionTestUtils.setField(meterReadingResource, "meterReadingRepository", meterReadingRepository);
		ReflectionTestUtils.setField(meterReadingResource, "createMeterReadingService", createMeterReadingService);
		this.restMeterReadingMockMVC = MockMvcBuilders.standaloneSetup(meterReadingResource)
				.setCustomArgumentResolvers(pageableArgumentResolver).setMessageConverters(jacksonMessageConverter)
				.build();
	}

	@Before
	public void beforeEachTest() {
		meterReadingRepository.deleteAll();
	}

	/**
	 * Covers MeterReading creation, getAll success scenario
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	// @Test
	public void meterReadingResourceSuccessTest() throws IOException, Exception {
		Profile profile = new Profile.ProfileBuilder("A").addFraction(new Fraction(Month.JAN, 1f)).build();
		profileRepository.save(profile);

		List<MeterReadingVo> vos = new ArrayList<>();
		int reading = 10;
		for (Month month : Month.values()) {
			MeterReadingVo vo = new MeterReadingVo();
			vo.setMonth(month);
			vo.setConnectionId("0001");
			vo.setProfileName("A");
			vo.setReading(reading);
			vos.add(vo);
			reading += 10;
		}

		int databaseSizeBeforeCreate = meterReadingRepository.findAll().size();

		restMeterReadingMockMVC.perform(post("/api/meterreadings").contentType(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(vos))).andExpect(status().isCreated());

		List<MeterReading> meterReadings = meterReadingRepository.findAll();
		assertThat(meterReadings).hasSize(databaseSizeBeforeCreate + 1);
	}

	@Test
	public void getMeterReading() throws Exception {
		// Initialize the database
		Profile profile = new Profile.ProfileBuilder("A").addFraction(new Fraction(Month.JAN, 1f)).build();
		profileRepository.save(profile);
		
		MeterReading mr = new MeterReading();
		mr.setConnection(new Connection("A", "0001"));
		List<MeterRecord> records = new ArrayList<>();
		int reading = 10;
		for (Month month : Month.values()) {
			MeterRecord vo = new MeterRecord(month, reading);
			records.add(vo);
			reading += 10;
		}
		
		meterReadingRepository.save(mr);

		// Get all the profiles
		restMeterReadingMockMVC.perform(get("/api/meterreadings/A/0001")).andExpect(status().isOk())
				.andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.connection.profileName").value("A"))
				.andExpect(jsonPath("$.connection.connectionId").value("0001"));
	}

}
