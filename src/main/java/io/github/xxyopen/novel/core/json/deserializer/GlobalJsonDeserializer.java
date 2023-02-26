package io.github.xxyopen.novel.core.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;


/**
 * JSON 全局反序列化器
 *
 使用 @JsonComponent 注解，可以将一个类或者接口标记为 JSON 组件，
 从而告诉 Spring 该类或接口需要参与 JSON 序列化或反序列化的过程。
 这些自定义的组件可以用来处理复杂的 JSON 序列化和反序列化需求，
 比如格式化日期、处理嵌套对象、支持自定义的数据类型等。
 */
@JsonComponent
public class GlobalJsonDeserializer {

    /**
     * 字符串反序列化器：过滤特殊字符，解决 XSS 攻击
     */
    public static class StringDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
            return jsonParser.getValueAsString()
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        }
    }
}
