package com.oroprise.bo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Connection implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4580492193993528801L;
	private String profileName;
	private String connectionId;
}
