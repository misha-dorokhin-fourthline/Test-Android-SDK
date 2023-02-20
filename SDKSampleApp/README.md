# Android Fourthline Sample app

## **OVERVIEW**
Sample app provides examples on how Fourthline SDK can be integrated into your own app.
Main screen contains 2 group of buttons: core SDK products (Selfie, Document, NFC, KYC ZIP) and ORCA products (KYC, QES, NFC, Document, Selfie) and each launches corresponding process.
In the end of the process the results are presented on separate result screen or, in case of critical error occurs during process, you'll be redirected to main screen with popup explaining failure reason.

## **INTEGRATION**

The Fourthline SDK artefacts are published to the restricted Github Maven repository. It's required to
provide a Github access token with a permission to the repo for Gradle to be able to download artefact 
during the build process.
The github access token is being supplied as a Gradle property with a key `githubToken`. 
There are multiple ways to provide it:
- The preferred way is to add `githubToken=YOUR_ACCESS_TOKEN_HERE` to the local ~/gradle/gradle.properties 
file to keep the token (as sensitive information) out of the source control.
- Update project gradle.properties file with `githubToken=YOUR_ACCESS_TOKEN_HERE`. Make sure you don't push this change to the remote!
- Command line option: `./gradle assemble -PgithubToken=YOUR_ACCESS_TOKEN_HERE`.
