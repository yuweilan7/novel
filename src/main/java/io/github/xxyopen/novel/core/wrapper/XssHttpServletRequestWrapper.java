package io.github.xxyopen.novel.core.wrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 *使用装饰者模式解决表单形式传参的XXS攻击
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Map<String, String> REPLACE_RULE = new HashMap<>();

    static {
        REPLACE_RULE.put("<", "&lt;");
        REPLACE_RULE.put(">", "&gt;");
    }

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapeValues = new String[length];
            for (int i = 0; i < length; i++) {
                escapeValues[i] = values[i];
                int index = i;
                /**
                 * 对 escapeValues 数组中的每个值执行替换规则。k 代表需要替换的字符，v 代表替换后的字符串，
                 * replaceAll() 方法用于将所有出现的 k 替换为 v。
                 */
                REPLACE_RULE.forEach(
                    (k, v) -> escapeValues[index] = escapeValues[index].replaceAll(k, v));
            }
            return escapeValues;
        }
        return new String[0];
    }
}
