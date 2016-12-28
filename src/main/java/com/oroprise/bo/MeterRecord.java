package com.oroprise.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeterRecord {
	private Month month;
	private long reading;
	
	private MeterRecord(){}
}
