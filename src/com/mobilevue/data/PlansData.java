package com.mobilevue.data;

import java.util.ArrayList;

public class PlansData {
	String id;
	String planCode;
	String planDescription;
	String[] startDate;
	String[] endDate;
	String status;
	PlanStatusData planstatus;
	String statusname;
	String contractPeriod;
	String contractId;
	ArrayList<ServiceData> services;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusname() {
		return statusname;
	}

	public void setStatusname(String statusname) {
		this.statusname = statusname;
	}

	public ArrayList<ServiceData> getServiceData() {
		return services;
	}

	public void setServiceData(ArrayList<ServiceData> services) {
		this.services = services;
	}

	public String getContractPeriod() {
		return contractPeriod;
	}

	public void setContractPeriod(String contractPeriod) {
		this.contractPeriod = contractPeriod;
	}
	
	public String getContractId() {
		return contractId;
	}

	public void setcontractId(String contractId) {
		this.contractId = contractId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlanCode() {
		return planCode;
	}

	public void setPlanCode(String planCode) {
		this.planCode = planCode;
	}

	public String getPlanDescription() {
		return planDescription;
	}

	public void setPlanDescription(String planDescription) {
		this.planDescription = planDescription;
	}

	public String[] getStartDate() {
		return startDate;
	}

	public void setStartDate(String[] startDate) {
		this.startDate = startDate;
	}

	public String[] getEndDate() {
		return endDate;
	}

	public void setEndDate(String[] endDate) {
		this.endDate = endDate;
	}

	public PlanStatusData getPlanstatus() {
		return planstatus;
	}

	public void setPlanstatus(PlanStatusData planstatus) {
		this.planstatus = planstatus;
	}

}
