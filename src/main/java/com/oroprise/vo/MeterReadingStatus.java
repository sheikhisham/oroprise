package com.oroprise.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeterReadingStatus {
	private String profileName;
	private String connectionId;
	private String status;
}
