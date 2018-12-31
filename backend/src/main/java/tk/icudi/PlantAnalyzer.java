/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.icudi;

import static javax.measure.unit.SI.KILOGRAM;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.model.RelativisticModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.drew.lang.GeoLocation;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


@Controller
public class PlantAnalyzer {

	private static Throttler throttler = new Throttler();

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Autowired
	private DataSource dataSource;

	@ModelAttribute
	public void setVaryResponseHeader(HttpServletResponse response) {
	    response.setHeader("Access-Control-Allow-Origin", "*");	    
	}    
	
	@RequestMapping("/saveUploadPath")
	ResponseEntity<Object> saveUploadPath(@RequestParam("plantURL") String plantURL) {
		
		System.out.println(" --- plantURL: " + plantURL);
		
		return ResponseEntity.ok().build();
	}
	
	@RequestMapping("/database")
	ResponseEntity<String> database() {

		
		try (Connection connection = dataSource.getConnection()) {
			JSONObject result = readFromDB(connection);
		    
		    final HttpHeaders httpHeaders= new HttpHeaders();
		    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		    return new ResponseEntity<String>(result.toString(), httpHeaders, HttpStatus.OK);
		    

		} catch (Exception e) {
			
			e.printStackTrace();
			
			
			JSONObject error = new JSONObject();
			try {
				error.put("error", e.toString());
			} catch (JSONException e1) {
				throw new RuntimeException("error while generation error message for: " + e, e1);
			}
			
		    final HttpHeaders httpHeaders= new HttpHeaders();
		    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		    return new ResponseEntity<String>(error.toString(), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		    
		}

	}

	@RequestMapping("/databaseMap")
	ResponseEntity<String> databaseMap() {

		
		try (Connection connection = dataSource.getConnection()) {
			JSONObject result = readWithCoordsFromDB(connection);
		    
		    final HttpHeaders httpHeaders= new HttpHeaders();
		    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		    return new ResponseEntity<String>(result.toString(), httpHeaders, HttpStatus.OK);
		    

		} catch (Exception e) {
			
			e.printStackTrace();
			
			
			JSONObject error = new JSONObject();
			try {
				error.put("error", e.toString());
			} catch (JSONException e1) {
				throw new RuntimeException("error while generation error message for: " + e, e1);
			}
			
		    final HttpHeaders httpHeaders= new HttpHeaders();
		    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		    return new ResponseEntity<String>(error.toString(), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		    
		}

	}
	
	private JSONObject readFromDB(Connection connection) throws SQLException, JSONException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM plants ORDER BY uploadTime DESC LIMIT 10");

		JSONArray resultArray = new JSONArray();
		
		while (rs.next()) {
			JSONObject plant = toJson(rs);
			
			resultArray.put(plant);
		}
		
		JSONObject result = new JSONObject();
		result.put("plants", resultArray);
		return result;
	}
	
	private JSONObject readWithCoordsFromDB(Connection connection) throws SQLException, JSONException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM plants ORDER BY uploadTime DESC LIMIT 10");

		JSONArray resultArray = new JSONArray();
		
		while (rs.next()) {
			JSONObject plant = toJsonWithCoords(rs);
			if(plant != null) {
				resultArray.put(plant);
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("plants", resultArray);
		return result;
	}
	
	@RequestMapping("/plantDB")
	String plantDB(Map<String, Object> model) {

		
		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT uploadTime, plantname FROM plants");

			List<String> output = new ArrayList<String>();
			while (rs.next()) {
				output.add("Read from DB: " + rs.getTimestamp("uploadTime") + " / " + rs.getString("plantname"));
			}
			
			System.out.println(" --- DDBBB222: " + output);
			model.put("records", output);
			return "db";

		} catch (Exception e) {
			model.put("message", e.getMessage());
			return "error";
		}

	}
	
	@RequestMapping(value = "/status")
	public ResponseEntity<String> status() {
		
		String responseJson = "{\"status\": \"ok\"}";
	    final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	    return new ResponseEntity<String>(responseJson, httpHeaders, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/analyze")
	public ResponseEntity<String> analyze(@RequestParam("plantURL") String plantURL, @RequestParam("metadata") String metadataRaw, @RequestParam("uuid") String uuid) {
		
		System.out.println(" --- metadataRaw: " + metadataRaw);
				
		String plantURLEnc;
		try {
			plantURLEnc = URLEncoder.encode(plantURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("i can't write UTF-8", e);
		}
		String organ = "flower";
		String analyzeURL = createAnalyseURL(plantURLEnc, organ);
		
		System.out.println(" --- analyzeURL: " + analyzeURL);
		String responseJson = analyzeUrl(analyzeURL);
		
		if(responseJson.contains("organ")){
			responseJson = analyzeUrl(createAnalyseURL(plantURLEnc, "fruit"));
		}
		
		try {
			saveResult(responseJson, plantURL, metadataRaw, uuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	    return new ResponseEntity<String>(responseJson, httpHeaders, HttpStatus.OK);
	}

	private String analyzeUrl(String analyzeURL) {
		return throttler.execute(PlantAnalyzer::callURL, analyzeURL);
	}

	private String createAnalyseURL(String plantURLEnc, String organ) {
		return "http://identify.plantnet-project.org/api/project/weurope/identify?imgs=" + plantURLEnc + "&tags=" + organ +"&json=true&lang=de&app_version=web-1.0.0";
	}

	private void saveResult(String responseJson, String plantURL, String metadataRaw, String uuid) throws JSONException {
		JSONObject analyzation = new JSONObject(responseJson);

		if(analyzation.get("status").equals("error")){
			return;
		}
		
		JSONObject firstResult = analyzation.getJSONArray("results").getJSONObject(0);
		double score = firstResult.getDouble("score");
		if(score < 20){
			System.out.println("score with '"  + score + "' to low for saving");
			return;
		}

		try (Connection connection = dataSource.getConnection()) {
			
			String binomial = firstResult.getString("binomial");
			final String folkname;
			if(firstResult.has("cn")){
				folkname = firstResult.getJSONArray("cn").join(",").replace("\"", "");
			} else {
				folkname = binomial;
			}
			System.out.println("name: " + folkname + "(" + binomial + ")");
			
			
			String matchURL = firstResult.getJSONArray("images").getJSONObject(0).getString("m_url");
			
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS plants (uploadTime timestamp, plantname text, folkname text, planturl text, score text, matchURL text, metadataRaw text, uuid text)");
			stmt.executeUpdate("INSERT INTO plants VALUES (now(), '" + binomial + "', '" + folkname + "', '" + plantURL +"', '" + score + "', '" + matchURL + "', '" + metadataRaw + "', '" + uuid + "')");

			System.out.println(" --- added plant " + folkname + "(" + binomial + ")to database");


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private JSONObject toJsonWithCoords(ResultSet rs) throws JSONException, SQLException {
		JSONObject metadata = new JSONObject(rs.getString("metadataRaw"));

		JSONObject plant = new JSONObject();
		plant.put("plantname", rs.getString("plantname"));
		plant.put("folkname", rs.getString("folkname"));
		plant.put("score", rs.getString("score"));
		plant.put("uuid", rs.getString("uuid"));
		
		plant.put("planturl", rs.getString("planturl"));
		plant.put("matchURL", rs.getString("matchURL"));
		
		JSONObject exifdata = metadata.getJSONObject("exifdata");
		//plant.put("exifdata", exifdata);
		GeoLocation coords = ExifConverter.getCoords(exifdata);
		
		if(coords == null) {
			return null;
		}
		plant.put("lat", coords.getLatitude());
		plant.put("lng", coords.getLongitude());
		plant.put("shotTime", exifdata.optString("DateTime"));
		plant.put("uploadTime", rs.getTimestamp("uploadTime"));
		
		
		return plant;
	}
	
	private JSONObject toJson(ResultSet rs) throws JSONException, SQLException {
		JSONObject metadata = new JSONObject(rs.getString("metadataRaw"));

		JSONObject plant = new JSONObject();
		plant.put("plantname", rs.getString("plantname"));
		plant.put("folkname", rs.getString("folkname"));
		plant.put("score", rs.getString("score"));
		plant.put("uuid", rs.getString("uuid"));
		
		plant.put("planturl", rs.getString("planturl"));
		plant.put("matchURL", rs.getString("matchURL"));
		
		JSONObject exifdata = metadata.getJSONObject("exifdata");
		//plant.put("exifdata", exifdata);
		plant.put("coords", ExifConverter.getCoords(exifdata));
		plant.put("shotTime", exifdata.optString("DateTime"));
		plant.put("uploadTime", rs.getTimestamp("uploadTime"));
		
		
		return plant;
	}
	

	private static String callURL(String urlString) {
		
		try {
			URL url = new URL(urlString);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String strTemp = "";
			
			StringBuilder builder = new StringBuilder();
			while (null != (strTemp = br.readLine())) {
				builder.append(strTemp).append("\n");
			}
			
			return builder.toString();
		} catch (Exception ex) {
			
			throw new RuntimeException("could not connect to'"  + urlString + "'", ex);
		}
	}
	
	
	@PostMapping("/imageUpload")
	String imageUpload(Map<String, Object> model, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {

		System.out.println(" --- file: " + file);
		System.out.println(" --- redirectAttributes: " + redirectAttributes);

		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS images (image bytea NOT NULL)");

			PreparedStatement statement = connection.prepareStatement("INSERT INTO images (image) VALUES (?)");
			statement.setBlob(1, file.getInputStream());
			statement.executeLargeUpdate();

			// ResultSet rs = stmt.executeQuery("SELECT uploadTime FROM uploadTimes");
			//
			// ArrayList<String> output = new ArrayList<String>();
			// while (rs.next()) {
			// output.add("Read from DB: " + rs.getTimestamp("uploadTime"));
			// }
			//
			// model.put("image", output);

			return "imageUpload";
		} catch (Exception e) {
			model.put("message", e.getMessage());
			return "error";
		}

	}

	@RequestMapping("/hello")
	String hello(Map<String, Object> model) {
		RelativisticModel.select();
		String energy = System.getenv().get("ENERGY");
		if (energy == null) {
			energy = "12 GeV";
		}
		Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
		model.put("science", "E=mc^2: " + energy + " = " + m.toString());
		return "hello";
	}

	@RequestMapping("/db")
	String db(Map<String, Object> model) {
		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadTimes (uploadTime timestamp)");
			stmt.executeUpdate("INSERT INTO uploadTimes VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT uploadTime FROM uploadTimes");

			ArrayList<String> output = new ArrayList<String>();
			while (rs.next()) {
				output.add("Read from DB: " + rs.getTimestamp("uploadTime"));
			}

			model.put("records", output);
			return "db";
		} catch (Exception e) {
			model.put("message", e.getMessage());
			return "error";
		}
	}

	@Bean
	public DataSource dataSource() throws SQLException {
		if (dbUrl == null || dbUrl.isEmpty()) {
			return new HikariDataSource();
		} else {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(dbUrl);
			return new HikariDataSource(config);
		}
	}

}
