package course.concurrency.m2_async.cf.min_price;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        List<CompletableFuture<Double>> completableFutures =
                shopIds.stream()
                        .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId))
                                .exceptionally(ex -> Double.NaN))
                        .collect(Collectors.toList());

        return Stream.of(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).
                map(f -> {
                    try {
                        return  (Double) f.get(1, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        //throw new RuntimeException(e);
                        return Double.NaN;
                    }
                })
                .filter(Objects::nonNull)
                .filter(d -> d > 0)
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }
}
