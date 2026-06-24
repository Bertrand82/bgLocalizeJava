package com.bg.bglocalize.match;



import org.opencv.core.DMatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class UtilsMatchFiltering {

    private UtilsMatchFiltering() {}

    /**
     * Trie par distance croissante (meilleurs d'abord).
     */
    public static List<DMatch> sortByDistance(List<DMatch> matches) {
        List<DMatch> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator.comparingDouble(m -> m.distance));
        return sorted;
    }

    /**
     * Garde les top-N meilleurs matches (distance la plus faible).
     */
    public static List<DMatch> topN(List<DMatch> matches, int n) {
        if (matches == null || matches.isEmpty() || n <= 0) {
            return List.of();
        }
        List<DMatch> sorted = sortByDistance(matches);
        int toIndex = Math.min(n, sorted.size());
        return new ArrayList<>(sorted.subList(0, toIndex));
    }

    /**
     * Seuil adaptatif: garde les matches dont distance <= mean + k * stdDev.
     *
     * k typiques:
     * - 0.5 à 1.0 : strict
     * - 1.5 à 2.0 : plus permissif
     */
    public static List<DMatch> adaptiveThresholdMeanStd(List<DMatch> matches, double k) {
        if (matches == null || matches.isEmpty()) {
            return List.of();
        }

        double mean = matches.stream().mapToDouble(m -> m.distance).average().orElse(Double.NaN);
        if (Double.isNaN(mean)) {
            return List.of();
        }

        double variance = matches.stream()
                .mapToDouble(m -> {
                    double d = m.distance - mean;
                    return d * d;
                })
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);
        double threshold = mean + k * stdDev;

        List<DMatch> kept = new ArrayList<>();
        for (DMatch m : matches) {
            if (m.distance <= threshold) {
                kept.add(m);
            }
        }
        kept.sort(Comparator.comparingDouble(m -> m.distance));
        return kept;
    }

    /**
     * Variante robuste: seuil adaptatif basé sur minDistance.
     * threshold = max(factor * minDist, floor)
     *
     * Très utilisé en pratique pour ORB.(Très utilisé en pratique pour ORB)
     */
    public static List<DMatch> adaptiveThresholdFromMin(
            List<DMatch> matches,
            double factor,   // ex: 2.0 ou 3.0
            double floor     // ex: 30.0 pour ORB (à ajuster)
    ) {
        if (matches == null || matches.isEmpty()) {
            return List.of();
        }

        double minDist = matches.stream()
                .mapToDouble(m -> m.distance)
                .min()
                .orElse(Double.POSITIVE_INFINITY);

        double threshold = Math.max(factor * minDist, floor);

        List<DMatch> kept = new ArrayList<>();
        for (DMatch m : matches) {
            if (m.distance <= threshold) {
                kept.add(m);
            }
        }
        kept.sort(Comparator.comparingDouble(m -> m.distance));
        return kept;
    }

    /**
     * Pipeline pratique:
     * 1) seuil adaptatif (mean+std)
     * 2) top-N
     */
    public static List<DMatch> adaptiveThenTopN(
            List<DMatch> matches,
            double k,
            int topN
    ) {
        List<DMatch> filtered = adaptiveThresholdMeanStd(matches, k);
        return topN(filtered, topN);
    }

    /**
     * Pipeline pratique (variante minDist):
     * 1) seuil adaptatif (factor * minDist avec plancher)
     * 2) top-N
     */
    public static List<DMatch> adaptiveMinThenTopN(
            List<DMatch> matches,
            double factor,
            double floor,
            int topN
    ) {
        List<DMatch> filtered = adaptiveThresholdFromMin(matches, factor, floor);
        return topN(filtered, topN);
    }
}
