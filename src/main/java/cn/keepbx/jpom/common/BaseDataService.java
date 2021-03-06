package cn.keepbx.jpom.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.keepbx.jpom.system.ConfigBean;
import cn.keepbx.jpom.util.JsonUtil;
import com.alibaba.fastjson.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 公共文件操作Service
 *
 * @author jiangzeyin
 * @date 2019/1/16
 */
public abstract class BaseDataService {

    /**
     * 获取数据文件的路径，如果文件不存在，则创建一个
     *
     * @param filename 文件名
     * @return path
     */
    protected String getDataFilePath(String filename) {
        return FileUtil.normalize(ConfigBean.getInstance().getDataPath() + "/" + filename);
    }

    /**
     * 保存json对象
     *
     * @param filename 文件名
     * @param json     json数据
     */
    protected void saveJson(String filename, JSONObject json) {
        String key = json.getString("id");
        // 读取文件，如果存在记录，则抛出异常
        JSONObject allData;
        JSONObject data = null;
        allData = getJSONObject(filename);
        if (allData != null) {
            data = allData.getJSONObject(key);
        } else {
            allData = new JSONObject();
        }
        // 判断是否存在数据
        if (null != data && 0 < data.keySet().size()) {
            throw new RuntimeException("数据Id已经存在啦：" + filename + " :" + key);
        } else {
            allData.put(key, json);
            JsonUtil.saveJson(getDataFilePath(filename), allData);
        }
    }

    /**
     * 修改json对象
     *
     * @param filename 文件名
     * @param json     json数据
     */
    protected void updateJson(String filename, JSONObject json) throws Exception {
        String key = json.getString("id");
        // 读取文件，如果不存在记录，则抛出异常
        JSONObject allData = getJSONObject(filename);
        JSONObject data = allData.getJSONObject(key);

        // 判断是否存在数据
        if (null == data || 0 == data.keySet().size()) {
            throw new Exception("数据不存在:" + key);
        } else {
            allData.put(key, json);
            JsonUtil.saveJson(getDataFilePath(filename), allData);
        }
    }

    /**
     * 删除json对象
     *
     * @param filename 文件
     * @param key      key
     * @throws Exception 异常
     */
    protected void deleteJson(String filename, String key) throws Exception {
        // 读取文件，如果存在记录，则抛出异常
        JSONObject allData = getJSONObject(filename);
        JSONObject data = allData.getJSONObject(key);
        // 判断是否存在数据
        if (JsonUtil.jsonIsEmpty(data)) {
            throw new Exception("项目名称存不在！");
        } else {
            allData.remove(key);
            JsonUtil.saveJson(getDataFilePath(filename), allData);
        }
    }

    /**
     * 读取整个json文件
     *
     * @param filename 文件名
     * @return json
     */
    protected JSONObject getJSONObject(String filename) {
        try {
            return (JSONObject) JsonUtil.readJson(getDataFilePath(filename));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    protected <T> T getJsonObjectById(String file, String id, Class<T> cls) throws IOException {
        if (StrUtil.isEmpty(id)) {
            return null;
        }
        JSONObject jsonObject = getJSONObject(file);
        if (jsonObject == null) {
            return null;
        }
        jsonObject = jsonObject.getJSONObject(id);
        if (jsonObject == null) {
            return null;
        }
        return jsonObject.toJavaObject(cls);
    }
}
