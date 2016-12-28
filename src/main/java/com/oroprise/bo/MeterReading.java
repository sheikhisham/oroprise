package com.oroprise.bo;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class MeterReading {
	@Id
	private Connection connection;
	private List<MeterRecord> meterRecords;
}
