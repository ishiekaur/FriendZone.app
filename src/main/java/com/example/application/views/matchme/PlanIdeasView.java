package com.example.application.views.matchme;

import ai.peoplecode.OpenAIConversation;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.util.List;
import java.util.Map;

@PageTitle("Plan Ideas")
@Route(value = "plan-ideas")
public class PlanIdeasView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private OpenAIConversation ideasConversation;
    private H1 header;
    private UnorderedList ideasList;  // Use UnorderedList for displaying ideas

    public PlanIdeasView() {
        header = new H1("Suggested Ideas");
        ideasList = new UnorderedList();  // Initialize the list
//        Button backButton = new Button("Back to Profile");

        // Add a click listener to the back button to return to the profile view
//        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("profile")));

        // Add components to the layout
        getContent().add(header, ideasList);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Fetch the match result passed from ProfileView
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        if (params.containsKey("matchResult") && params.containsKey("user1") && params.containsKey("user2")) {
            String matchResult = params.get("matchResult").get(0);
            String user1 = params.get("user1").get(0);
            String user2 = params.get("user2").get(0);

            // Generate plan ideas with both users' information
            generatePlanIdeas(user1, user2);
        } else {
            ideasList.add(new ListItem("No match result available."));
        }
    }

    private void generatePlanIdeas(String user1Profile, String user2Profile) {
        // Initialize OpenAIConversation with your API key and model
        ideasConversation = new OpenAIConversation(
                "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", // Replace with your actual API key
                "gpt-4o-mini"
        );

        // Construct a prompt for generating plan ideas based on both users' profiles
        String prompt = "Based on the following match results for both users, suggest three places that both users would enjoy visiting in their city. This can be restaurants, coffee shops, bars, activities, etc. Please format it as the location name, then a few bullet points describing what they can do there.\n" +
                "User 1 Profile: " + user1Profile + "\n" +
                "User 2 Profile: " + user2Profile + "\n";

        try {
            // Get plan ideas from OpenAI
            String openAIResponse = ideasConversation.askQuestion("You are an activity suggestion expert.", prompt);
            String[] ideas = openAIResponse.split("\n");

            // Clear any previous items in the list
            ideasList.removeAll();

            // Add each idea as a ListItem to the UnorderedList
            for (String idea : ideas) {
                if (!idea.trim().isEmpty()) {
                    ideasList.add(new ListItem(idea.trim()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ideasList.add(new ListItem("Error occurred while fetching plan ideas."));
        }
    }
}