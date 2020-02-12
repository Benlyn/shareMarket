package utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;

public class marketUtils {
    private final String FILE_SEPARATOR = System.getProperty("file.separator");
    private final String processedFolder = "processedData";

    ArrayList buyList = new ArrayList();
    ArrayList sellList = new ArrayList();
    ArrayList keepOnHoldList = new ArrayList();

    public JSONObject fetchDataFromCsvFile(String csvFilePath) throws IOException {
        JSONObject jsonObject = new JSONObject();
        ArrayList list = new ArrayList();
        File csvFile = new File(System.getProperty("user.dir") + FILE_SEPARATOR + "marketData" + FILE_SEPARATOR + csvFilePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
        bufferedReader.lines().forEach(eachLine -> {
            list.add((eachLine.split(",")[0]));
            jsonObject.put((eachLine.split(",")[0]).replace("\"", ""), createIndividualJsonObject(eachLine));
        });
        return jsonObject;
    }

    JSONObject createIndividualJsonObject(String eachDetail) {
        JSONObject jsonObject = new JSONObject();
        String[] detailsOfEachCompany = eachDetail.replace("\"", "").split(",");
        jsonObject.put("COMPANY_NAME", detailsOfEachCompany[0]);
        jsonObject.put("OPEN_PRICE", detailsOfEachCompany[1]);
        jsonObject.put("HIGH_PRICE", detailsOfEachCompany[2]);
        jsonObject.put("LOW_PRICE", detailsOfEachCompany[3]);
        jsonObject.put("LAST_TRADED_PRICE", detailsOfEachCompany[4]);
        jsonObject.put("CHANGE_IN_RUPEES", detailsOfEachCompany[5]);
        jsonObject.put("CHANGE_IN_PERCENTAGE", detailsOfEachCompany[6]);
        jsonObject.put("TRADED_VOLUME_IN_LACS", detailsOfEachCompany[7]);
        jsonObject.put("TRADED_VOLUME_IN_CRS", detailsOfEachCompany[8]);
        jsonObject.put("52_WEEK_HIGH", detailsOfEachCompany[9]);
        jsonObject.put("52_WEEK_LOW", detailsOfEachCompany[10]);
        jsonObject.put("365_DAYS_CHANGE_PERCENTAGE", detailsOfEachCompany[11]);
        jsonObject.put("30_DAYS_CHANGE_PERCENTAGE", detailsOfEachCompany[12]);
        return jsonObject;
    }

    public void writeDataToJsonFile(String jsonString, String folder, String fileName) {
        String filePath = System.getProperty("user.dir") + FILE_SEPARATOR + folder + FILE_SEPARATOR + fileName;
        System.out.println("Path to write is : " + filePath);
        try {
            Files.write(Paths.get(filePath), jsonString.getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while writing data to file");
        }
    }

    public String readJSONFileToString(String folder, String fileName) {
        String absoluteFilePath = System.getProperty("user.dir") + FILE_SEPARATOR + folder + FILE_SEPARATOR + fileName;
        System.out.println(" path to read : " + absoluteFilePath);
        String fileContent;
        try {
            fileContent = new String(Files.readAllBytes(Paths.get(absoluteFilePath)));
            return fileContent.trim();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while reading data from file");
        }
        return null;
    }


    public void processNiftyData() throws IOException {
        JSONObject csvToJsonData;
        csvToJsonData = fetchDataFromCsvFile(getFolderName() + FILE_SEPARATOR + "NIFTY_FIFTY.csv");
        loadDataToProcessedDataFolder(csvToJsonData.toString(4), "nifty50data.json");
    }

    public void performOHLStrategy() throws IOException {
        processNiftyData();
        applyOHLStrategy();
    }

    public void applyOHLStrategy() throws IOException {
        JSONObject jsonObject = new JSONObject(readJSONFileToString(processedFolder + FILE_SEPARATOR + getFolderName(), "nifty50data.json"));
        jsonObject.keySet().forEach(eachKey -> {
            String open = jsonObject.getJSONObject(eachKey).get("OPEN_PRICE").toString();
            String high = jsonObject.getJSONObject(eachKey).get("HIGH_PRICE").toString();
            String low = jsonObject.getJSONObject(eachKey).get("LOW_PRICE").toString();
            builddataToBePrintedForOhlStrategy(eachKey, ohlStrategy(open, high, low));
        });

        JSONObject finalJson = new JSONObject();
        finalJson.put("BUY", buyList);
        finalJson.put("SELL", sellList);
        finalJson.put("KEEP_ON_HOLD", keepOnHoldList);
        loadDataToProcessedDataFolder(finalJson.toString(4), "ohlResult.json");
    }

    void builddataToBePrintedForOhlStrategy(String companyName, int i) {
        if (i == 1)
            buyList.add(companyName);
        if (i == 0)
            sellList.add(companyName);
        if (i == -1)
            keepOnHoldList.add(companyName);
    }


    int ohlStrategy(String open, String high, String low) {
        if (open.equalsIgnoreCase(low)) {
            return 1;
        } else if (open.equalsIgnoreCase(high)) {
            return 0;
        } else {
            return -1;
        }
    }

    private String getFolderName() {
        LocalDate currentdate = LocalDate.now();
        return currentdate.toString();
    }


    public void loadDataToProcessedDataFolder(String dataToBeWritten, String fileName) throws IOException {
        String pathTillProcessedFolder = System.getProperty("user.dir") + FILE_SEPARATOR + processedFolder;
        File directory = new File(pathTillProcessedFolder, getFolderName());
        if (!directory.exists()) {
            directory.mkdir();
        }
        String pathTillChildDirectoryFile = pathTillProcessedFolder + FILE_SEPARATOR + getFolderName();
        File fileRequiredToCreate = new File(pathTillChildDirectoryFile, fileName);
        if (!fileRequiredToCreate.exists()) {
            fileRequiredToCreate.createNewFile();
        }
        writeDataToJsonFile(dataToBeWritten, processedFolder + FILE_SEPARATOR + getFolderName(), fileName);
    }


}
