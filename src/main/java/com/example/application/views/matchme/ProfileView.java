package com.example.application.views.matchme;

import ai.peoplecode.OpenAIConversation;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@PageTitle("Match Me")
@Route(value = "profile")
public class ProfileView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private OpenAIConversation matchingConversation;
    private Div profileDetails;
    private H1 profileHeader;
    private Button matchMeButton;
    private Button planIdeasButton;
    private List<String> matchedProfile; // Class member to store matched profile

    public ProfileView() {
        profileHeader = new H1("Your Profile");
        profileDetails = new Div();

        // Initialize buttons
        matchMeButton = new Button("Match me!", event -> matchProfiles());
        planIdeasButton = new Button("Plan Ideas", event -> fetchPlanIdeas());
        planIdeasButton.setVisible(false);  // Hidden initially

        // Add components to the layout
        getContent().add(profileHeader, profileDetails, matchMeButton, planIdeasButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        if (params.containsKey("username")) {
            String username = params.get("username").get(0);
            try {
                List<String> profileData = GoogleSheetsIntegration.getProfileByUsername(username);
                if (profileData != null) {
                    // Build profile details
                    StringBuilder details = new StringBuilder();
                    if (profileData.size() > 0) details.append("Username: ").append(profileData.get(0)).append("<br>");
                    if (profileData.size() > 1) details.append("Name: ").append(profileData.get(1)).append("<br>");
                    if (profileData.size() > 2) details.append("Age: ").append(profileData.get(2)).append("<br>");
                    if (profileData.size() > 3) details.append("Gender: ").append(profileData.get(3)).append("<br>");
                    if (profileData.size() > 4) details.append("Location: ").append(profileData.get(4)).append("<br>");
                    if (profileData.size() > 5 && !profileData.get(5).isEmpty()) {
                        details.append("Interests: ").append(profileData.get(5)).append("<br>");
                    } else {
                        details.append("Interests: Not specified.<br>");
                    }
                    profileDetails.getElement().setProperty("innerHTML", details.toString());
                } else {
                    profileDetails.setText("Profile not found.");
                }
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                profileDetails.setText("Failed to retrieve profile from Google Sheets.");
            }
        } else {
            profileDetails.setText("No profile information provided.");
        }
    }

    private void matchProfiles() {
        matchingConversation = new OpenAIConversation("DEMO", "gpt-4o-mini");

        // Extract username, location, and interests
        String username = profileDetails.getElement().getProperty("innerHTML").split("<br>")[0].replace("Username: ", "");
        String location = profileDetails.getElement().getProperty("innerHTML").split("Location: ")[1].split("<br>")[0];
        String interests = profileDetails.getElement().getProperty("innerHTML").contains("Interests: ") ?
                profileDetails.getElement().getProperty("innerHTML").split("Interests: ")[1].replace("<br>", "") : "N/A";

        List<List<String>> allProfiles;
        try {
            allProfiles = GoogleSheetsIntegration.getAllProfiles();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            profileDetails.setText("Error occurred while fetching profiles from Google Sheets.");
            return;
        }

        // Filter profiles by location (same city)
        StringBuilder prompt = new StringBuilder("Match the following user profile with another user from the same city with similar interests. Only return the matched user's profile as well a short reasoning for why they were matched. Each feature of the user's profile should be on a new line.\n")
                .append("Username: ").append(username).append("\n")
                .append("Interests: ").append(interests).append("\n")
                .append("Location: ").append(location).append("\n\n")
                .append("Available Profiles:\n");

        for (List<String> profile : allProfiles) {
            if (!profile.get(0).equals(username) && profile.size() > 4 && profile.get(4).equals(location)) { // Check if the profile is from the same city
                prompt.append("Profile: Username: ").append(profile.get(0)).append(", ")
                        .append("Name: ").append(profile.get(1)).append(", ")
                        .append("Age: ").append(profile.size() > 2 ? profile.get(2) : "N/A").append(", ")
                        .append("Gender: ").append(profile.size() > 3 ? profile.get(3) : "N/A").append(", ")
                        .append("Location: ").append(profile.get(4)).append(", ")
                        .append("Interests: ").append(profile.size() > 5 ? profile.get(5) : "N/A")
                        .append("\n\n"); // Add a newline for better separation
            }
        }

        String openAIResponse;
        try {
            // Send the prompt to OpenAI and get the response
            openAIResponse = matchingConversation.askQuestion("gpt-4o-mini", prompt.toString());

            System.out.println("OpenAI Response: " + openAIResponse); // Debugging line

            // Display the OpenAI response directly on the frontend
            profileHeader.setText("Match Found!");
            // Improved formatting using <div> tags for readability
            profileDetails.getElement().setProperty("innerHTML",
                    "<div>" + openAIResponse.replace("\n", "<br>") + "</div>"); // Use innerHTML to render HTML content

            // Show Plan Ideas button
            planIdeasButton.setVisible(true);
            // Hide "Match Me" button
            matchMeButton.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
            profileDetails.setText("Error occurred while fetching a match.");
        }
    }

    private void fetchPlanIdeas() {
        // Extract the location of the user
        String location = profileDetails.getElement().getProperty("innerHTML").split("Location: ")[1].split("<br>")[0];

        // Create the OpenAI prompt for generating plan ideas
        String prompt = String.format("Suggest 5 small coffee shops, bars, or restaurants in %s that both users would enjoy visiting together. Please make this short, just include the name of the business and a short one-sentence description.", location);

        try {
            // Send the plan ideas prompt to OpenAI
            String openAIResponse = matchingConversation.askQuestion("gpt-4o-mini", prompt);
            // Improved formatting: each suggestion as a separate bullet point
            String formattedResponse = "<ul>";
            for (String suggestion : openAIResponse.split("\n")) {
                formattedResponse += "<li>" + suggestion + "</li>";
            }
            formattedResponse += "</ul>";

            // Display the response from OpenAI in the profile details
            profileDetails.getElement().setProperty("innerHTML", formattedResponse);

            // Hide Plan Ideas button after displaying ideas
            planIdeasButton.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
            profileDetails.setText("Error occurred while fetching plan ideas.");
        }
    }
}


