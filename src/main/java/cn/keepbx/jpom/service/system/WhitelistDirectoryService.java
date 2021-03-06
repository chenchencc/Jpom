package cn.keepbx.jpom.service.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.keepbx.jpom.common.BaseDataService;
import cn.keepbx.jpom.system.ConfigBean;
import cn.keepbx.jpom.util.JsonUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author jiangzeyin
 * @date 2019/2/28
 */
@Service
public class WhitelistDirectoryService extends BaseDataService {

    public JSONObject getWhitelist() {
        try {
            JSONObject jsonObject = getJSONObject(ConfigBean.WHITELIST_DIRECTORY);
            if (jsonObject == null) {
                return null;
            }
            return jsonObject;
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取项目路径白名单
     *
     * @return project
     */
    public JSONArray getProjectDirectory() {
        return getItemArray("project");
    }


    public JSONArray getNgxDirectory() {
        return getItemArray("nginx");
    }

    public JSONArray getCertificateDirectory() {
        return getItemArray("certificate");
    }

    public boolean checkProjectDirectory(String path) {
        return check(path, "project");
    }

    public boolean checkNgxDirectory(String path) {
        return check(path, "nginx");
    }

    public boolean checkCertificateDirectory(String path) {
        return check(path, "certificate");
    }

    private boolean check(String path, String key) {
        JSONArray jsonArray = getItemArray(key);
        if (jsonArray == null) {
            return false;
        }
        if (StrUtil.isEmpty(path)) {
            return false;
        }
        return jsonArray.contains(path);
    }

    private JSONArray getItemArray(String key) {
        try {
            JSONObject jsonObject = getWhitelist();
            if (jsonObject == null) {
                return null;
            }
            return jsonObject.getJSONArray(key);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 转换为字符串
     *
     * @param jsonArray jsonArray
     * @return str
     */
    public String convertToLine(JSONArray jsonArray) {
        try {
            return CollUtil.join(jsonArray, "\r\n");
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return "";
    }

    public void saveWhitelistDirectory(JSONObject jsonObject) {
        String path = getDataFilePath(ConfigBean.WHITELIST_DIRECTORY);
        JsonUtil.saveJson(path, jsonObject);
    }
}
