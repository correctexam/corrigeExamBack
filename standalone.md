# Standalone version

This documentation could be used to build and deploy a local version of CorrectExam. It could be used to correct exam offline and export the results, import them to correctexam.github.io to share results to students. 

## build 

```bash
git clone https://github.com/correctexam/corrigeExamFront
cd corrigeExamFront
npm i
npm run webapp:build:prodlocal
cd ..
git clone https://github.com/correctexam/corrigeExamBack
cd corrigeExamBack
cp -r ../corrigeExamFront/target/classes/static/* src/main/resources/META-INF/resources/
./mvnw package  -Pnative,alone -DskipTests
```

in target you get a self content binary *correctexam-1.0.0-SNAPSHOT-runner* that could be used in any laptop to test correctexam offlile. 

