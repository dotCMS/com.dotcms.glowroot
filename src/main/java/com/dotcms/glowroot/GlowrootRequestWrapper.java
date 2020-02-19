package com.dotcms.glowroot;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class GlowrootRequestWrapper extends HttpServletRequestWrapper {

    public GlowrootRequestWrapper(HttpServletRequest request) {
        super(request);

    }

    @Override
    public String getContextPath() {
        return "/glowroot";
    }
}
