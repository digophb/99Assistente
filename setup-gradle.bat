@echo off
echo Criando Gradle Wrapper...

if not exist gradle\wrapper mkdir gradle\wrapper

echo Baixando gradle-wrapper.jar...
curl -L -o gradle\wrapper\gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar

echo Criando gradle-wrapper.properties...
(
echo distributionBase=GRADLE_USER_HOME
echo distributionPath=wrapper/dists
echo distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
echo networkTimeout=10000
echo validateDistributionUrl=true
echo zipStoreBase=GRADLE_USER_HOME
echo zipStorePath=wrapper/dists
) > gradle\wrapper\gradle-wrapper.properties

echo Criando gradlew.bat...
(
echo @echo off
echo setlocal
echo.
echo set DIRNAME=%%~dp0
echo if "%%DIRNAME%%" == "" set DIRNAME=.
echo set APP_BASE_NAME=%%~n0
echo set APP_HOME=%%DIRNAME%%
echo.
echo set DEFAULT_JVM_OPTS=
echo.
echo set JAVA_EXE=java.exe
echo if not "%%JAVA_HOME%%" == "" goto init
echo.
echo :init
echo if not exist "%%APP_HOME%%\gradle\wrapper\gradle-wrapper.jar" goto fail
echo.
echo set CLASSPATH=%%APP_HOME%%\gradle\wrapper\gradle-wrapper.jar
echo.
echo "%%JAVA_EXE%%" %%DEFAULT_JVM_OPTS% %%JAVA_OPTS% %%GRADLE_OPTS% -classpath "%%CLASSPATH%%" org.gradle.wrapper.GradleWrapperMain %%*
echo.
echo :fail
echo echo Nao foi possivel encontrar o Gradle Wrapper.
echo exit /b 1
) > gradlew.bat

echo.
echo Gradle Wrapper criado!
echo Para compilar, execute: gradlew.bat assembleDebug
pause
