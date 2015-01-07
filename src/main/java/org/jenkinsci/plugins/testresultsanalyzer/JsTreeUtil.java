package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.List;
import java.util.Set;

import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsTreeUtil {

    public JSONObject getJsTree(List<Integer> builds, ResultInfo resultInfo) {
        JSONObject tree = new JSONObject();

        JSONArray buildJson = new JSONArray();
        for (Integer buildNumber : builds) {
            buildJson.add(buildNumber.toString());
        }
        tree.put("builds", buildJson);
        JSONObject packageResults = resultInfo.getJsonObject();
        JSONArray results = new JSONArray();
        for (Object packageName : packageResults.keySet()) {

            JSONObject packageJson = packageResults.getJSONObject((String) packageName);
            results.add(createJson(builds, packageJson));
        }
        tree.put("results", results);
        return tree;
    }

    private JSONObject getBaseJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", "");
        jsonObject.put("buildResults", new JSONArray());
        return jsonObject;
    }

    private JSONObject createJson(List<Integer> builds, JSONObject dataJson) {
        JSONObject baseJson = getBaseJson();
        baseJson.put("text", dataJson.get("name"));
        baseJson.put("type", dataJson.get("type"));
        baseJson.put("buildStatuses", dataJson.get("buildStatuses"));
        JSONObject packageBuilds = dataJson.getJSONObject("builds");
        JSONArray treeDataJson = new JSONArray();
        for (Integer buildNumber : builds) {
            JSONObject build = new JSONObject();
            if (packageBuilds.containsKey(buildNumber.toString())) {
                JSONObject buildResult = packageBuilds.getJSONObject(buildNumber.toString());
                //String status = buildResult.getString("status");
                buildResult.put("buildNumber", buildNumber.toString());
                build = buildResult;
            } else {
                build.put("status", "N/A");
                build.put("buildNumber", buildNumber.toString());
            }
            treeDataJson.add(build);
        }
        baseJson.put("buildResults", treeDataJson);

        if (dataJson.containsKey("children")) {
            JSONArray childrens = new JSONArray();
            JSONObject childrenJson = dataJson.getJSONObject("children");
            @SuppressWarnings("unchecked")
            Set<String> childeSet = (Set<String>) childrenJson.keySet();
            for (String childName : childeSet) {
                childrens.add(createJson(builds, childrenJson.getJSONObject(childName)));
            }
            baseJson.put("children", childrens);
        }

        return baseJson;
    }
}
