package org.lhfe.rabbitmq.producer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class PopObj implements Serializable {
	private static final long serialVersionUID = -7715207194736514670L;
	private Map<String, String> mp;
	private BigDecimal annualRate;

	public Map<String, String> getMp() {
		return mp;
	}

	public void setMp(Map<String, String> mp) {
		this.mp = mp;
	}

	public BigDecimal getAnnualRate() {
		return annualRate;
	}

	public void setAnnualRate(BigDecimal annualRate) {
		this.annualRate = annualRate;
	}

}
