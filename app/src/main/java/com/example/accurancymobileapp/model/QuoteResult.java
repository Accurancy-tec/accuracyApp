package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class QuoteResult {
    @SerializedName("requestedSymbol") private String requestedSymbol;
    @SerializedName("symbol") private String symbol;
    @SerializedName("changed") private boolean changed;
    @SerializedName("data") private QuoteData data;

    @SerializedName("name") private String name;
    @SerializedName("shortName") private String shortName;
    @SerializedName("longName") private String longName;
    @SerializedName("regularMarketVolume") private Long volume;
    @SerializedName("regularMarketPrice") private Double price;
    @SerializedName("regularMarketChangePercent") private Double changePercent;
    @SerializedName("regularMarketTime") private String marketTime;
    @SerializedName("regularMarketOpen") private Double regularMarketOpen;
    @SerializedName("regularMarketDayHigh") private Double regularMarketDayHigh;
    @SerializedName("fiftyTwoWeekHigh") private Double fiftyTwoWeekHigh;
    @SerializedName("fiftyTwoWeekLow") private Double fiftyTwoWeekLow;
    @SerializedName("logourl") private String logoUrl;
    @SerializedName("currency") private String currency;

    public String getRequestedSymbol() { return requestedSymbol; }
    public String getSymbol() { return symbol; }
    public boolean isChanged() { return changed; }
    public QuoteData getData() { return data; }

    public QuoteData toQuoteData() {
        String nomeCurto = shortName != null ? shortName : name;
        String nomeLongo = longName != null ? longName : name;

        if (data != null) {
            return new QuoteData(
                    data.getSymbol() != null ? data.getSymbol()
                            : (symbol != null ? symbol : requestedSymbol),
                    data.getShortName() != null ? data.getShortName() : nomeCurto,
                    data.getLongName() != null ? data.getLongName() : nomeLongo,
                    data.getRegularMarketVolume() != null
                            ? data.getRegularMarketVolume() : volume,
                    data.getRegularMarketPrice() != null
                            ? data.getRegularMarketPrice() : price,
                    data.getRegularMarketChangePercent() != null
                            ? data.getRegularMarketChangePercent() : changePercent,
                    data.getRegularMarketTime() != null
                            ? data.getRegularMarketTime() : marketTime,
                    data.getLogoUrl() != null ? data.getLogoUrl() : logoUrl,
                    data.getCurrency() != null ? data.getCurrency() : currency,
                    data.getRegularMarketOpen() != null
                            ? data.getRegularMarketOpen() : regularMarketOpen,
                    data.getRegularMarketDayHigh() != null
                            ? data.getRegularMarketDayHigh() : regularMarketDayHigh,
                    data.getFiftyTwoWeekHigh() != null
                            ? data.getFiftyTwoWeekHigh() : fiftyTwoWeekHigh,
                    data.getFiftyTwoWeekLow() != null
                            ? data.getFiftyTwoWeekLow() : fiftyTwoWeekLow
            );
        }

        return new QuoteData(
                symbol != null ? symbol : requestedSymbol,
                nomeCurto,
                nomeLongo,
                volume,
                price,
                changePercent,
                marketTime,
                logoUrl,
                currency,
                regularMarketOpen,
                regularMarketDayHigh,
                fiftyTwoWeekHigh,
                fiftyTwoWeekLow
        );
    }
}
