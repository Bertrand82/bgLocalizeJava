package com.bg.bglocalize.match;

import java.util.List;
import java.util.Objects;

import org.opencv.core.DMatch;

import com.bg.bglocalize.colmap.ColmapImageOpenCV;
import com.bg.bglocalize.features.FeatureExtractionResult;

public final class FeatureMatchResult {

    private final FeatureExtractionResult query;
    private final ColmapImageOpenCV target;
    private final List<DMatch> matches;

    public FeatureMatchResult(FeatureExtractionResult query, ColmapImageOpenCV target, List<DMatch> matches) {
        this.query = Objects.requireNonNull(query, "query must not be null");
        this.target = Objects.requireNonNull(target, "target must not be null");
        this.matches = List.copyOf(Objects.requireNonNull(matches, "matches must not be null"));
    }

    public FeatureExtractionResult getQuery() {
        return query;
    }

    public ColmapImageOpenCV getTarget() {
        return target;
    }

    public List<DMatch> getMatches() {
        return matches;
    }

    public int getMatchCount() {
        return matches.size();
    }

    @Override
    public String toString() {
        return "FeatureMatchResult [query=" + query.getImageId()
                + ", target=" + target.getImageName()
                + ", matchCount=" + matches.size() + "]";
    }
}
