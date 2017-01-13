package org.jenkinsci.plugins.testresultsanalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jenkinsci.plugins.testresultsanalyzer.config.UserConfig;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.Info;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsTreeUtil {

    public JSONObject getJsTree(List<Integer> builds, ResultInfo resultInfo, boolean hideConfigMethods) {
        JSONObject tree = new JSONObject();

        JSONArray buildJson = new JSONArray();
        for (Integer buildNumber : builds) {
            buildJson.add(buildNumber.toString());
        }
        tree.put("builds", buildJson);

        JSONArray results = new JSONArray();
        for (Map.Entry<String, ? extends Info> entry : resultInfo.getPackageResults().entrySet()) {
            results.add(createJson(builds, entry.getValue(), hideConfigMethods));
        }
        tree.put("results", results);

        return tree;
    }

    private JSONObject createJson(List<Integer> builds, Info info, boolean hideConfigMethods) {
        JSONObject baseJson = new JSONObject();

        baseJson.put("text", info.getName());
        baseJson.put("buildResults", getBuilds(builds, info));
        baseJson.put("children", getChildren(builds, info, hideConfigMethods));

        return baseJson;
    }

    private JSONArray getBuilds(List<Integer> builds, Info info) {
        JSONArray treeDataJson = new JSONArray();
        for (Integer buildNumber : builds) {
            treeDataJson.add(getBuild(buildNumber, info));
        }
        return treeDataJson;
    }

    private JSONArray getChildren(List<Integer> builds, Info info, boolean hideConfigMethods) {
        Map<String, ? extends Info> childrenInfo = info.getChildren();
        if (childrenInfo == null)
            return new JSONArray();

        JSONArray children = new JSONArray();
        for (Map.Entry<String, ? extends Info> entry : childrenInfo.entrySet()) {
            if (!hideConfigMethods || !entry.getValue().isConfig()) {
                children.add(createJson(builds, entry.getValue(), hideConfigMethods));
            }
        }

        return children;
    }

    private JSONObject getBuild(Integer buildNumber, Info info) {
        JSONObject json = new JSONObject();
        json.put("buildNumber", buildNumber.toString());

        ResultData result = info.getBuildResult(buildNumber);
        if (result == null) {
            json.put("status", "N/A");
        } else {
            json.put("totalTests", result.getTotalTests());
            json.put("totalFailed", result.getTotalFailed());
            json.put("totalPassed", result.getTotalPassed());
            json.put("totalSkipped", result.getTotalSkipped());
            json.put("totalTimeTaken", result.getTotalTimeTaken());
            json.put("status", result.getStatus());
            json.put("url", result.getUrl());
        }


        return json;
    }
}
