package jsonobject;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class Location{
	private Map<String, String> locationMap;

	public Location(){
		super();
		locationMap = new HashMap<String, String>();
	}

	public Map<String, String> getLocationMap(){
		return locationMap;
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value){
		// System.out.println("handleUnknown in " + Location.class.toString());
		locationMap.put(key, value.toString());
	}

	@Override
	public String toString(){
		String string = "";
		for (Map.Entry<String, String> entry: locationMap.entrySet()){
			string += "" + entry.getKey() + ":" + entry.getValue() + ",";
		}
		return string;
	}
}
