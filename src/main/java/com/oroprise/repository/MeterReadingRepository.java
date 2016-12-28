/**
 * 
 */
package com.oroprise.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oroprise.bo.MeterReading;
import com.oroprise.bo.Connection;

/**
 * @author danis
 *
 */
public interface MeterReadingRepository extends MongoRepository<MeterReading, Connection> {

}
