package com.example.application.views.matchme;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleSheetsService {
    private static final String APPLICATION_NAME = "FriendZone";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";  // Path to service account key

    public static Sheets getSheetsService() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new FileInputStream(CREDENTIALS_FILE_PATH))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

