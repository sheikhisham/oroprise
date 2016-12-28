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

import com.oroprise.bo.Fraction;
import com.oroprise.bo.Month;
import com.oroprise.bo.Profile;
import com.oroprise.repository.ProfileRepository;
import com.oroprise.vo.ProfileVo;
import com.oroprise.web.rest.ProfileResource;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@TestPropertySource(locations="classpath:application-test.properties")
public class ProfileTests {
	
	private MockMvc restProfileMockMvc;
	
	@Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

	@Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
	
	@Autowired
    private ProfileRepository profileRepository;
	
	@PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ProfileResource profileResource = new ProfileResource();
        ReflectionTestUtils.setField(profileResource, "profileRepository", profileRepository);
        this.restProfileMockMvc = MockMvcBuilders.standaloneSetup(profileResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }
	
	@Before
	public void beforeEachTest() {
		profileRepository.deleteAll();
	}

	/**
	 * Covers Profile creation, getAll success scenario
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void profileResourceSuccessTest() throws IOException, Exception {
		profileRepository.deleteAll();
		
		List<ProfileVo> vos = new ArrayList<>();
		ProfileVo vo1 = new ProfileVo();
		vo1.setMonth("DEC");
		vo1.setFraction("0.1");
		vo1.setName("A");
		ProfileVo vo2 = new ProfileVo();
		vo2.setMonth("FEB");
		vo2.setFraction("0.9");
		vo2.setName("A");
		ProfileVo vo3 = new ProfileVo();
		vo3.setMonth("JAN");
		vo3.setFraction("1.0");
		vo3.setName("B");
		
		vos.add(vo1);
		vos.add(vo2);
		vos.add(vo3);
		
		int databaseSizeBeforeCreate = profileRepository.findAll().size();
		
        restProfileMockMvc.perform(post("/api/profiles")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(vos)))
                .andExpect(status().isCreated());
        
        List<Profile> profiles = profileRepository.findAll();
        assertThat(profiles).hasSize(databaseSizeBeforeCreate + 2);
	}
	
	/**
	 * Covers Profile creation failure scenario
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void profileResourceFailureTest() throws IOException, Exception {
		profileRepository.deleteAll();
		
		List<ProfileVo> vos = new ArrayList<>();
		ProfileVo vo1 = new ProfileVo();
		vo1.setMonth("DEC");
		vo1.setFraction("0.1");
		vo1.setName("A");
		
		vos.add(vo1);
		
        restProfileMockMvc.perform(post("/api/profiles")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(vos)))
                .andExpect(status().isBadRequest());
	}
	
	@Test
    public void getProfile() throws Exception {
        // Initialize the database
		Profile profile = new Profile.ProfileBuilder("A").addFraction(new Fraction(Month.JAN, 1f)).build();
		profileRepository.save(profile);

        // Get all the profiles
        restProfileMockMvc.perform(get("/api/profiles/A"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name").value("A"));
    }
}
