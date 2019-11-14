package io.jpom.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

/**
 * @author bwcx_jzy
 * @date 2019/11/14
 */
public class UrlRedirectUtil {

    /**
     * 获取 protocol 协议完全跳转
     *
     * @param request  请求
     * @param response 响应
     * @param url      跳转url
     * @throws IOException io
     * @see javax.servlet.http.HttpUtils#getRequestURL
     */
    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url, int port) throws IOException {
        String proto = ServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Proto");
        if (proto == null) {
            response.sendRedirect(url);
        } else {
            String host = request.getHeader(HttpHeaders.HOST);
            if (StrUtil.isEmpty(host)) {
                throw new RuntimeException("请配置host header");
            }
            if ("http".equals(proto) && port == 0) {
                port = 80;
            } else if ("https".equals(proto) && port == 0) {
                port = 443;
            }
            String toUrl = StrUtil.format("{}://{}:{}{}", proto, host, port, url);
            response.sendRedirect(toUrl);
        }
    }


    /**
     * 获取 protocol 协议完全跳转
     *
     * @param request  请求
     * @param response 响应
     * @param url      跳转url
     * @throws IOException io
     * @see javax.servlet.http.HttpUtils#getRequestURL
     */
    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String proxyPort = ServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Port");
        int port = 0;
        if (StrUtil.isNotEmpty(proxyPort)) {
            port = Integer.parseInt(proxyPort);
        }
        sendRedirect(request, response, url, port);
    }

    /**
     * 二级代理路径
     *
     * @param request req
     * @return context-path+nginx配置
     */
    public static String getHeaderProxyPath(HttpServletRequest request, String headName) {
        return getHeaderProxyPath(request, headName, null);
    }

    /**
     * 二级代理路径
     *
     * @param request req
     * @return context-path+nginx配置
     */
    public static String getHeaderProxyPath(HttpServletRequest request, String headName, Function<String, String> function) {
        String proxyPath = ServletUtil.getHeaderIgnoreCase(request, headName);
        //
        if (StrUtil.isEmpty(proxyPath)) {
            return request.getContextPath();
        }
        // 回调处理
        if (function != null) {
            proxyPath = function.apply(proxyPath);
        }
        //
        proxyPath = FileUtil.normalize(request.getContextPath() + StrUtil.SLASH + proxyPath);
        if (proxyPath.endsWith(StrUtil.SLASH)) {
            proxyPath = proxyPath.substring(0, proxyPath.length() - 1);
        }
        return proxyPath;
    }
}