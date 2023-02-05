package igentuman.galacticresearch.util;

import java.util.HashMap;

public class Util {
    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? ""
                : (s.substring(0, s.length() - 1));
    }
    public static String serializeMap(HashMap<String, Integer> map)
    {
        StringBuilder tmp = new StringBuilder();
        for(String m: map.keySet()) {
            tmp.append(m).append(":").append(map.get(m)).append(";");
        }
        return removeLastChar(tmp.toString());
    }

    public static HashMap<String, Integer> unserializeMap(String sr)
    {
        String[] pairs = sr.split(";");
        HashMap<String, Integer> tmp = new HashMap<>();
        for (String pair: pairs) {
            String[] entry = pair.split(":");
            if(entry.length != 2) continue;
            tmp.put(entry[0],Integer.parseInt(entry[1]));
        }
        return tmp;
    }
}
