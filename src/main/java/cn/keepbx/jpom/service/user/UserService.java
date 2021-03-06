package cn.keepbx.jpom.service.user;

import cn.jiangzeyin.common.DefaultSystemLog;
import cn.keepbx.jpom.common.BaseOperService;
import cn.keepbx.jpom.model.UserModel;
import cn.keepbx.jpom.system.ConfigBean;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理
 *
 * @author Administrator
 */
@Service
public class UserService extends BaseOperService<UserModel> {

    /**
     * 用户列表是否为空
     *
     * @return true 为空需要初始化
     */
    public boolean userListEmpty() {
        return userSize() <= 0;
    }

    /**
     * 获取系统用户个数
     *
     * @return int
     */
    public int userSize() {
        try {
            JSONObject userInfo = getJSONObject(ConfigBean.USER);
            if (userInfo == null) {
                return 0;
            }
            return userInfo.keySet().size();
        } catch (Exception ignored) {
        }
        return 0;
    }

    /**
     * 用户登录
     *
     * @param name 用户名
     * @param pwd  密码
     * @return 登录
     */
    public UserModel simpleLogin(String name, String pwd) throws IOException {
        UserModel userModel = getItem(name);
        if (userModel == null) {
            return null;
        }
        if (pwd.equals(userModel.getPassword())) {
            return userModel;
        }
        return null;
    }

    /**
     * 验证用户md5
     *
     * @param userMd5 用户md5
     * @return userModel 用户对象
     */
    public UserModel checkUser(String userMd5) {
        JSONObject jsonData = getJSONObject(ConfigBean.USER);
        if (jsonData == null) {
            return null;
        }
        for (String strKey : jsonData.keySet()) {
            JSONObject jsonUser = jsonData.getJSONObject(strKey);
            UserModel userModel = jsonUser.toJavaObject(UserModel.class);
            String strUserMd5 = userModel.getUserMd5Key();
            if (strUserMd5.equals(userMd5)) {
                return userModel;
            }
        }
        return null;
    }

    /**
     * 获取用户列表
     *
     * @return 用户列表
     */
    @Override
    public List<UserModel> list() {
        JSONObject jsonObject = getJSONObject(ConfigBean.USER);
        if (jsonObject == null) {
            return null;
        }
        Set<Map.Entry<String, Object>> set = jsonObject.entrySet();
        List<UserModel> array = new ArrayList<>();
        for (Map.Entry entry : set) {
            JSONObject value = (JSONObject) entry.getValue();
            UserModel userModel = value.toJavaObject(UserModel.class);
            // 不显示系统管理员信息
            if (UserModel.SYSTEM_ADMIN.equals(userModel.getParent())) {
                continue;
            }
            userModel.setPassword("");
            array.add(userModel);
        }
        return array;
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    @Override
    public UserModel getItem(String userId) throws IOException {
        return getJsonObjectById(ConfigBean.USER, userId, UserModel.class);
    }

    /**
     * 删除用户
     *
     * @param id 用户id
     * @return String
     */
    public boolean deleteUser(String id) {
        try {
            deleteJson(ConfigBean.USER, id);
            return true;
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 添加用户
     *
     * @param userModel 实体
     */
    @Override
    public void addItem(UserModel userModel) {
        saveJson(ConfigBean.USER, userModel.toJson());
    }

    /**
     * 修改用户
     *
     * @return String
     */
    public boolean updateUser(UserModel userModel) {
        try {
            updateJson(ConfigBean.USER, userModel.toJson());
            return true;
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return false;
    }
}
