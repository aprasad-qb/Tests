package com.asp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

public class JsonPTest {
    public static void main(String[] args) throws IOException {
        System.out.println(String.format("JSON: %s", JsonPath.read(JsonPTest.class.getResourceAsStream("in.json"), "$[0].ns2:ExportBCKewill")));

        List<List<Map<String, String>>> parties1 = (List<List<Map<String, String>>>) JsonPath.read(JsonPTest.class.getResourceAsStream("in.json"), "$..Transaction.PartiesWithAccess.PartyIdentifier");
        for (Map<String, String> party: parties1.get(0)) {
            System.out.println(String.format("PartiesWithAccess1: %s", party.get("_")));
        }

        List<List<Object>> parties2 = (List<List<Object>>) JsonPath.read(JsonPTest.class.getResourceAsStream("in.json"), "$..PartyIdentifier");
        for (Object party: parties2.get(0)) {
            JSONObject j = (JSONObject) party;
            System.out.println(String.format("PartiesWithAccess2: %s", j.keySet()));
        }
    }
}
