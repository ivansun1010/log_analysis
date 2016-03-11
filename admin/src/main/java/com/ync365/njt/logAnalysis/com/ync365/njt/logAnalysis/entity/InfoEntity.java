package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.entity;

/**
 * Created by ivan on 16/3/9.
 */
public class InfoEntity {
    private String date;

    private String message;

    private String functionName;

    private String num;

    private String dateL;

    private String logId;

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getDateL() {
        return dateL;
    }

    public void setDateL(String dateL) {
        this.dateL = dateL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
