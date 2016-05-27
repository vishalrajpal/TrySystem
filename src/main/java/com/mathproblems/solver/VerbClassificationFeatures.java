package com.mathproblems.solver;

import com.mathproblems.solver.classifier.SVMClassifier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.http.protocol.HTTP.USER_AGENT;


public class VerbClassificationFeatures {

    private static final String VERB_CATEGORY_REGEX = "(<[a-zA-Z0-9.]+>)";

    public static LinkedHashMap<String, Double> runWord2Vec(final String word) {
        LinkedHashMap<String, Double> closestWords = new LinkedHashMap<>();
        try {
            String url = "http://hetzner.rare-technologies.com/w2v/most_similar?positive[]=" + word;
            //String url = "http://dev.fz-qqq.net:5000/api/w2v/?corpus=text8&type=word&queries=" + word;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray dataArray = jsonObject.getJSONArray("similars");

            for ( int i = 0; i < dataArray.length(); i++) {
                JSONArray singleData = dataArray.getJSONArray(i);
                closestWords.put((String)singleData.get(0), (Double)singleData.get(1));
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return prepareWord2VecFeature(closestWords);
        //return closestWords;
    }

    private static LinkedHashMap<String, Double> prepareWord2VecFeature(LinkedHashMap<String, Double> closestWords) {
        int maxClosestWordsToConsider = 3;
        int closestWordCounter = 0;
        int maxClosestCategoriesToConsider = 5;
        int closestCategoriesCounter;
        List<LinkedHashMap<String, Double>> wordNetCategoriesForClosestWords = new ArrayList<>();
        LinkedHashSet<String> categories = new LinkedHashSet<>();
        for (String word: closestWords.keySet()) {
            if(closestWordCounter == maxClosestWordsToConsider) {
                break;
            }
            LinkedHashMap<String, Double> categoriesForClosestVerb = runWordNet(word);
            if(categoriesForClosestVerb.size() != 0) {
                closestCategoriesCounter = 0;
                for (String category : categoriesForClosestVerb.keySet()) {
                    if (closestCategoriesCounter == maxClosestCategoriesToConsider) {
                        break;
                    }
                    if(SVMClassifier.importantCategories.contains(category)) {
                        categories.add(category);
                        closestCategoriesCounter++;
                    }
                }
                wordNetCategoriesForClosestWords.add(runWordNet(word));
                closestWordCounter++;
            }
        }

        LinkedHashMap<String, Double> probabilityFromWord2Vec = assignProbabilities(categories);
        return probabilityFromWord2Vec;
    }

    public static LinkedHashMap<String, Double> runWordNet(final String word) {
        StringBuffer output = new StringBuffer();
        LinkedHashSet<String> verbCategories = new LinkedHashSet<String>();
        String command = "/usr/local/WordNet-3.0/bin/wn " + word + " -a -simsv";
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            final Pattern verbCategoryPattern = Pattern.compile(VERB_CATEGORY_REGEX);
            Matcher verbCategoryMatcher;
            while ((line = reader.readLine())!= null) {
                if(line.startsWith("<verb.")) {
                    verbCategoryMatcher = verbCategoryPattern.matcher(line);
                    if(verbCategoryMatcher.find()) {
                        String category = verbCategoryMatcher.group();
                        if(SVMClassifier.importantCategories.contains(category)) {
                            verbCategories.add(category);
                        }
                    }
                    output.append(line + "\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return assignProbabilities(verbCategories);
    }

    public static LinkedHashMap<String, Double> assignProbabilities(LinkedHashSet<String> categories) {
        LinkedHashMap<String, Double> categoriesWithProbabilities = new LinkedHashMap<String, Double>();
        int noOfCategories = categories.size();
        double toSubtract = 0;
        double defaultValue = 1.0/noOfCategories;
        double restSum = 1 - defaultValue;
        double currentValue;
        int index = 1;
        for (String key : categories) {
            int denominator = (noOfCategories - index) + 1;
            double toAdd;
            toAdd = (restSum / denominator);
            currentValue = (defaultValue - toSubtract) + toAdd;
            categoriesWithProbabilities.put(key, currentValue);
            toSubtract = toSubtract + (toAdd / denominator);
            restSum = restSum - toAdd - (defaultValue - toSubtract);
            index++;
        }
        return categoriesWithProbabilities;
    }
}
