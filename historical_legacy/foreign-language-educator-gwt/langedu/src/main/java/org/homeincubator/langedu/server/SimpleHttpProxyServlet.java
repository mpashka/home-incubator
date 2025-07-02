package org.homeincubator.langedu.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Проксируем запросы чтобы обработать редиректы и избежать проблем с originator domain
 *
 * todo [!] добавить поддержку обработки редиректов, content type и прочую хуитень
 */
public class SimpleHttpProxyServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(SimpleHttpProxyServlet.class.getName());

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.info("SimpleHttpProxyServlet.Init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.finest("Proxy request to OK...");

        Map<String, String[]> parameterMap = new HashMap<String, String[]>(req.getParameterMap());
        String url = parameterMap.remove("requestUrl")[0];
        ProxyUtils.proxyRequest(url, getRequestParams(parameterMap).toString(), resp.getOutputStream());
        resp.setContentType("application/json");
    }

    private StringBuilder getRequestParams(Map<String, ?> paramsSorted) {
        StringBuilder paramsString = new StringBuilder();
        for (Map.Entry<String, ?> stringEntry : paramsSorted.entrySet()) {
            if (paramsString.length() > 0) {
                paramsString.append('&');
            }
            paramsString.append(stringEntry.getKey() + '=');
            Object value = stringEntry.getValue();
            if (value instanceof String[]) {
                paramsString.append(((String[]) value)[0]);
            } else {
                paramsString.append(value.toString());
            }
        }
        return paramsString;
    }

}
