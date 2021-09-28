package org.openhab.binding.sonytv.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ApiResponse {
    public JsonElement result;
    public JsonArray error;
    public Integer id;
}
