package com.ducta.MigrateImageProperties;

import com.ducta.MigrateImageProperties.helper.StringHelper;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@SpringBootApplication
public class MigrateImagePropertiesApplication {

	public static void main(String[] args) throws JSONException, IOException, InterruptedException {

		String token = "<INPUT_TOKEN_HERE>";
		String newsURL ="https://admin.lunglinh.vn/api/news?page=0&size=1000";
		String newsUpdateURL = "https://admin.lunglinh.vn/api/news/";
		String productsURL = "https://admin.lunglinh.vn/api/products?page=0&size=1000";
		String productURL = "https://admin.lunglinh.vn/api/products/";

		CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		RestTemplate restTemplate = new RestTemplate(requestFactory);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
//		updateNewsDetail(headers, restTemplate, newsURL, newsUpdateURL);
		updateProductDetail(headers, restTemplate, productsURL, productURL);

	}
	public static void updateNewsDetail(HttpHeaders headers, RestTemplate restTemplate, String newsURL, String newsUpdateURL) throws JSONException, IOException, InterruptedException {
		HttpEntity<String> request = new HttpEntity<String>(headers);

		ResponseEntity<String> response = restTemplate.exchange(newsURL, HttpMethod.GET, request, String.class);
		JSONObject newsResponse = new JSONObject(response.getBody());

		JSONArray newsList = newsResponse.getJSONArray("content");
		for (int i = 0; i < newsList.length(); i++) {
			JSONObject news = newsList.getJSONObject(i);
			String content = news.getString("content");
			String fixedContent = StringHelper.rewriteHtml(content, restTemplate);
			System.out.println("New content for: " + newsList.getJSONObject(i).getString("title"));
			news.put("content", fixedContent);
			System.out.println(news);
			HttpEntity<String> postRequest = new HttpEntity<String>(news.toString(), headers);
			ResponseEntity<String> updatedResult = restTemplate.exchange(newsUpdateURL + news.getInt("id"), HttpMethod.PUT, postRequest, String.class);
			if (updatedResult.getStatusCodeValue() == 200) {
				System.out.println("Updated news with ID=" + news.getInt("id"));
			} else {
				System.out.println("Error");
			}
			Thread.sleep(500);
		}
	}

	public static void updateProductDetail(HttpHeaders headers, RestTemplate restTemplate, String allProductsURL, String productURL) throws JSONException, IOException, InterruptedException {
		HttpEntity<String> request = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(allProductsURL, HttpMethod.GET, request, String.class);
		System.out.println(response.getBody());
		JSONObject productsResponse = new JSONObject(response.getBody());
		JSONArray productsList = productsResponse.getJSONArray("content");
		System.out.println("Total " + productsList.length() + " products");
		for (int i = 0; i < productsList.length(); i++) {
			int id = productsList.getJSONObject(i).getInt("id");
			System.out.println("Getting product with ID=" + id);
			ResponseEntity<String> productDetailResponse = restTemplate.exchange(productURL +"/id/" + id, HttpMethod.GET, request, String.class);
			if (productDetailResponse.getStatusCodeValue() == 200) {
				JSONObject productDetails = new JSONObject(productDetailResponse.getBody());

				String details = productDetails.getString("details");
				String fixedDetails = StringHelper.rewriteHtml(details, restTemplate);
				productDetails.put("details", fixedDetails);
				System.out.println(productDetails);
				HttpEntity<String> putRequest = new HttpEntity<String>(productDetails.toString(), headers);
				ResponseEntity<String> updatedResult = restTemplate.exchange(productURL + id, HttpMethod.PUT, putRequest, String.class);
				if (updatedResult.getStatusCodeValue() == 200) {
					System.out.println("Updated news with ID=" + id);
				} else {
					System.out.println("Error");
				}
				Thread.sleep(200);
			} else {
				System.out.println("Error getting product with ID=" + id);
			}
		}
	}

}
