package com.einzig.ipst2.Objects;

import java.util.Date;

public class PortalSubmission {

	public String name;
	public Date dateSubmitted;
	public Date dateAccepted;
	public Date dateRejected;
	public String status;
	public String accountAcceptedEmail;
	public String rejectionReason;
	public String liveAddress;
	public String intelLinkURL;
	public String pictureURL;

	public String getPictureURL() {
		return pictureURL;
	}
	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
	}
	public String getLiveAddress() {
		return liveAddress;
	}
	public void setLiveAddress(String liveAddress) {
		this.liveAddress = liveAddress;
	}
	public String getIntelLinkURL() {
		return intelLinkURL;
	}
	public void setIntelLinkURL(String intelLinkURL) {
		this.intelLinkURL = intelLinkURL;
	}
	public String getRejectionReason() {
		return rejectionReason;
	}
	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
	public String getAccountAcceptedEmail() {
		return accountAcceptedEmail;
	}
	public void setAccountAcceptedEmail(String accountAcceptedEmail) {
		this.accountAcceptedEmail = accountAcceptedEmail;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDateSubmitted() {
		return dateSubmitted;
	}
	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}
	public Date getDateAccepted() {
		return dateAccepted;
	}
	public void setDateAccepted(Date dateAccepted) {
		this.dateAccepted = dateAccepted;
	}
	public Date getDateRejected() {
		return dateRejected;
	}
	public void setDateRejected(Date dateRejected) {
		this.dateRejected = dateRejected;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
