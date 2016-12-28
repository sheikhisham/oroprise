package com.oroprise.web.rest;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.oroprise.bo.Fraction;
import com.oroprise.bo.Month;
import com.oroprise.bo.Profile;
import com.oroprise.repository.ProfileRepository;
import com.oroprise.util.HeaderUtil;
import com.oroprise.vo.ProfileVo;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class ProfileResource {

	@Autowired
	private ProfileRepository profileRepository;

	/**
     * POST  /profiles : Create new profiles.
     *
     * @param profileVos list of profileVos to create
     * @return the ResponseEntity with status 201 (Created) and with body the new profiles created, or with status 400 (Bad Request) if the profile validation failed
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
	@RequestMapping(value = "/profiles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Profile>> createResources(@RequestBody List<ProfileVo> profileVos)
			throws URISyntaxException {
		log.debug("Enter createResource");

		// group the profileVos as a map [profileName as key, list of Fraction(month,
		// fractionvalue) as value]
		Map<String, Set<Fraction>> groupedVos = profileVos.stream().collect(groupingBy(ProfileVo::getName, mapping(vo -> new Fraction(Month.valueOf(vo.getMonth()), Double.parseDouble(vo.getFraction())), Collectors.toCollection(HashSet::new))));

		List<Profile> profileListToSave = new ArrayList<>();
		for (String name : groupedVos.keySet()) {
			Set<Fraction> fractions = groupedVos.get(name);

			// build profile for each of the profileVo using ProfileBuilder
			Profile.ProfileBuilder builder = new Profile.ProfileBuilder(name);
			fractions.forEach(builder::addFraction);

			try {
				Profile profile = builder.build();
				profileListToSave.add(profile);
			} catch (Exception e) {
				log.error("Error occurred while creating profile : " + e.getMessage());
				return ResponseEntity.badRequest().headers(
						HeaderUtil.createFailureAlert("profile", e.getMessage()))
						.body(null);
			}
		}

		List<Profile> result = null;

		// save generated profiles
		result = profileRepository.save(profileListToSave);

		log.debug("Exit createResource");

		return ResponseEntity.created(new URI("/api/profiles/")).body(result);

	}
	
	/**
     * GET  /profiles : get all the profiles.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of profiles in body
     */
    @RequestMapping(value = "/profiles",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Profile> getAllProfiles() {
        log.debug("REST request to get all profiles");
        List<Profile> profiles = profileRepository.findAll();
        return profiles;
    }
    
    /**
     * GET  /profiles/:id : get the "name" profile.
     *
     * @param name the id of the profile to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the profile, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/profiles/{name}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profile> getProfile(@PathVariable String name) {
        log.debug("REST request to get profile : {}", name);
        Profile profile = profileRepository.findOne(name);
        return Optional.ofNullable(profile)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * DELETE  /profiles/:name : delete the "name" profile.
     *
     * @param name the name of the profile to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/profiles/{name}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteProfile(@PathVariable String name) {
        log.debug("REST request to delete Profile : {}", name);
        profileRepository.delete(name);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("profile", name)).build();
    }
}
