package com.mobilevue.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobilevue.data.MovieDetailsObj;
import com.mobilevue.data.PriceDetailsObj;

public class MovieDetailsEngine {
	public static MovieDetailsObj parseMovieDetails(String json) {
		MovieDetailsObj movieObject = new MovieDetailsObj();
		try {
			JSONObject movieDtls = new JSONObject(json);
			String Replace = "[\\[\\]\"]";
			String ReplaceTo = "";
			movieObject.setId(movieDtls.getInt("mediaId"));
			movieObject.setTitle(movieDtls.getString("title").replaceAll(
					Replace, ReplaceTo));
			movieObject.setRating(movieDtls.getString("rating").replaceAll(
					Replace, ReplaceTo));
			movieObject.setImage(movieDtls.getString("image").replaceAll(
					Replace, ReplaceTo));
			movieObject.setGenres(movieDtls.getString("Genre").replaceAll(
					Replace, ReplaceTo));
			movieObject.setSubject(movieDtls.getString("subject").replaceAll(
					Replace, ReplaceTo));
			movieObject.setActors(movieDtls.getString("Actor").replaceAll(
					Replace, ReplaceTo));
			movieObject.setDirectors(movieDtls.getString("Director")
					.replaceAll(Replace, ReplaceTo));
			movieObject.setWriters(movieDtls.getString("Writer").replaceAll(
					Replace, ReplaceTo));
			movieObject.setDuration(movieDtls.getString("duration").replaceAll(
					Replace, ReplaceTo));
			movieObject.setOverview(movieDtls.getString("overview").replaceAll(
					Replace, ReplaceTo));
			movieObject.setReleaseDate(movieDtls.getString("releaseDate").replaceAll(
					Replace, ReplaceTo));
			// movieObject.setProPromoURL(movieDtls.getString("promoURL").replaceAll(Replace,
			// ReplaceTo));

			JSONArray jsonLocnArr = new JSONArray(
					movieDtls.getString("filmLocations"));
			JSONObject filmLocObj = (JSONObject) jsonLocnArr.get(0);
			movieObject.setLanguage(filmLocObj.getInt("languageId"));
			movieObject.setFormattype(filmLocObj.getString("formatType"));
			movieObject.setLocation(filmLocObj.getString("location"));

			JSONArray jsonPriceDtlsArr = new JSONArray(
					movieDtls.getString("priceDetails"));
			ArrayList<PriceDetailsObj> arrPriceDetailsObj = new ArrayList<PriceDetailsObj>();
			for (int i = 0; i < jsonPriceDtlsArr.length(); i++) {
				JSONObject jsonPriceDtlsObj = (JSONObject) jsonPriceDtlsArr
						.get(i);
				PriceDetailsObj priceDtlsObj = new PriceDetailsObj();
				priceDtlsObj.setId(Integer.parseInt(jsonPriceDtlsObj
						.getString("id")));
				priceDtlsObj.setFormatType(jsonPriceDtlsObj
						.getString("formatType"));
				priceDtlsObj.setOptType(jsonPriceDtlsObj.getString("optType"));
				priceDtlsObj.setPrice(Float.parseFloat(jsonPriceDtlsObj
						.getString("price")));
				arrPriceDetailsObj.add(priceDtlsObj);
			}
			movieObject.setArrPriceDetails(arrPriceDetailsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return movieObject;
	}
}
