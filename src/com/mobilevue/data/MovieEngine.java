package com.mobilevue.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieEngine {

	public static GridViewData parseMovieDetails(String json) {
		GridViewData gvDataObj = new GridViewData();

		// ArrayList<MovieObj> movieListArray = new ArrayList<MovieObj>();
		try {
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject movieDtls = (JSONObject) jsonArray.get(i);
				// for (int j = 0; j < 8; j++) {
				if (movieDtls.has("mediaId")) {
					MovieObj movieObject = new MovieObj();
					// movieObject.setTitle(movieDtls.getString("title") + j);
					movieObject.setId(movieDtls.getInt("mediaId"));
					movieObject.setEventId(movieDtls.getInt("eventId"));
					movieObject.setTitle(movieDtls.getString("mediaTitle"));
					movieObject.setRating(movieDtls.getString("mediaRating"));
					movieObject.setImage(movieDtls.getString("mediaImage"));

					/*
					 * movieObject.setGenres(movieDtls.getString("genres"));
					 * movieObject.setSubject(movieDtls.getString("subject"));
					 * movieObject.setActors(movieDtls.getString("actors"));
					 * movieObject
					 * .setDirectors(movieDtls.getString("directors"));
					 * movieObject.setLocation(movieDtls.getString("location"));
					 */
					// movieListArray.add(movieObject);
					gvDataObj.getMovieListObj().add(movieObject);
				} else if (movieDtls.has("noOfPages")) {
					gvDataObj.setPageCount(movieDtls.getInt("noOfPages"));
					gvDataObj.setPageNumber(movieDtls.getInt("pageNo"));
				}
			}

			// }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return gvDataObj;
	}
}
