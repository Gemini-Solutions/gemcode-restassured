import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.*;
import java.util.Properties;

public class compileExceptionsUploader {
    static String executionName;

    public static void main(String[] args) throws IOException {
        Properties reportNameReader = new Properties();
        reportNameReader.load(new FileInputStream("./reportName.properties"));
            executionName = reportNameReader.getProperty("reportName");
            String output=readClassFileAsString("./test-output/"+executionName+".txt");
            if (output.contains("on project RestassuredExecution: Compilation failure")) {
                output = output.split("Error:  Failed to execute goal org.apache.maven.plugins:maven-compiler")[1].replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r");;
                String classContent = readClassFileAsString("./src/main/java/App.java").replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r");
                mongoTransfer(classContent,output);
            }
    }

    public static String readClassFileAsString(String filePath) throws IOException {
        //Reading user-updated code
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }
        return content.toString();
    }

    public static void mongoTransfer(String classContent, String output){
        String userId = executionName.split("_")[1];
        RestAssured.baseURI = "https://us-east-1.aws.data.mongodb-api.com/app/application-0-awqqz/endpoint/updateSeleniumSubmission";
        //    String payload = "{\"filter\": {\"url\":\"\"" + userId + "},\"SubmittedCode\":\"\"" + classContent + ",\"Output\":\"\"" + output + "}";
        String payload = "{\n" +
                "    \"filter\": {\n" +
                "        \"url\": \"" + userId + "\"\n" +
                "    },\n" +
                "    \"SubmittedCode\":\"" + classContent + "\",\n" +
                "    \"Output\":\"" + output + "\"\n" +
                "}";
        Response response = RestAssured.given().contentType("application/json").body(payload).put().then().extract().response();
        System.out.println(response.statusCode());
    }
}
