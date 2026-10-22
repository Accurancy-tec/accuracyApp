package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StockHistoryResponse {
    @SerializedName("results")
    private List<Result> results;

    public List<Result> getResults() { return results; }

    public static class Result {
        @SerializedName("requestedSymbol") private String requestedSymbol;
        @SerializedName("symbol") private String symbol;
        @SerializedName("data") private Data data;
        public String getRequestedSymbol() { return requestedSymbol; }
        public String getSymbol() { return symbol; }
        public Data getData() { return data; }
    }

    public static class Data {
        @SerializedName("usedRange") private String usedRange;
        @SerializedName("usedInterval") private String usedInterval;
        @SerializedName("historicalDataPrice") private List<Point> historicalDataPrice;
        public String getUsedRange() { return usedRange; }
        public String getUsedInterval() { return usedInterval; }
        public List<Point> getHistoricalDataPrice() { return historicalDataPrice; }
    }

    public static class Point {
        @SerializedName("date") private long date;
        @SerializedName("open") private Double open;
        @SerializedName("high") private Double high;
        @SerializedName("low") private Double low;
        @SerializedName("close") private Double close;
        @SerializedName("adjustedClose") private Double adjustedClose;
        @SerializedName("volume") private Long volume;
        public long getDate() { return date; }
        public Double getOpen() { return open; }
        public Double getHigh() { return high; }
        public Double getLow() { return low; }
        public Double getClose() { return close; }
        public Double getAdjustedClose() { return adjustedClose; }
        public Long getVolume() { return volume; }
    }
}
