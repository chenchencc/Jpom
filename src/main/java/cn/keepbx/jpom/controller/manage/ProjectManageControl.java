package cn.keepbx.jpom.controller.manage;

import cn.hutool.core.util.StrUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import cn.keepbx.jpom.common.BaseController;
import cn.keepbx.jpom.common.commander.AbstractCommander;
import cn.keepbx.jpom.common.interceptor.ProjectPermission;
import cn.keepbx.jpom.model.ProjectInfoModel;
import cn.keepbx.jpom.model.UserModel;
import cn.keepbx.jpom.service.manage.ProjectInfoService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * 项目管理
 *
 * @author Administrator
 */
@Controller
@RequestMapping(value = "/manage/")
public class ProjectManageControl extends BaseController {

    @Resource
    private ProjectInfoService projectInfoService;

    /**
     * 展示项目页面
     *
     * @return page
     */
    @RequestMapping(value = "projectInfo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String projectInfo() throws IOException {
        //获取所有分组
        List<ProjectInfoModel> projectInfoModels = projectInfoService.list();
        HashSet<String> hashSet = new HashSet<>();
        for (ProjectInfoModel projectInfoModel : projectInfoModels) {
            hashSet.add(projectInfoModel.getGroup());
        }
        setAttribute("groups", hashSet);
        return "manage/projectInfo";
    }

    /**
     * 查询所有项目
     *
     * @return json
     */
    @RequestMapping(value = "getProjectInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getProjectInfo(String group) {
        try {
            UserModel userName = getUser();
            // 查询数据
            List<ProjectInfoModel> projectInfoModels = projectInfoService.list();
            // 转换为数据
            JSONArray array = new JSONArray();
            for (ProjectInfoModel projectInfoModel : projectInfoModels) {
                if (StrUtil.isNotEmpty(group) && !group.equals(projectInfoModel.getGroup())) {
                    continue;
                }
                String id = projectInfoModel.getId();
                JSONObject object = (JSONObject) JSONObject.toJSON(projectInfoModel);
                object.put("manager", userName.isProject(id));
                array.add(object);
            }
            array.sort((oo1, oo2) -> {
                JSONObject o1 = (JSONObject) oo1;
                JSONObject o2 = (JSONObject) oo2;
                String group1 = o1.getString("group");
                String group2 = o2.getString("group");
                if (group1 == null || group2 == null) {
                    return -1;
                }
                return group1.compareTo(group2);
            });
            return JsonMessage.getString(200, "查询成功！", array);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
            return JsonMessage.getString(500, e.getMessage());
        }
    }


    /**
     * 删除项目
     *
     * @param id id
     * @return json
     */
    @RequestMapping(value = "deleteProject", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ProjectPermission
    public String deleteProject(String id, String type) {
        ProjectInfoModel projectInfoModel = getProjectInfoModel();
        UserModel userModel = getUser();
        try {
            // 运行判断
            if (AbstractCommander.getInstance().isRun(projectInfoModel.getId())) {
                return JsonMessage.getString(401, "不能删除正在运行的项目");
            }
            String userId;
            if (UserModel.SYSTEM_ADMIN.equals(userModel.getParent())) {
                userId = UserModel.SYSTEM_OCCUPY_NAME;
            } else {
                userId = userModel.getId();
            }
            projectInfoService.deleteProject(projectInfoModel, userId);
            return JsonMessage.getString(200, "删除成功！");
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
            return JsonMessage.getString(500, e.getMessage());
        }
    }
}
