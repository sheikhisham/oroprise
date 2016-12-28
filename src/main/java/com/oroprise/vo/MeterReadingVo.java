/**
 * 
 */
package com.oroprise.vo;

import java.util.Comparator;

import com.oroprise.bo.Month;

import lombok.Data;

/**
 * @author danis
 *
 */
@Data
public class MeterReadingVo {
	private String connectionId;
	private String profileName;
	private Month month;
	private long reading;
	
	public static final Comparator<MeterReadingVo> METER_MONTH_COMPARATOR = new Comparator<MeterReadingVo>() {

		@Override
		public int compare(MeterReadingVo o1, MeterReadingVo o2) {
			return o1.getMonth().compareTo(o2.getMonth());		}
	};
}
