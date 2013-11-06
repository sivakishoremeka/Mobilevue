package com.mobilvue.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ser.std.SerializableSerializer;

import android.os.Parcelable;

public class PlansData {
	int id;
	int statusName;
	String planCode;
	String planDescription;
	String[] startDate;
	String[] endDate;
	String status;
	PlanStatusData planstatus;
	String statusname;
	String contractPeriod;

	public String getContractPeriod() {
		return contractPeriod;
	}

	public void setContractPeriod(String contractPeriod) {
		this.contractPeriod = contractPeriod;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStatusName() {
		return statusName;
	}

	public void setStatusName(int statusName) {
		this.statusName = statusName;
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
