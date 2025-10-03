package utils;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class SupabaseClient {
    private static final String SUPABASE_URL = "https://hfujdljobwilnfwpcaea.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhmdWpkbGpvYndpbG5md3BjYWVhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTg4NTc5ODYsImV4cCI6MjA3NDQzMzk4Nn0.LEpFZBWsnKHR5lZ0l7yPuWUodLhhUpZlthQVqi_eFBk";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public SupabaseClient() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String insert(String table, String jsonData) throws IOException {
        RequestBody body = RequestBody.create(jsonData, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String select(String table, String filter) throws IOException {
        String url = SUPABASE_URL + "/rest/v1/" + table;
        if (filter != null && !filter.isEmpty()) {
            url += "?" + filter;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}