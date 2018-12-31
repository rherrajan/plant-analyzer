package tk.icudi;

import java.util.Arrays;

import org.json.JSONObject;

import com.drew.lang.GeoLocation;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;

public class ExifConverter {

	public static GeoLocation getCoords(JSONObject exifdata) {

		String latitudeRef = exifdata.optString("GPSLatitudeRef");
		if(latitudeRef == null || latitudeRef.isEmpty()) {
			return null;
		}
		Double[] latitudes = toNumberArray(exifdata.optString("GPSLatitude"));

		String longitudeRef = exifdata.optString("GPSLongitudeRef");
		Double[] longitudes = toNumberArray(exifdata.optString("GPSLongitude"));

		// GPSLongitude
		Double lat = degreesMinutesSecondsToDecimal(latitudes[0], latitudes[1], latitudes[2],
				latitudeRef.equalsIgnoreCase("S"));
		Double lon = degreesMinutesSecondsToDecimal(longitudes[0], longitudes[1], longitudes[2],
				longitudeRef.equalsIgnoreCase("W"));

		// This can return null, in cases where the conversion was not possible
//        if (lat == null || lon == null)
//            return null;
//		} else {
		return new GeoLocation(lat, lon);
//		}
	}

	/**
	 * Converts DMS (degrees-minutes-seconds) rational values, as given in
	 * {@link com.drew.metadata.exif.GpsDirectory}, into a single value in degrees,
	 * as a double.
	 */
	@Nullable
	public static Double degreesMinutesSecondsToDecimal(@NotNull final double degs, @NotNull final double mins,
			@NotNull final double secs, final boolean isNegative) {
		double decimal = Math.abs(degs) + mins / 60.0d + secs / 3600.0d;

		if (Double.isNaN(decimal))
			return null;

		if (isNegative) {
			decimal *= -1;
		}
		return decimal;
	}

	private static Double[] toNumberArray(String gpsString) {
		//System.out.println(" --- gpsString: " + gpsString);
		String[] gpsArray = gpsString.replace("[", "").replace("]","").split(",");
		return Arrays.stream(gpsArray).
				map(chars -> Double.valueOf(chars))
				.toArray(Double[]::new);
	}

}
