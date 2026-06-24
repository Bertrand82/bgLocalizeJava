package com.bg.bglocalize.match;

import java.util.List;
import java.util.Objects;

import org.opencv.core.DMatch;

import com.bg.bglocalize.colmap.Image2DColmapOpenCV;
import com.bg.bglocalize.features.FeatureExtractionResult;

public final class FeatureMatchResult {

	private final FeatureExtractionResult query;
	private final Image2DColmapOpenCV target;
	private final List<DMatch> matches;
	float DISTANCE_MIN = 60.001f;
	double distanceMoyenne;
	double distanceEcartType;

	public FeatureMatchResult(FeatureExtractionResult query, Image2DColmapOpenCV target, List<DMatch> matches) {
		this.query = Objects.requireNonNull(query, "query must not be null");
		this.target = Objects.requireNonNull(target, "target must not be null");
		distanceMoyenne = matches.stream().mapToDouble(m -> m.distance).average().orElse(Double.NaN);

		double variance = matches.stream().mapToDouble(m -> Math.pow(m.distance - distanceMoyenne, 2)).average()
				.orElse(Double.NaN); // variance population

		this.distanceEcartType = Math.sqrt(variance);
		this.matches = matches.stream().filter(e -> e.distance < distanceMoyenne).toList();
	}

	public FeatureExtractionResult getQuery() {
		return query;
	}

	public Image2DColmapOpenCV getTarget() {
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
		return "FeatureMatchResult [query=" + query.getImageId() + ", target=" + target.getImageName() + ", matchCount="
				+ matches.size()+"  distanceMoyenne="+distanceMoyenne +" ecartType="+distanceEcartType+ "]";
	}



}
