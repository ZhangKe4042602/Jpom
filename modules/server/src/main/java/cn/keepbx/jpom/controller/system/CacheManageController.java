package cn.keepbx.jpom.controller.system;

import cn.hutool.core.io.FileUtil;
import cn.jiangzeyin.common.JsonMessage;
import cn.jiangzeyin.common.validator.ValidatorItem;
import cn.jiangzeyin.common.validator.ValidatorRule;
import cn.keepbx.build.BuildUtil;
import cn.keepbx.jpom.common.BaseServerController;
import cn.keepbx.jpom.common.forward.NodeForward;
import cn.keepbx.jpom.common.forward.NodeUrl;
import cn.keepbx.jpom.common.interceptor.UrlPermission;
import cn.keepbx.jpom.controller.LoginControl;
import cn.keepbx.jpom.model.Role;
import cn.keepbx.jpom.model.log.UserOperateLogV1;
import cn.keepbx.jpom.socket.ServiceFileTailWatcher;
import cn.keepbx.jpom.system.ConfigBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

/**
 * 缓存管理
 *
 * @author bwcx_jzy
 * @date 2019/7/20
 */
@Controller
@RequestMapping(value = "system")
public class CacheManageController extends BaseServerController {

    @RequestMapping(value = "cache.html", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String cache() {
        if (tryGetNode() == null) {
            //
            File file = ConfigBean.getInstance().getTempPath();
            String fileSize = FileUtil.readableFileSize(FileUtil.size(file));
            setAttribute("cacheFileSize", fileSize);

            int size = LoginControl.LFU_CACHE.size();
            setAttribute("ipSize", size);
            int oneLineCount = ServiceFileTailWatcher.getOneLineCount();
            setAttribute("readFileOnLineCount", oneLineCount);

            File buildDataDir = BuildUtil.getBuildDataDir();
            fileSize = FileUtil.readableFileSize(FileUtil.size(buildDataDir));
            setAttribute("cacheBuildFileSize", fileSize);
        }
        return "system/cache";
    }

    /**
     * 获取节点中的缓存
     *
     * @return json
     */
    @RequestMapping(value = "node_cache.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String nodeCache() {
        return NodeForward.request(getNode(), getRequest(), NodeUrl.Cache).toString();
    }

    /**
     * 清空缓存
     *
     * @return json
     */
    @RequestMapping(value = "clearCache.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @UrlPermission(value = Role.System, optType = UserOperateLogV1.OptType.ClearCache)
    public String clearCache(@ValidatorItem(value = ValidatorRule.NOT_BLANK, msg = "类型错误") String type) {
        switch (type) {
            case "serviceCacheFileSize":
                boolean clean = FileUtil.clean(ConfigBean.getInstance().getTempPath());
                if (!clean) {
                    return JsonMessage.getString(504, "清空文件缓存失败");
                }
                break;
            case "serviceIpSize":
                LoginControl.LFU_CACHE.clear();
                break;
            default:
                return NodeForward.request(getNode(), getRequest(), NodeUrl.ClearCache).toString();

        }
        return JsonMessage.getString(200, "清空成功");
    }
}