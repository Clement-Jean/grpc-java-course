# gRPC Java

[![build master branch](https://github.com/Clement-Jean/grpc-java-course/actions/workflows/gradle.yml/badge.svg)](https://github.com/Clement-Jean/grpc-java-course/actions/workflows/gradle.yml) ![Coverage](.github/badges/jacoco.svg) [![lint protocol buffers](https://github.com/Clement-Jean/grpc-java-course/actions/workflows/lint.yml/badge.svg)](https://github.com/Clement-Jean/grpc-java-course/actions/workflows/lint.yml) ![Udemy](.github/badges/udemy.svg)

## COUPON: `START_JULY`

## Notes

- The code you see in this repository might be little different from what you saw in the course videos, this is due to the fact that:
  - I'm testing the code
  - I or a student noticed an error/bug/deprecation after recording
  - Dependencies are evolving faster than I can rerecord

  I do the maximum to keep the main features and the code syntax similar by keeping the edits trivial. **However if you
  do get in a situation where you feel lost, leave an `issue` on the repository**
- The coverage shown in the badges section only counts the classes that need testing, such as:
  - greeting/server/GreetingServiceImpl
  - calculator/server/CalculatorServiceImpl
  - blog/server/BlogServiceImpl
  - blog/client/BlogClient

  For more information, please check the `jacocoTestReport` task in `build.gradle`
