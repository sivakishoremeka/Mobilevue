package com.mobilevue.data;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

public class MovieDetailsObj {
	int id;
	String title;
	String type;
	String classType;
	String subject;
	String image;
	String duration;
	String genres;
	String actors;
	String directors;
	String writers;
	String rating;
	int languageId;
	String releaseDate;
	String formattype;
	String location;
	String overview;
	String producers;
	String promoURL;
	
	@JsonProperty("priceDetails")
	ArrayList<PriceDetailsObj> arrPriceDetails;

	public String getPromoURL() {
		return promoURL;
	}

	public void setProPromoURL(String promoURL) {
		this.promoURL = promoURL;
	}

	public String getProducers() {
		return producers;
	}

	public void setProducers(String producers) {
		this.producers = producers;
	}

	public String getOverview() {
		return overview;
	}

	public ArrayList<PriceDetailsObj> getArrPriceDetails() {
		return arrPriceDetails;
	}

	public void setArrPriceDetails(ArrayList<PriceDetailsObj> arrPriceDetails) {
		this.arrPriceDetails = arrPriceDetails;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public int getLanguage() {
		return languageId;
	}

	public void setLanguage(int languageId) {
		this.languageId = languageId;
	}

	public String getFormattype() {
		return formattype;
	}

	public void setFormattype(String formattype) {
		this.formattype = formattype;
	}

	public String getDirectors() {
		return directors;
	}

	public void setDirectors(String directors) {
		this.directors = directors;
	}

	public String getWriters() {
		return writers;
	}

	public void setWriters(String writers) {
		this.writers = writers;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getGenres() {
		return genres;
	}

	public void setGenres(String genres) {
		this.genres = genres;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
