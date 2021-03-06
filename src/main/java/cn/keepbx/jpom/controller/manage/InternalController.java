package cn.keepbx.jpom.controller.manage;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import cn.keepbx.jpom.common.BaseController;
import cn.keepbx.jpom.common.commander.AbstractCommander;
import cn.keepbx.jpom.model.ProjectInfoModel;
import cn.keepbx.jpom.service.manage.CommandService;
import cn.keepbx.jpom.service.manage.ProjectInfoService;
import cn.keepbx.jpom.socket.top.TopManager;
import cn.keepbx.jpom.system.ConfigBean;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.util.List;

/**
 * 内存查看
 *
 * @author Administrator
 */
@Controller
@RequestMapping(value = "/manage/")
public class InternalController extends BaseController {
    @Resource
    private ProjectInfoService projectInfoService;
    @Resource
    private CommandService commandService;

    /**
     * 获取内存信息
     */
    @RequestMapping(value = "internal", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getInternal(String tag) throws Exception {
        setAttribute("tag", tag);
        String pid = AbstractCommander.getInstance().getPid(tag);
        if (AbstractCommander.OS_INFO.isLinux()) {
            String command = "top -b -n 1 -p " + pid;
            String internal = AbstractCommander.getInstance().execCommand(command);
            JSONArray array = TopManager.formatLinuxTop(internal);
            if (null != array) {
                setAttribute("item", array.getJSONObject(0));
            }
        } else {
            String command = "tasklist /V /FI \"pid eq " + pid + "\"";
            String result = AbstractCommander.getInstance().execCommand(command);
            JSONArray array = TopManager.formatWindowsProcess(result);
            if (null != array) {
                setAttribute("item", array.getJSONObject(0));
            }
        }
        JSONObject beanMem = getBeanMem(tag);
        setAttribute("beanMem", beanMem);
        //获取端口信息
        JSONArray port = getPort(tag);
        setAttribute("port", port);
        return "manage/internal";
    }

    /**
     * 获取jvm内存
     *
     * @param tag tag
     * @return JSONObject
     */
    private JSONObject getBeanMem(String tag) {
        try {
            MemoryMXBean memoryMXBean = AbstractCommander.getInstance().getMemoryMXBean(tag);
            if (memoryMXBean == null) {
                return null;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mount", memoryMXBean.getObjectPendingFinalizationCount());
            //堆内存
            MemoryUsage memory = memoryMXBean.getHeapMemoryUsage();
            //非堆内存
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            long used = memory.getUsed();
            long max = memory.getMax();
            //未定义
            if (-1 == max) {
                max = memory.getCommitted();
            }
            //计算使用内存占最大内存的百分比
            double v = new BigDecimal(used).divide(new BigDecimal(max), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            jsonObject.put("heapUsed", FileUtil.readableFileSize(used));
            jsonObject.put("heapProportion", String.format("%.2f", v) + "%");
            jsonObject.put("heapCommitted", FileUtil.readableFileSize(memory.getCommitted()));
            long nonUsed = nonHeapMemoryUsage.getUsed();
            long nonMax = nonHeapMemoryUsage.getMax();
            long nonCommitted = nonHeapMemoryUsage.getCommitted();
            if (-1 == nonMax) {
                nonMax = nonCommitted;
            }
            jsonObject.put("nonHeapUsed", FileUtil.readableFileSize(nonUsed));
            double proportion = new BigDecimal(nonUsed).divide(new BigDecimal(nonMax), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            jsonObject.put("nonHeapProportion", String.format("%.2f", proportion) + "%");
            jsonObject.put("nonHeapCommitted", FileUtil.readableFileSize(nonCommitted));
            return jsonObject;
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 导出堆栈信息
     */
    @RequestMapping(value = "stack", method = RequestMethod.GET)
    @ResponseBody
    public String stack(String tag) throws Exception {
        String fileName = ConfigBean.getInstance().getTempPathName() + "/" + tag + "_java_cpu.txt";
        fileName = FileUtil.normalize(fileName);
        try {
            String pid = AbstractCommander.getInstance().getPid(tag);
            String command = String.format("jstack %s >> %s ", pid, fileName);
            AbstractCommander.getInstance().execSystemCommand(command);
            downLoad(getResponse(), fileName);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
            getResponse().sendRedirect("internal?tag=" + tag);
        }
        return JsonMessage.getString(200, "");
    }

    /**
     * 导出内存信息
     */
    @RequestMapping(value = "ram", method = RequestMethod.GET)
    @ResponseBody
    public String ram(String tag) throws Exception {
        String fileName = ConfigBean.getInstance().getTempPathName() + "/" + tag + "_java_ram.txt";
        fileName = FileUtil.normalize(fileName);
        try {
            String pid = AbstractCommander.getInstance().getPid(tag);
            String command = String.format("jmap -histo:live %s >> %s", pid, fileName);
            AbstractCommander.getInstance().execSystemCommand(command);
            downLoad(getResponse(), fileName);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
            getResponse().sendRedirect("internal?tag=" + tag);
        }
        return JsonMessage.getString(200, "");
    }

    /**
     * 下载文件
     *
     * @param response response
     * @param fileName 文件名字
     */
    private void downLoad(HttpServletResponse response, String fileName) {
        //获取项目根路径
        File file = new File(fileName);
        ServletUtil.write(response, file);
        FileUtil.del(file);
    }

    /**
     * 获取端口
     *
     * @param tag 项目id
     */
    private JSONArray getPort(String tag) {
        // 查询数据
        try {
            ProjectInfoModel projectInfoModel = projectInfoService.getItem(tag);
            String pId = commandService.execCommand(CommandService.CommandOp.pid, projectInfoModel).trim();
            if (StrUtil.isNotEmpty(pId)) {
                String cmd;
                boolean isLinux = true;
                if (AbstractCommander.OS_INFO.isLinux()) {
                    cmd = "netstat -antup | grep " + pId + " |grep -v \"CLOSE_WAIT\" | head -10";
                } else {
                    isLinux = false;
                    cmd = "netstat -nao -p tcp | findstr /V \"CLOSE_WAIT\" | findstr " + pId;
                }
                String result = AbstractCommander.getInstance().execSystemCommand(cmd);
                return formatRam(isLinux, result);
            }
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error(e.getMessage(), e);
        }
        return null;
    }

    private JSONArray formatRam(boolean isLinux, String result) {
        List<String> netList = StrSpliter.splitTrim(result, "\n", true);
        if (netList == null || netList.size() <= 0) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (String str : netList) {
            List<String> list = StrSpliter.splitTrim(str, " ", true);
            if (list.size() < 5) {
                continue;
            }
            JSONObject item = new JSONObject();
            if (isLinux) {
                item.put("protocol", list.get(0));
                item.put("receive", list.get(1));
                item.put("send", list.get(2));
                item.put("local", list.get(3));
                item.put("foreign", list.get(4));
                item.put("status", list.get(5));
                item.put("name", list.get(6));
            } else {
                item.put("protocol", list.get(0));
                item.put("receive", 0);
                item.put("send", 0);
                item.put("local", list.get(1));
                item.put("foreign", list.get(2));
                item.put("status", list.get(3));
                item.put("name", list.get(4));
            }
            array.add(item);
        }
        return array;
    }
}
