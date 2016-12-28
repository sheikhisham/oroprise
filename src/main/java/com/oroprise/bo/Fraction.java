package com.oroprise.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = { "month" })
public class Fraction implements Comparable <Fraction> {
	private Month month;
	private double value;
	
	@Override
	public int compareTo(Fraction o) {
		int a = this.month.getSortValue();
		int b = o.month.getSortValue();
		return a > b ? +1 : a < b ? -1 : 0;
	}
}
