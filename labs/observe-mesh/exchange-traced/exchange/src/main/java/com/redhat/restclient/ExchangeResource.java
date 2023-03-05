package com.redhat.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.metrics.annotation.Metered;

@Path("/exchangeRate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExchangeResource {

    @Inject
    @RestClient
    ExchangeService historyService;

    @Inject
    @RestClient
    NewsService newsService;

    ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/news")
    public List<News> getFinancialNews() {
        return newsService.getFinancialNews();
    }

    @POST
    @Path("/historicalData")
    @SimplyTimed(name = "exchange_svc:history_fetch_time",
               description = "A measure of how long it takes to process getHistoricalData",
               unit = MetricUnits.MILLISECONDS)
    @Metered(name = "exchange_svc:history_fetch_rate",
             unit = MetricUnits.MINUTES,
             description = "Rate at which request are placed",
             absolute = true)
    public List<Currency> getHistoricalData(String body) {
        
        return historyService.getCurrencyExchangeRates(body);
    }

    @POST
    @Path("/singleCurrency")
    public Currency getExchangeRate(String body) {
        List<Currency> currencies = historyService.getCurrencyExchangeRates(body);
        Currency latestCurrency = currencies.get(0);
        try {
            String target = mapper.readTree(body).get("target").asText();
            if(target.equals("USD")) {
                latestCurrency.setSign("$");
            } else {
                latestCurrency.setSign("€");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latestCurrency;
    }

    // A simple health check of the service, as well as
    // connectivity check between the service and other services
    @GET
    public String ping() {
        return "pong";
    }
}
