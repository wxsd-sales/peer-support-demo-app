# Peer Support Demo App
Welcome to our WXSD DEMO Repo! <!-- Keep this here --> 

This is a sample application, which demonstrates how a potential community-based peer-to-peer mental health support platform could be created, using Webex Android SDK. We use the SDK to demonstarte functionalities like creating sessions, let the user join the sessions, get details of the members in the sessions, retrieve details of the user and soon. The target audience for this PoC are health care professionals who want an effortless way to group and meet thier clients. The app has a user-friendly interface, making it easy for users to find the right group to solve their problems.

[![Peer Support Android App Demo](assets/peer_support_main.PNG)](https://www.youtube.com/watch?v=SqZhiC8jHhU&t=10s, "Peer Support App")

## Table of Contents

- [Video Demo](#video-demo)
- [Overview](#overview)
- [Setup](#setup)
- [Functionality](#functionality)
  - [Support Groups](#support-groups)
    - [Info](#info)
    - [Members in a Group](#members-in-a-group)
    - [Join the space](#join-the-space)
  - [Contacts](#contacts)
    - [Create New Session](#create-new-session)
    - [View Join Requests](#view-join-requests)
  - [About Me](#about-me)
  - [Stay Anonymous](#stay-anonymous)
  - [Logout](#logout)
- [Demo](#demo)
- [License](#license)
- [Disclaimer](#disclaimer)
- [Questions](#questions)


## Overview

In this demo application, the user can create new therapy sessions or join an existing session and start messages and meetings in the group using Webex Android SDK. Here, the user can login using OAuth or as a guest user using JWT. We use SDK to create sessions, let the user join the sessions, get details of the members in the sessions, retrieve details of the user and soon. During the meeting the user can define various meeting options.

## Setup

### Prerequisites & Dependencies:

- Mobile Integration with valid client ID and client secret. Please refer [Webex Developer Site](https://developer.webex.com/docs/integrations#registering-your-integration) to see how to register your integration.
- Android Studio 4.0 or above (recommended)
- Webex Android SDK version >3.1.0
- Android SDK Tools 29 or later
- Android API Level 24 or later
- Java JDK 8
- Kotlin - 1.3.+
- Gradle for dependency management

<!-- GETTING STARTED -->

### Installation Steps:
1.  Download or clone this git project and open it in your android studio IDE
2.  Include all these constants in your gradle.properties file
    ```
    CLIENT_ID=""
    CLIENT_SECRET=""
    SCOPE=""
    REDIRECT_URI=""
    ```

3.  Add the following repository to your top-level `build.gradle` file:

        allprojects {
            repositories {
                maven {
                    url 'https://devhub.cisco.com/artifactory/webexsdk/'
                }
            }
        }

4.  Add the `webex-android-sdk` library as a dependency for your app in the `build.gradle` file:

        dependencies {
            implementation 'com.ciscowebex:androidsdk:3.2.0@aar'
        }
        
5.  Run this project on android emulator or on local device

## Functionality

### Support Groups

The first page in the Peer Support Demo app is the support groups page. In this all the support groups that are active/ closed are displayed and you can view the details of the support group.

![Support Groups Page](assets/support_groups_page.jpg)

#### Info

By clicking on the info icon on each support group, you can view the description, owner of the group, session ID, member emails, duration, gender or age preferneces of that group.

![Info](assets/info_peersupport.jpg)

#### Members in a Group

You can view the number of members present in a group on the support group card. To view the mebers details like email address, press on the info icon.

![Member in a Group](assets/members_in_group.PNG)

#### Join the Space

Once you are ready and would like to join a space, you can click on the Join button and you are already not a member it turns to waiting and you have to wait till the owner of the space lets you in. But if you are a member you can directly join the space and start messaging and calling.

![Join the Space](assets/join_the_space.PNG)

### Contacts

This is the second page in the peer support app. In this all the support groups you are a member of is shown. If you are the owner of a specific group then, view join request button is also shown.

#### Create New Session

You can click on the green create new session button on the top to create a space. On clicking you can enter the details like topic name, description, duration, and other peer preferences like gender and age.

![Create New Session](assets/create_new_session_peer_support.jpg)

#### View Join Requests

If you the owner of any support group, you will be able to see this button in this page. You can click on a specific group card to view and allow people into that space.

### About Me

Next page in the peer support app is the about me page. In this page you can view your details like avatar, email ID, last activity and status. You cn also edit your information like gender and age details. This is always optional.

![About Me](assets/about_me_peer_support.jpg)

### Stay Anonymous

If you wish to stay anonymous, while logging in just click on the stay anonymous button and you will be logged in as a guest user. Your details wont be displayed to anyone.

![Stay Anonymous](assets/login.PNG)

### Logout

Once you are done exploring the application please click on the logout button to securely logout.

## Demo

<!-- Insert link to the website below (if deployed). -->
Check out our live demo, available [here](<https://www.youtube.com/watch?v=SqZhiC8jHhU&t=10s>)!

<!-- Keep the following statement -->
*For more demos & PoCs like this, check out our [Webex Labs site](https://collabtoolbox.cisco.com/webex-labs).

## License
<!-- MAKE SURE an MIT license is included in your Repository. If another license is needed, verify with management. This is for legal reasons.--> 

<!-- Keep the following statement -->
All contents are licensed under the MIT license. Please see [license](LICENSE) for details.


## Disclaimer
<!-- Keep the following here -->  
 Everything included is for demo and Proof of Concept purposes only. Use of the site is solely at your own risk. This site may contain links to third party content, which we do not warrant, endorse, or assume liability for. These demos are for Cisco Webex usecases, but are not Official Cisco Webex Branded demos.


## Questions
Please contact the WXSD team at [wxsd@external.cisco.com](mailto:wxsd@external.cisco.com?subject=RepoName) for questions. Or, if you're a Cisco internal employee, reach out to us on the Webex App via our bot (globalexpert@webex.bot). In the "Engagement Type" field, choose the "API/SDK Proof of Concept Integration Development" option to make sure you reach our team. 
