package com.asp.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JacksonTest {

    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        Person person = mapper.readValue("{\"name\": \"Name\", \"verified\": true, \"data\": {\"some\": \"thing\"}}", Person.class);

        System.out.println(person);
    }

}
