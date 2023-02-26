package io.github.xxyopen.novel.core.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 流量限制 拦截器：实现接口防刷和限流
 *
 * @author xiongxiaoyang
 * @date 2022/6/1
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FlowLimitInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    /**
     * novel 项目所有的资源
     */
    private static final String NOVEL_RESOURCE = "novelResource";

    static {
        /**
         * 流控规则有三种控制行为（ControlBehavior）：
         * 直接拒绝（REJECT）、冷启动（WARM_UP）、匀速排队（RATE_LIMITER）
         */
        /**
         * FlowRule用来限制整个应用程序中的资源访问，比如说对一个接口进行限流，限制整个系统中对该接口的并发调用量。
         * FlowRule可以对指定的资源进行流量控制，也可以对系统中所有资源进行控制。
         * 而ParamFlowRule则是在FlowRule的基础上，增加了对参数级别的流量控制。
         * 它可以实现对一些参数敏感的接口进行限流，比如说根据访问接口的用户的IP地址、
         * 请求参数等对请求进行限流。
         */
        // 接口限流规则：所有的请求，限制每秒最多只能通过 2000 个，超出限制匀速排队
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(NOVEL_RESOURCE);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 2000.
        rule1.setCount(2000);
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
        /**
         * rule2和rule3都是基于参数的流量控制规则，它们的限制条件都是基于第一个参数（paramIdx=0），
         * 也就是请求的IP地址
         */
        // 接口防刷规则 1：所有的请求，限制每个 IP 每秒最多只能通过 50 个，超出限制直接拒绝
        ParamFlowRule rule2 = new ParamFlowRule(NOVEL_RESOURCE)
            .setParamIdx(0)
            .setCount(50);
        // 接口防刷规则 2：所有的请求，限制每个 IP 每分钟最多只能通过 1000 个，超出限制直接拒绝
        ParamFlowRule rule3 = new ParamFlowRule(NOVEL_RESOURCE)
            .setParamIdx(0)
            .setCount(1000)
            .setDurationInSec(60);
        ParamFlowRuleManager.loadRules(Arrays.asList(rule2, rule3));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        String ip = IpUtils.getRealIp(request);
        Entry entry = null;
        try {
            // 若需要配置例外项，则传入的参数只支持基本类型。
            // EntryType 代表流量类型，其中系统规则只对 IN 类型的埋点生效
            // count 大多数情况都填 1，代表统计为一次调用。
            entry = SphU.entry(NOVEL_RESOURCE, EntryType.IN, 1, ip);
            // Your logic here.
            return HandlerInterceptor.super.preHandle(request, response, handler);
        } catch (BlockException ex) {
            // Handle request rejection.
            log.info("IP:{}被限流了！", ip);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter()
                .write(objectMapper.writeValueAsString(RestResp.fail(ErrorCodeEnum.USER_REQ_MANY)));
        } finally {
            // 注意：exit 的时候也一定要带上对应的参数，否则可能会有统计错误。
            if (entry != null) {
                entry.exit(1, ip);
            }
        }
        return false;
    }

}
