# Test signing

`test-signing.p12` is a public test key only. It signs QA and debug APKs so every test build can install over the previous one. Password and alias: `android` / `androiddebugkey`. It must never be used for a store release.
