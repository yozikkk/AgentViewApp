package com.avaya.aes.agenttest.jsonparser;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonParse {
	
	public JSONObject parse(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		return jsonObject;
	}

}
