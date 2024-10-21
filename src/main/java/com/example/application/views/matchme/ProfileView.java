package com.example.application.views.matchme;

import ai.peoplecode.OpenAIConversation;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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
    private Button newMatchButton; // New button for getting a new match
    private List<String> matchedProfile; // Class member to store matched profile

    public ProfileView() {
        profileHeader = new H1("Your Profile");
        profileDetails = new Div();

        // Initialize "Match Me" button
        matchMeButton = new Button("Match me!", event -> matchProfiles());

        // Initialize "Plan Ideas" button, initially hidden
        planIdeasButton = new Button("Plan Ideas", event -> fetchPlanIdeas());
        planIdeasButton.setVisible(false);  // Hidden initially

        // Initialize "New Match" button, initially hidden
        newMatchButton = new Button("Find a New Match", event -> matchProfiles());
        newMatchButton.setVisible(false); // Hidden initially

        // Add components to the layout
        getContent().add(profileHeader, profileDetails, matchMeButton, planIdeasButton, newMatchButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        if (params.containsKey("username")) {
            String username = params.get("username").get(0);
            try {
                List<String> profileData = GoogleSheetsIntegration.getProfileByUsername(username);
                if (profileData != null) {
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
                    // Set the profile details with HTML
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
        matchingConversation = new OpenAIConversation(
                "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", // Replace with your actual API key
                "gpt-4o-mini"
        );

        String username = profileDetails.getElement().getProperty("innerHTML").split("<br>")[0].replace("Username: ", "");
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

        // Create the prompt with all profiles for matching
        StringBuilder prompt = new StringBuilder("Match the following user profile with another user with similar interests:\n")
                .append("Username: ").append(username).append("\n")
                .append("Interests: ").append(interests).append("\n\n")
                .append("Available Profiles:\n");

        for (List<String> profile : allProfiles) {
            if (!profile.get(0).equals(username)) { // Exclude the current user from the list
                prompt.append("Profile: Username: ").append(profile.get(0)).append(", ")
                        .append("Name: ").append(profile.get(1)).append(", ")
                        .append("Interests: ").append(profile.size() > 5 ? profile.get(5) : "N/A")
                        .append("\n");
            }
        }

        String matchedUsername = null;
        String openAIResponse;
        try {
            // Send the prompt to OpenAI and parse the response
            openAIResponse = matchingConversation.askQuestion(
                    "Find the best match for the given user based on their interests. Please make sure you match everyone with someone that is from the same city as them.", prompt.toString());

            System.out.println("OpenAI Response: " + openAIResponse); // Debugging line

            // Check if the response contains a valid match
            if (openAIResponse.contains("Username:")) {
                matchedUsername = openAIResponse.split("Username:")[1].trim().split(",")[0];
            }

        } catch (Exception e) {
            e.printStackTrace();
            profileDetails.setText("Error occurred while fetching a match.");
            return;
        }

        if (matchedUsername == null) {
            profileDetails.setText("No match found.");
            newMatchButton.setVisible(true); // Show the new match button if no match is found
            return;
        }

        // Now search the matched profile from the list
        matchedProfile = null; // Reset matchedProfile before fetching
        for (List<String> profile : allProfiles) {
            if (profile.get(0).equals(matchedUsername)) {
                matchedProfile = profile; // Assign the matched profile
                break;
            }
        }

        // Update the UI with the matched user's full profile
        if (matchedProfile != null) {
            StringBuilder matchedProfileDetails = new StringBuilder();
            if (matchedProfile.size() > 0) matchedProfileDetails.append("Username: ").append(matchedProfile.get(0)).append("<br>");
            if (matchedProfile.size() > 1) matchedProfileDetails.append("Name: ").append(matchedProfile.get(1)).append("<br>");
            if (matchedProfile.size() > 2) matchedProfileDetails.append("Age: ").append(matchedProfile.get(2)).append("<br>");
            if (matchedProfile.size() > 3) matchedProfileDetails.append("Gender: ").append(matchedProfile.get(3)).append("<br>");
            if (matchedProfile.size() > 4) matchedProfileDetails.append("Location: ").append(matchedProfile.get(4)).append("<br>");
            if (matchedProfile.size() > 5 && !matchedProfile.get(5).isEmpty()) {
                matchedProfileDetails.append("Interests: ").append(matchedProfile.get(5)).append("<br>");
            } else {
                matchedProfileDetails.append("Interests: Not specified.<br>");
            }

            profileHeader.setText("We think you'd get along with...");
            profileDetails.getElement().setProperty("innerHTML", matchedProfileDetails.toString()); // Use innerHTML to render HTML content
        } else {
            profileDetails.setText("Matched profile not found.");
        }

        // Hide "Match Me" button and show "Plan Ideas" and "New Match" buttons
        matchMeButton.setVisible(false);
        planIdeasButton.setVisible(true);
        newMatchButton.setVisible(true); // Show the new match button when a match is found
    }

    private void fetchPlanIdeas() {
        // Assuming you have both user profiles stored or accessible
        String user1Profile = profileDetails.getElement().getProperty("innerHTML"); // Current user profile

        // Check if matchedProfile is set before using it
        String user2Profile = matchedProfile != null ?
                "Username: " + matchedProfile.get(0) + "<br>Name: " + matchedProfile.get(1) + "<br>Age: " + matchedProfile.get(2) + "<br>Gender: " + matchedProfile.get(3) + "<br>Location: " + matchedProfile.get(4) + "<br>Interests: " + (matchedProfile.size() > 5 ? matchedProfile.get(5) : "N/A") + "<br>"
                : "Matched profile not found."; // Use stored matched profile details

        // Combine both profiles for matchResult
        String matchResult = user1Profile + "\n\n" + user2Profile; // Combine both profiles
        getUI().ifPresent(ui -> ui.navigate("plan-ideas?matchResult=" + matchResult + "&user1=" + user1Profile + "&user2=" + user2Profile));
    }
}
