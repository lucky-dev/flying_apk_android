# FlyingApk (Android client)

This is a client for [the server FlyingApk](https://github.com/lucky-dev/flying_apk_sinatra). You can bind this client to your server:

1. Open file `<project_directory>/app/build.gradle`
2. Find this line `buildConfigField "String", "ENDPOINT_URL", "\"127.0.0.1:8080\""`
3. In sections `release` or `debug` change value `127.0.0.1:8080` to other value.
4. Go to the root directory of this project.
5. Assemble the project ([more details about Gradle](http://tools.android.com/tech-docs/new-build-system/user-guide)):
    * Debug: `./gradlew clean assembleDebug`
    * Release: `./gradlew clean assembleRelease`
