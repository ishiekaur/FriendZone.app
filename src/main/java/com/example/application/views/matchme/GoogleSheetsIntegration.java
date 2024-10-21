package com.example.application.views.matchme;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GoogleSheetsIntegration {
    private static final String SPREADSHEET_ID = "1YtFlxgorERmVEI0GTVLowWoep0TsWuhMupYzDrXHe8U";  // Your Google Sheet ID

    // Method to write profile data to Google Sheet
    public static void writeProfileToSheet(List<String> profile) throws IOException, GeneralSecurityException {
        Sheets sheetsService = GoogleSheetsService.getSheetsService();

        // Convert List<String> to List<Object>
        List<Object> profileAsObjectList = new ArrayList<>(profile);

        // Create a ValueRange object for appending data to the sheet
        ValueRange body = new ValueRange().setValues(
                List.of(profileAsObjectList)
        );

        // Append data to the sheet
        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "Sheet1!A:Z", body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("Profile successfully written to the sheet.");
    }

    // Method to get a profile by username
    public static List<String> getProfileByUsername(String username) throws IOException, GeneralSecurityException {
        Sheets sheetsService = GoogleSheetsService.getSheetsService();
        String range = "Sheet1!A:Z"; // Adjust the range based on your sheet's structure

        ValueRange result = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = result.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
            return null;
        } else {
            for (List<Object> row : values) {
                // Assuming the username is in the first column (A), adjust index if needed
                if (row.size() > 0 && row.get(0).toString().equalsIgnoreCase(username)) {
                    List<String> profile = new ArrayList<>();
                    for (Object value : row) {
                        profile.add(value.toString());
                    }
                    return profile; // Return the entire row as the profile
                }
            }
        }

        return null; // Return null if no profile is found
    }

    // Method to get all profiles
    public static List<List<String>> getAllProfiles() throws IOException, GeneralSecurityException {
        Sheets sheetsService = GoogleSheetsService.getSheetsService();
        String range = "Sheet1!A:Z"; // Adjust the range based on your sheet's structure

        ValueRange result = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = result.getValues();
        List<List<String>> profiles = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
            return profiles; // Return empty list if no data
        } else {
            for (List<Object> row : values) {
                List<String> profile = new ArrayList<>();
                for (Object value : row) {
                    profile.add(value.toString());
                }
                profiles.add(profile); // Add the entire row as a profile
            }
        }

        return profiles; // Return the list of profiles
    }

    // Method to check if a username already exists in the sheet
    public static boolean usernameExists(String username) throws IOException, GeneralSecurityException {
        Sheets sheetsService = GoogleSheetsService.getSheetsService();
        String range = "Sheet1!A:A"; // Check only the first column for usernames

        ValueRange result = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = result.getValues();

        if (values == null || values.isEmpty()) {
            return false; // No usernames exist
        }

        // Loop through all usernames (first column)
        for (List<Object> row : values) {
            if (row.size() > 0 && row.get(0).toString().equalsIgnoreCase(username)) {
                return true; // Username exists
            }
        }

        return false; // Username not found
    }
}
