package fr.openwide.core.wicket.gmap.component.map;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

import fr.openwide.core.wicket.gmap.api.directions.CoreDirectionsResult;
import fr.openwide.core.wicket.gmap.api.directions.GDirectionsStatus;
import fr.openwide.core.wicket.gmap.api.directions.GPolyline;
import fr.openwide.core.wicket.gmap.component.GMapHeaderContributor;
import fr.openwide.core.wicket.gmap.js.jquery.plugins.gmap.map.GMapBehavior;
import fr.openwide.core.wicket.gmap.js.jquery.plugins.gmap.map.GMapOptions;

public class GMapPanel extends Panel {
	private static final long serialVersionUID = -904534558476084988L;
	
	private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	static {
		OBJECT_MAPPER.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private AbstractDefaultAjaxBehavior geocoderResultAjax;
	private AbstractDefaultAjaxBehavior directionsResultAjax;
	private AbstractDefaultAjaxBehavior shapeResultAjax;
	
	private CoreDirectionsResult directionsResult;
	private GeocoderResult geocoderResult;
	
	private GMapOptions options;
	
	public GMapPanel(String id, GMapOptions options) {
		this(id, null, null, false, options);
	}
	
	public GMapPanel(String id, Boolean draw, GMapOptions options) {
		this(id, null, null, draw, options);
	}
	
	public GMapPanel(String id, Locale region, GMapOptions options) {
		this(id, region, null, false, options);
	}
	
	public GMapPanel(String id, Locale region, Boolean draw, GMapOptions options) {
		this(id, region, null, draw, options);
	}
	
	public GMapPanel(String id, Locale region, Locale locale, GMapOptions options) {
		this(id, region, locale, false, options);
	}
	
	public GMapPanel(String id, Locale region, Locale locale, Boolean draw, GMapOptions options) {
		super(id);
		setOutputMarkupId(true);
		
		this.options = options;
		
		add(new GMapHeaderContributor(region, locale, draw));
		
		geocoderResultAjax = new AbstractDefaultAjaxBehavior() {
			private static final long serialVersionUID = -7684282452805422195L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				Request request = RequestCycle.get().getRequest();
				String result = request.getPostParameters().getParameterValue("result").toString();
				try {
					geocoderResult = OBJECT_MAPPER.readValue(result, GeocoderResult.class);
				} catch (IOException e) {
					new RuntimeException(e);
				}
			}
		};
		add(geocoderResultAjax);
		
		directionsResultAjax = new AbstractDefaultAjaxBehavior() {
			private static final long serialVersionUID = -7684282452805422195L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				Request request = RequestCycle.get().getRequest();
				String result = request.getPostParameters().getParameterValue("result").toString();
				try {
					directionsResult = OBJECT_MAPPER.readValue(result, CoreDirectionsResult.class);
					if (GDirectionsStatus.OK.getValue().equals(directionsResult.getStatus())) {
						final List<LatLng> points = GPolyline.decodePolyline(directionsResult.getPolyline());
					}
				} catch (IOException e) {
					new RuntimeException(e);
				}
			}
		};
		add(directionsResultAjax);
		
		shapeResultAjax = new AbstractDefaultAjaxBehavior() {
			private static final long serialVersionUID = -7684282452805422195L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				Request request = RequestCycle.get().getRequest();
				String polygons = request.getPostParameters().getParameterValue("polygons").toString();
				String polylines = request.getPostParameters().getParameterValue("polylines").toString();
			}
		};
		add(shapeResultAjax);
		
		// Behavior d'initialisation de la carte
		add(new GMapBehavior(this.options));
	}
	
	public AbstractDefaultAjaxBehavior getDirectionsResultAjax() {
		return directionsResultAjax;
	}
	
	public AbstractDefaultAjaxBehavior getGeocoderResultAjax() {
		return geocoderResultAjax;
	}
	
	public AbstractDefaultAjaxBehavior getShapeResultAjax() {
		return shapeResultAjax;
	}
	
	public CoreDirectionsResult getDirectionsResult() {
		return directionsResult;
	}
	
	public GeocoderResult getGeocoderResult() {
		return geocoderResult;
	}
}