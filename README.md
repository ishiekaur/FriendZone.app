FriendZone.app

FriendZone is a web application designed to match users based on their location and interests, and provide curated suggestions for meeting places. Built using Java and Vaadin, this project integrates with OpenAI's GPT API for  recommendations and Google Sheets API for profile data management.

The featurs include
Displays user details including name, age, gender, location, and interests.

Match Me:
Matches users based on location and shared interests.
Utilizes OpenAI's GPT API for intelligent matching.

Plan Ideas:
Suggests personalized coffee shops, bars, or restaurants where matched users can meet, powered by GPT.
Google Sheets Integration
Profiles are stored and retrieved dynamically via Google Sheets API.

Most Interesting Part of the Build
The most fascinating part of the project was integrating OpenAI's GPT API to handle intelligent user matching and generate plan ideas. It was exciting to see how AI could be used to interpret user data and generate meaningful, human-like responses that add value to the user experience.

Most Challenging Part of the Build
The biggest challenge was implementing the Google Sheets API. Setting up the credentials, ensuring secure and authenticated access, and dynamically reading and filtering data from the sheet required careful planning and debugging. Ensuring that it seamlessly integrated into the Vaadin-based UI was also tricky but rewarding.

Java Concepts Learned
Through this project, we explored several important Java concepts, including:
API Integration
Learned how to interact with external APIs (OpenAI and Google Sheets).
Used Java to dynamically generate HTML and CSS for the Vaadin frontend.
Dealt with API calls and response handling to keep the UI responsive.

External Code and References

Google Sheets API Integration
The code to set up and use the Google Sheets API was adapted from the official Google Sheets API documentation and tutorials. Below are the key references:

Google Sheets API Java Quickstart
Official setup guide and sample code:
Google Sheets API Java Quickstart

OAuth 2.0 Authentication
Used Google's client libraries to handle OAuth authentication securely.
Library: google-api-client, google-oauth-client

Vaadin Framework
The project uses the Vaadin framework for building the frontend UI. Documentation and examples were referenced from:
Vaadin Official Documentation

We also used Google tutorials, We got the dependencies from the the framework page. To make our code better we also used AI. We also used Stack Overflow and Google Workspace Guides.
