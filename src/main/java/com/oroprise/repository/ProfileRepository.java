/**
 * 
 */
package com.oroprise.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.oroprise.bo.Profile;

/**
 * @author danis
 *
 */
//@Repository
public interface ProfileRepository extends MongoRepository<Profile, String> {

}
