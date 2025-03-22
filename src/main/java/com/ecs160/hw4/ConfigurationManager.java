package com.ecs160.hw4;

public class ConfigurationManager {
    private static ConfigurationManager instance;

    private String jsonFileName;
    private int analysisType;

    private ConfigurationManager() {}

    public static final int WEIGHTED = 1;
    public static final int NON_WEIGHTED = 2;

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    public void setConfig(String jsonFileName, int analysisType) {
        this.jsonFileName = jsonFileName;
        this.analysisType = analysisType;
    }

    public void setJsonFileName(String fileName){
        jsonFileName = fileName;
    }

    public void setAnalysisType(int aType) {
        analysisType = aType;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public int getAnalysisType() {
        return analysisType;
    }
}