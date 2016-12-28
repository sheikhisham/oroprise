/**
 * 
 */
package com.oroprise.bo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;

import lombok.Getter;

/**
 * @author danis
 *
 */
public class Profile {
	@Getter
	@Id
	private String name;
	@Getter
	private Set<Fraction> fractions;
	
	private final static List<Fraction> DUMMYFRACTIONS = Arrays.asList(Month.values()).stream().map(m->new Fraction(m, 0f)).collect(Collectors.toList());
	
	private Profile() {
	}

	public static class ProfileBuilder {
		private double totalFractionValue;
		private String name;
		private TreeSet<Fraction> fractions;

		public ProfileBuilder(String name) {
			this.name = name;
			this.fractions = new TreeSet<>();
		}

		/**
		 * Adds fraction to Profilebuilder
		 * 
		 * @param fraction
		 * @return
		 */
		public ProfileBuilder addFraction(Fraction fraction) {
			if (fraction == null) {
				throw new IllegalArgumentException("Fraction cannot be null");
			}

			// adds fraction only if the same month is not added already
			if (fractions.add(fraction)) {
				totalFractionValue += fraction.getValue();
			}
			
			return this;
		}

		/**
		 * Builds profile only if the totalfractionvalue is 1
		 * 
		 * @return
		 */
		public Profile build() {
			if (!new DecimalFormat("#").format(totalFractionValue).equals("1")) {
				throw new IllegalArgumentException(
						String.format("Total Fraction Value for %s : %d, but should be 1", name, totalFractionValue));
			}
			
			// add missing months
			fractions.addAll(DUMMYFRACTIONS);

			Profile profile = new Profile();
			profile.name = name;
			profile.fractions = fractions;

			return profile;
		}
	}
}
