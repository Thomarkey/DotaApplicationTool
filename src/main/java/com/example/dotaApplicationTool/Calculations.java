package com.example.dotaApplicationTool;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.*;

import static com.example.dotaApplicationTool.HeroDisplay.zScoresMap;

public class Calculations {
    static HeroDisplay heroDisplay = new HeroDisplay();

    public int getStatsRank(JSONObject json, String selectedHero, String selectedStat) throws JSONException {
        double selectedStatValue = json.getJSONObject(selectedHero).getDouble(selectedStat);

        int rank = 1;
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject hero = json.getJSONObject(key);
            double statValue = hero.getDouble(selectedStat);
            if (key.equals(selectedHero)) {
                continue;
            }
            if (statValue > selectedStatValue) {
                rank++;
            }
        }

        return rank;
    }

    public Map<String, Map<String, Integer>> calculateStatsRanks(JSONObject json) throws JSONException {
        Map<String, Map<String, Integer>> statsRankMap = new HashMap<>();
        for (Iterator<String> heroIter = json.keys(); heroIter.hasNext(); ) {
            String hero = heroIter.next();
            JSONObject heroObj = json.getJSONObject(hero);
            Map<String, Double> statsMap = new HashMap<>();

            //map because its needs to be sorted
            for (Iterator<String> statIter = heroObj.keys(); statIter.hasNext(); ) {
                String stat = statIter.next();
                statsMap.put(stat, heroObj.getDouble(stat));
            }

            Map<String, Integer> ranksMap = new HashMap<>();
            int rank = 1;
            for (String stat : Helper.sortMapByValueDescending(statsMap).keySet()) {
                ranksMap.put(stat, rank++);
            }
            statsRankMap.put(hero, ranksMap);
        }

        return statsRankMap;
    }


    public static Map<String, Map<String, Double>> calculateZScores(JSONObject json) throws JSONException {
        if (zScoresMap == null) {
            Map<String, Map<String, Double>> zScoresMap = new HashMap<>();

            // Step 1: Calculate mean and standard deviation for each statistic
            Map<String, Double> meanMap = new HashMap<>();
            Map<String, Double> stdDevMap = new HashMap<>();
            for (Iterator<String> heroIter = json.keys(); heroIter.hasNext(); ) {
                String hero = heroIter.next();
                JSONObject heroObj = json.getJSONObject(hero);
                for (Iterator<String> statIter = heroObj.keys(); statIter.hasNext(); ) {
                    String stat = statIter.next();
                    double value = heroObj.getDouble(stat);
                    meanMap.merge(stat, value, Double::sum);
                    stdDevMap.merge(stat, value * value, Double::sum);
                }
            }
            int numHeroes = json.length();
            for (String stat : meanMap.keySet()) {
                double mean = meanMap.get(stat) / numHeroes;
                double stdDev = Math.sqrt(stdDevMap.get(stat) / numHeroes - mean * mean);
                stdDevMap.put(stat, stdDev);
                meanMap.put(stat, mean);
            }

            // Step 2: Calculate z-score for each statistic of each hero
            for (Iterator<String> heroIter = json.keys(); heroIter.hasNext(); ) {
                String hero = heroIter.next();
                JSONObject heroObj = json.getJSONObject(hero);
                Map<String, Double> zScoreMap = new HashMap<>();
                for (Iterator<String> statIter = heroObj.keys(); statIter.hasNext(); ) {
                    String stat = statIter.next();
                    double value = heroObj.getDouble(stat);
                    double mean = meanMap.get(stat);
                    double stdDev = stdDevMap.get(stat);
                    double zScore;
                    if (stdDev == 0) {
                        zScore = 0;
                    } else {
                        zScore = (value - mean) / stdDev;
                    }

                    zScoreMap.put(stat, zScore);
                }
                zScoresMap.put(hero, zScoreMap);
            }
            return zScoresMap;
        } else {
            return zScoresMap;
        }
    }

    public static Double getHeroZScore(String hero, String propertyName) throws UnirestException, JSONException {
        if (zScoresMap == null) {
            JSONObject heroesStatsJson = heroDisplay.getHeroesWithStatsJson();
            zScoresMap = calculateZScores(heroesStatsJson);
        }

        Map<String, Double> heroZScores = zScoresMap.get(hero);
        if (heroZScores == null) {
            throw new IllegalArgumentException("Hero not found: " + hero);
        }

        Double zScore = heroZScores.get(propertyName);
        if (zScore == null) {
            throw new IllegalArgumentException("Property not found for hero " + hero + ": " + propertyName);
        }

        return zScore;
    }

}