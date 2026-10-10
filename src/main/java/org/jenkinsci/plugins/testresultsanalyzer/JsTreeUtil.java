package org.jenkinsci.plugins.testresultsanalyzer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.jenkinsci.plugins.testresultsanalyzer.result.data.ResultData;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.BuildInfo;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.Info;
import org.jenkinsci.plugins.testresultsanalyzer.result.info.ResultInfo;

public class JsTreeUtil {

    public JSONObject getJsTree(List<Integer> builds, ResultInfo resultInfo, boolean hideConfigMethods) {
        JSONObject tree = new JSONObject();

        JSONArray buildJson = new JSONArray();
        for (Integer buildNumber : builds) {
            buildJson.add(buildNumber.toString());
        }
        tree.put("builds", buildJson);

        JSONArray buildInfoJson = new JSONArray();
        for (Integer buildNumber : builds) {
            buildInfoJson.add(getBuildInfo(buildNumber, resultInfo));
        }
        tree.put("buildInfo", buildInfoJson);

        JSONArray results = new JSONArray();
        for (Map.Entry<String, ? extends Info> entry :
                resultInfo.getPackageResults().entrySet()) {
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

    /**
     * The number, display name, date and URL of a build. Only the number is known for builds whose details
     * were not recorded.
     */
    private static JSONObject getBuildInfo(Integer buildNumber, ResultInfo resultInfo) {
        JSONObject json = new JSONObject();
        json.put("number", buildNumber);
        BuildInfo build = resultInfo.getBuild(buildNumber);
        if (build != null) {
            json.put("displayName", build.displayName()); // dropped when null
            json.put("timestamp", build.timestamp());
            json.put("url", build.url());
        }
        return json;
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
        if (childrenInfo == null) return new JSONArray();

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

    /**
     * Writes the same JSON as {@link #getJsTree(List, ResultInfo, boolean)} straight to {@code out}, without
     * building a {@link JSONObject} per cell first: for many builds that tree is several times larger than
     * the serialized form, and {@code net.sf.json} copies nested objects as they are added.
     */
    public void writeJsTree(Writer out, List<Integer> builds, ResultInfo resultInfo, boolean hideConfigMethods)
            throws IOException {
        String[] buildNumbers = new String[builds.size()];
        out.write("{\"builds\":[");
        for (int i = 0; i < buildNumbers.length; i++) {
            buildNumbers[i] = JSONUtils.quote(builds.get(i).toString());
            if (i > 0) {
                out.write(',');
            }
            out.write(buildNumbers[i]);
        }
        out.write("],\"buildInfo\":[");
        for (int i = 0; i < buildNumbers.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            out.write(getBuildInfo(builds.get(i), resultInfo).toString());
        }
        out.write("],\"results\":[");
        boolean first = true;
        for (Info info : resultInfo.getPackageResults().values()) {
            if (!first) {
                out.write(',');
            }
            first = false;
            writeNode(out, builds, buildNumbers, info, hideConfigMethods);
        }
        out.write("]}");
    }

    private void writeNode(
            Writer out, List<Integer> builds, String[] buildNumbers, Info info, boolean hideConfigMethods)
            throws IOException {
        out.write("{\"text\":");
        out.write(JSONUtils.quote(info.getName()));
        out.write(",\"buildResults\":[");
        for (int i = 0; i < buildNumbers.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            out.write("{\"buildNumber\":");
            out.write(buildNumbers[i]);
            ResultData result = info.getBuildResult(builds.get(i));
            if (result == null) {
                out.write(",\"status\":\"N/A\"}");
            } else {
                out.write(",\"totalTests\":");
                out.write(Integer.toString(result.getTotalTests()));
                out.write(",\"totalFailed\":");
                out.write(Integer.toString(result.getTotalFailed()));
                out.write(",\"totalPassed\":");
                out.write(Integer.toString(result.getTotalPassed()));
                out.write(",\"totalSkipped\":");
                out.write(Integer.toString(result.getTotalSkipped()));
                out.write(",\"totalTimeTaken\":");
                out.write(JSONUtils.numberToString(result.getTotalTimeTaken()));
                out.write(",\"status\":");
                out.write(JSONUtils.quote(result.getStatus()));
                if (result.getUrl() != null) { // as JSONObject drops null values
                    out.write(",\"url\":");
                    out.write(JSONUtils.quote(result.getUrl()));
                }
                out.write('}');
            }
        }
        out.write("],\"children\":[");
        Map<String, ? extends Info> children = info.getChildren();
        if (children != null) {
            boolean first = true;
            for (Info child : children.values()) {
                if (hideConfigMethods && child.isConfig()) {
                    continue;
                }
                if (!first) {
                    out.write(',');
                }
                first = false;
                writeNode(out, builds, buildNumbers, child, hideConfigMethods);
            }
        }
        out.write("]}");
    }
}
