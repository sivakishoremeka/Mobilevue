package com.mobilevue.data;

public class OrdersData {

	String planCode;
	String dateFormat;
	String locale;
	String start_date;
	String contractPeriod;
	boolean billAlign;
	String paytermCode;

	public String getPlanCode() {
		return planCode;
	}

	public void setPlanCode(String planCode) {
		this.planCode = planCode;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getContractPeriod() {
		return contractPeriod;
	}

	public void setContractPeriod(String contractPeriod) {
		this.contractPeriod = contractPeriod;
	}

	public boolean isBillAlign() {
		return billAlign;
	}

	public void setBillAlign(boolean billAlign) {
		this.billAlign = billAlign;
	}

	public String getPaytermCode() {
		return paytermCode;
	}

	public void setPaytermCode(String paytermCode) {
		this.paytermCode = paytermCode;
	}

}
