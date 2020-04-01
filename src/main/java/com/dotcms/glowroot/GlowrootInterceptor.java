package com.dotcms.glowroot;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotcms.glowroot.proxy.MockHttpCaptureResponse;
import com.dotcms.glowroot.proxy.ProxyResponse;
import com.dotcms.glowroot.proxy.ProxyTool;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import io.vavr.control.Try;

public class GlowrootInterceptor implements WebInterceptor {


    private static final long serialVersionUID = 1L;
    private static final String GLOWROOT_URI = "/glowroot";
    private static final ProxyTool proxy = new ProxyTool();


    @Override
    public String[] getFilters() {
        return new String[] {GLOWROOT_URI};
    }

    @Override
    public Result intercept(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final Optional<String> proxyUrl = proxyUrl(request);
        
        
        User user = PortalUtil.getUser(request);
        if(user == null || ! Try.of(()-> APILocator.getUserAPI().isCMSAdmin(user)).getOrElse(false)) {
            return Result.NEXT;
        }
        

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().entrySet().forEach(
                        e -> params.put(e.getKey(), (e.getValue() != null && e.getValue().length > 0) ? e.getValue()[0] : null));


        Map<String, String> urlParams = new HashMap<>();
        request.getParameterMap().entrySet().forEach(e -> params.put(e.getKey(),
                        (e.getValue() != null && e.getValue().length > 0) ? URLEncoder.encode(e.getValue()[0]) : null));

        ProxyResponse pResponse = null;
        if (GLOWROOT_URI.equals(request.getRequestURI())) {
            response.sendRedirect(GLOWROOT_URI + "/");
            return Result.SKIP_NO_CHAIN;
        }
        if (rewriteBaseHref(request)) {

            pResponse = proxy.sendGet(proxyUrl.get(), params);
            String strResponse = new String(pResponse.getResponse()).replace("<base href=\"/\">",
                            "<base href=\"" + GLOWROOT_URI + "/\"><script>var contextPath='" + GLOWROOT_URI + "/'</script>");
            response.getWriter().write(strResponse);
            return Result.SKIP_NO_CHAIN;
        }


        Logger.debug(this.getClass(), "GOT AN Glowroot Call -->" + request.getRequestURI() + " --> " + proxyUrl.get());



        if ("GET".equalsIgnoreCase(request.getMethod())) {
            pResponse = proxy.sendGet(proxyUrl.get(), params);
        } else if ("POST".equalsIgnoreCase(request.getMethod())) {
            pResponse = proxy.sendPost(proxyUrl.get(), params);
        } else if ("PUT".equalsIgnoreCase(request.getMethod())) {
            pResponse = proxy.sendPut(proxyUrl.get(), params);
        } else if ("DELETE".equalsIgnoreCase(request.getMethod())) {
            pResponse = proxy.sendDelete(proxyUrl.get(), params);
        }

        if (pResponse.getResponseCode() == 200) {
            response.getOutputStream().write(pResponse.getResponse());
        } else {
            response.sendError(pResponse.getResponseCode());
        }


        return Result.SKIP_NO_CHAIN;
    }


    @Override
    public boolean afterIntercept(final HttpServletRequest request, final HttpServletResponse response) {

        try {

            if (response instanceof MockHttpCaptureResponse) {

                final Optional<String> proxyUrl = proxyUrl(request);
                final MockHttpCaptureResponse mockResponse = (MockHttpCaptureResponse) response;



                response.getOutputStream().write(mockResponse.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    private Optional<String> proxyUrl(HttpServletRequest request) {

        final String incoming = request.getRequestURI().replace(GLOWROOT_URI, "");

        if (incoming.endsWith(".js") || incoming.endsWith(".css") || incoming.endsWith(".js") || incoming.endsWith(".woff2")
                        || incoming.endsWith(".ico") || incoming.startsWith("/backend")) {
           // return Optional.ofNullable("http://localhost:4000" + incoming);
        }

        return Optional.ofNullable("http://localhost:4000" + incoming);


    }

    private boolean rewriteBaseHref(HttpServletRequest request) {

        final String incoming = request.getRequestURI().replace(GLOWROOT_URI, "");

        if (incoming.endsWith(".js") || incoming.endsWith(".css") || incoming.endsWith(".js") || incoming.endsWith(".woff2")
                        || incoming.endsWith(".ico") || incoming.startsWith("/backend")) {
           return false;
        }

        return true;

    }

}
