package com.mobilevue.data;

public class ServiceData {
	int id;
	String serviceCode;
	String planCode;
	String chargeCode;
	String chargeDescription;
	String serviceDescription;

	public String getserviceDescription() {
		return serviceDescription;
	}

	public void setserviceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getserviceCode() {
		return serviceCode;
	}

	public void setserviceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getPlanCode() {
		return planCode;
	}

	public void setPlanCode(String planCode) {
		this.planCode = planCode;
	}

	public String getchargeDescription() {
		return chargeDescription;
	}

	public void setchargeDescription(String chargeDescription) {
		this.chargeDescription = chargeDescription;
	}

	public String getchargeCode() {
		return chargeCode;
	}

	public void setchargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

}
