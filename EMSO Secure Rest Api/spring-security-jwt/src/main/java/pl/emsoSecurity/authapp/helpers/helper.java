package pl.emsoSecurity.authapp.helpers;

import org.json.JSONArray;

import java.util.*;
import java.util.stream.IntStream;

public class helper {

    /***
     * This method is used to flatten the JSON Array to accomodate the format to the CSV parser.
     * @param nestedArrayString
     * @return
     */
    private static List<String> flattenJsonArray(String nestedArrayString) {
        int attributeCount = 178;
        String[] flatArray = new String[attributeCount];
        Arrays.fill(flatArray, "");
        List flattenedArray = Arrays.asList(flatArray);
        List<JSONArray> extra = new ArrayList<>();

        List<Integer> incoherentPids = Arrays.asList(1, 2, 10, 11, 12, 13, 15, 16, 17, 32, 36, 37);
        Map<Integer, Integer> locationMap = new HashMap<>();
        locationMap.put(1, 0);
        locationMap.put(2, 1);
        locationMap.put(10, 2);
        locationMap.put(11, 3);
        locationMap.put(12, 4);
        locationMap.put(13, 5);
        locationMap.put(15, 6);
        locationMap.put(16, 7);
        locationMap.put(17, 8);
        locationMap.put(32, 9);
        locationMap.put(36, 10);
        locationMap.put(37, 11);

        JSONArray nestedArray = new JSONArray(nestedArrayString);

        int minimumAttributeCount = 259;
        int overhead = 259 - (locationMap.size());

        IntStream
                .range(0, nestedArray.length())
                .mapToObj(nestedArray::getJSONArray)
                .forEach( array -> {
                    if (!(array.get(1) instanceof Integer)) {
                        return;
                    } else if (incoherentPids.contains(array.get(1))) {
                        flattenedArray.set(locationMap.get(array.getInt(1)), String.valueOf(array.get(2)));
                    } else if (array.getInt(1) >= minimumAttributeCount) {
                        flattenedArray.set(array.getInt(1)-overhead, String.valueOf(array.get(2)));
                    } else {
                        JSONArray extraArray = new JSONArray();
                        extraArray.put(new JSONArray(Arrays.asList(array.getInt(1), array.getInt(2))));
                        extra.add(extraArray);
                    }
                });
        flattenedArray.set(attributeCount-1, String.valueOf(extra));

        return flattenedArray;
    }

    /***
     * This method is a wrapper for processing a JSON response and flattening it to be fed to the CSV parser.
     * @param inputJson
     * @return
     */
    public static List<List<String>> processResponseJson(List<List<String>> inputJson) {

        List<List<String>> processedJson = new ArrayList<>();

        inputJson.forEach(row -> {
            List<String> processedRow = row.subList(1,6);
            processedRow.set(1, String.valueOf(new Date(Long.parseLong(row.get(2))*1000L)));
            processedRow.addAll(flattenJsonArray(row.get(6)));

            processedJson.add(processedRow);
        });

        return processedJson;
    }
}
