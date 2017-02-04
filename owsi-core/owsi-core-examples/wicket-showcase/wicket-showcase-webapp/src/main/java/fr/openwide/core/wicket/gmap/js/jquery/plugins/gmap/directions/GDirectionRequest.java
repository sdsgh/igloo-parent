package fr.openwide.core.wicket.gmap.js.jquery.plugins.gmap.directions;

import java.io.Serializable;
import java.util.List;

import org.odlabs.wiquery.core.javascript.ChainableStatement;
import org.odlabs.wiquery.core.javascript.JsUtils;
import org.odlabs.wiquery.core.options.Options;

import com.google.code.geocoder.model.LatLng;

import fr.openwide.core.wicket.gmap.api.directions.GDirectionsWayPoint;
import fr.openwide.core.wicket.gmap.api.directions.GTravelMode;
import fr.openwide.core.wicket.gmap.api.directions.GUnitSystem;
import fr.openwide.core.wicket.gmap.api.utils.GJsStatementUtils;

/*
 * see <a href="http://code.google.com/intl/fr-FR/apis/maps/documentation/javascript/directions.html#DirectionsRequests">
 * </a>
 */
public class GDirectionRequest implements ChainableStatement, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String callback;
	private String callbackUrl;
	
	// origin is required
	private LatLng originLatLng;
	private LatLng destinationLatLng;
	
	// destination is required
	private String originString;
	private String destinationString;
	
	List<GDirectionsWayPoint> waypoints; //optional
	
	private GTravelMode travelMode; //required
	
	private GUnitSystem unitSystem; //optional
	
	private Boolean optimizeWaypoints; //optional
	
	private Boolean provideRouteAlternatives; //optional
	
	private Boolean avoidHighways; //optional
	
	private Boolean avoidTolls; //optional
	
	private String region; //optional
	
	public GDirectionRequest(String origin, String destination, GTravelMode travelMode, String callback, String callbackUrl) {
		this.originString = origin;
		this.destinationString = destination;
		this.travelMode = travelMode;
		this.callback = callback;
		this.callbackUrl = callbackUrl;
		
		this.originLatLng = null;
		this.destinationLatLng = null;
	}
	
	public GDirectionRequest(LatLng origin, LatLng destination, GTravelMode travelMode, String callback, String callbackUrl) {
		this.originLatLng = origin;
		this.destinationLatLng = destination;
		this.travelMode = travelMode;
		this.callback = callback;
		this.callbackUrl = callbackUrl;
		
		this.originString = null;
		this.destinationString = null;
	}

	public GDirectionRequest(String origin, LatLng destination, GTravelMode travelMode, String callback, String callbackUrl) {
		this.originString = origin;
		this.destinationLatLng = destination;
		this.travelMode = travelMode;
		this.callback = callback;
		this.callbackUrl = callbackUrl;
		
		this.originLatLng = null;
		this.destinationString = null;
	}
	
	public GDirectionRequest(LatLng origin, String destination, GTravelMode travelMode, String callback, String callbackUrl) {
		this.originLatLng = origin;
		this.destinationString = destination;
		this.travelMode = travelMode;
		this.callback = callback;
		this.callbackUrl = callbackUrl;
		
		this.originString = null;
		this.destinationLatLng = null;
	}
	
	public LatLng getOriginLatLng() {
		return originLatLng;
	}

	@Override
	public String chainLabel() {
		return "gmap";
	}

	@Override
	public CharSequence[] statementArgs() {
		if (!isValid()) {
			throw new IllegalArgumentException("A direction request must be initialized with an origin and a destination" +
					"a travel mode, a callback function and a callback url");
		}
		
		Options options = new Options();
		if (originLatLng == null) {
			options.put("origin", JsUtils.quotes(originString));
		} else {
			options.put("origin", GJsStatementUtils.getJavaScriptStatement(originLatLng));
			
		}
		if (destinationLatLng == null) {
			options.put("destination", JsUtils.quotes(destinationString));
		} else {
			options.put("destination", GJsStatementUtils.getJavaScriptStatement(destinationLatLng));
		}
		
		options.put("travelMode", GJsStatementUtils.getJavaScriptStatement(travelMode));
		
		if (waypoints != null) {
			options.put("waypoints", GJsStatementUtils.getJavaScriptStatement(waypoints));
		}
		if (unitSystem != null) {
			options.put("unitSystem", GJsStatementUtils.getJavaScriptStatement(unitSystem));
		}
		if (optimizeWaypoints != null) {
			options.put("optimizeWaypoints", optimizeWaypoints);
		}
		if (provideRouteAlternatives != null) {
			options.put("provideRouteAlternatives", provideRouteAlternatives);
		}
		if (avoidHighways != null) {
			options.put("avoidHighways", avoidHighways);
		}
		if (avoidTolls != null) {
			options.put("avoidTolls", avoidTolls);
		}
		if (region != null) {
			options.put("region", JsUtils.quotes(region));
		}
		
		CharSequence[] args = new CharSequence[4];
		args[0] = JsUtils.quotes("route");
		args[1] = options.getJavaScriptOptions();
		args[2] = JsUtils.quotes(callback);
		args[3] = JsUtils.quotes(callbackUrl);
		return args;
	}
	
	private Boolean isValid() {
		if ( (originLatLng != null || originString != null) && (destinationLatLng != null || destinationString != null)
				&& travelMode != null && callback != null && callbackUrl != null) {
			return true;
		} else {
			return false;
		}
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public LatLng getDestinationLatLng() {
		return destinationLatLng;
	}

	public String getOriginString() {
		return originString;
	}

	public String getDestinationString() {
		return destinationString;
	}

	public List<GDirectionsWayPoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<GDirectionsWayPoint> waypoints) {
		this.waypoints = waypoints;
	}

	public GTravelMode getTravelMode() {
		return travelMode;
	}

	public void setTravelMode(GTravelMode travelMode) {
		this.travelMode = travelMode;
	}

	public GUnitSystem getUnitSystem() {
		return unitSystem;
	}

	public void setUnitSystem(GUnitSystem unitSystem) {
		this.unitSystem = unitSystem;
	}

	public Boolean getOptimizeWaypoints() {
		return optimizeWaypoints;
	}

	public void setOptimizeWaypoints(Boolean optimizeWaypoints) {
		this.optimizeWaypoints = optimizeWaypoints;
	}

	public Boolean getProvideRouteAlternatives() {
		return provideRouteAlternatives;
	}

	public void setProvideRouteAlternatives(Boolean provideRouteAlternatives) {
		this.provideRouteAlternatives = provideRouteAlternatives;
	}

	public Boolean getAvoidHighways() {
		return avoidHighways;
	}

	public void setAvoidHighways(Boolean avoidHighways) {
		this.avoidHighways = avoidHighways;
	}

	public Boolean getAvoidTolls() {
		return avoidTolls;
	}

	public void setAvoidTolls(Boolean avoidTolls) {
		this.avoidTolls = avoidTolls;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
}