package com.mobilvue.data;

public class MovieObj {
	private int id;
	private int eventId;
	private String title;
	// String type;
	// String classType;
	// String subject;
	private String image;
	// String location;
	// int duration;
	// String genres;
	// String actors;
	// String directors;
	private String rating;

	// use json object

	/*
	 * public String getDirectors() { return directors; } public void
	 * setDirectors(String directors) { this.directors = directors; }
	 */
	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	/*
	 * public String getActors() { return actors; } public void setActors(String
	 * actors) { this.actors = actors; } public String getGenres() { return
	 * genres; } public void setGenres(String genres) { this.genres = genres; }
	 */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id + "::" + title + "::" + rating;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

}
