package com.example.application.views.matchme;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import ai.peoplecode.OpenAIConversation;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Match me!")
@Menu(icon = "line-awesome/svg/user-friends-solid.svg", order = 0)
@Route("")
public class MatchmeView extends Composite<VerticalLayout> {

    private OpenAIConversation conversation;
    private TextField userName; // For creating a new account
    private TextField existingUserName; // For finding an existing account
    private TextField name;
    private TextField age;
    private RadioButtonGroup<String> gender; // Radio button group for gender
    private RadioButtonGroup<String> location; // Radio button group for location
    private CheckboxGroup<String> interestsCheckboxGroup;

    class MyClickListener implements ComponentEventListener<ClickEvent<Button>> {
        @Override
        public void onComponentEvent(ClickEvent<Button> event) {
            // Validate if all fields are filled
            if (userName.isEmpty() || name.isEmpty() || age.isEmpty() || gender.isEmpty() || location.isEmpty() || interestsCheckboxGroup.getSelectedItems().isEmpty()) {
                // Show a dialog with the error message if any field is empty
                Dialog dialog = new Dialog();
                VerticalLayout layout = new VerticalLayout();
                layout.setSpacing(true);
                layout.setPadding(true);
                layout.add("Please fill out all fields.");
                Button closeButton = new Button("Close", e -> dialog.close());
                layout.add(closeButton);
                dialog.add(layout);
                dialog.setWidth("300px");
                dialog.setHeight("150px");
                dialog.open();
                return;
            }

            // Collect values from input fields
            String usernameValue = userName.getValue();
            String nameValue = name.getValue();
            String ageValue = age.getValue();
            String genderValue = gender.getValue();
            String locationValue = location.getValue();

            // Get selected interests as a comma-separated string
            List<String> selectedInterests = interestsCheckboxGroup.getSelectedItems().stream().toList();
            String interestsValue = String.join(", ", selectedInterests);

            // Check if the username already exists
            try {
                if (GoogleSheetsIntegration.usernameExists(usernameValue)) {
                    userName.setInvalid(true);
                    userName.setErrorMessage("Username already exists.");
                    return;
                }
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                return;
            }

            // Write profile data to Google Sheets
            try {
                GoogleSheetsIntegration.writeProfileToSheet(Arrays.asList(usernameValue, nameValue, ageValue, genderValue, locationValue, interestsValue));
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                return;
            }

            // Navigate to ProfileView and pass the user profile details as query parameters
            Map<String, List<String>> params = new HashMap<>();
            params.put("username", List.of(usernameValue));
            params.put("name", List.of(nameValue));
            params.put("age", List.of(ageValue));
            params.put("gender", List.of(genderValue));
            params.put("location", List.of(locationValue));
            params.put("interests", List.of(interestsValue));

            getUI().ifPresent(ui -> ui.navigate(ProfileView.class, new QueryParameters(params)));
        }
    }

    class FindMeClickListener implements ComponentEventListener<ClickEvent<Button>> {
        @Override
        public void onComponentEvent(ClickEvent<Button> event) {
            String existingUsernameValue = existingUserName.getValue();
            Map<String, List<String>> params = new HashMap<>();
            params.put("username", List.of(existingUsernameValue));
            getUI().ifPresent(ui -> ui.navigate(ProfileView.class, new QueryParameters(params)));
        }
    }

    public MatchmeView() {
        // Initialize the OpenAI conversation
        conversation = new OpenAIConversation(
                "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",
                "gpt-4o-mini"
        );

        userName = new TextField("Choose a username:");
        userName.setRequiredIndicatorVisible(true);

        existingUserName = new TextField("Enter your username to log in:");
        name = new TextField("What's your name?");
        name.setRequiredIndicatorVisible(true);

        age = new TextField("How old are you?");
        age.setRequiredIndicatorVisible(true);

        // Initialize RadioButtonGroup for gender
        gender = new RadioButtonGroup<>();
        gender.setLabel("What is your gender?");
        gender.setItems("Male", "Female", "Non-binary");
        gender.setRequiredIndicatorVisible(true);

        // Initialize RadioButtonGroup for location
        location = new RadioButtonGroup<>();
        location.setLabel("Where are you based?");
        location.setItems("San Francisco", "Los Angeles", "New York City", "Boston", "Chicago", "Austin", "Seattle");
        location.setRequiredIndicatorVisible(true);

        // Initialize CheckboxGroup for interests
        interestsCheckboxGroup = new CheckboxGroup<>();
        interestsCheckboxGroup.setLabel("Select your interests:");
        interestsCheckboxGroup.setItems(
                "Sports", "Music", "Travel", "Technology", "Food", "Math", "Art", "Anime", "Movies", "Fitness", "Reading", "Gaming", "Photography", "Cooking", "Nature", "Fashion", "Politics", "Science", "History", "Animals", "Gardening", "DIY", "Writing", "Bhangra"
        );
        interestsCheckboxGroup.setRequiredIndicatorVisible(true);

        existingUserName.setWidth("300px");
        Div existingUserContainer = new Div();
        existingUserContainer.add(existingUserName, new Button("Find me!", new FindMeClickListener()));

        existingUserContainer.getStyle()
                .set("position", "absolute")
                .set("right", "100px")
                .set("top", "100px")
                .set("border", "1px solid #ccc")
                .set("padding", "5px")
                .set("border-radius", "5px")
                .set("background-color", "#f9f9f9");

        Button askButton = new Button("Create my profile!");
        askButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        askButton.addClickListener(new MyClickListener());

        // Add components to layout
        getContent().add(existingUserContainer, userName, name, age, gender, location, interestsCheckboxGroup, askButton);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
