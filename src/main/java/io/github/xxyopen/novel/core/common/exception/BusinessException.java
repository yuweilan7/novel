package io.github.xxyopen.novel.core.common.exception;

import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常，用于处理用户请求时，业务错误时抛出
 *
 * @author xiongxiaoyang
 * @date 2022/5/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {

    private final ErrorCodeEnum errorCodeEnum;

    public BusinessException(ErrorCodeEnum errorCodeEnum) {
        /*
        当抛出异常时，JVM会将当前线程的栈信息记录下来，
        并将这个异常对象和栈信息保存到内存中。这个过程会占用一定的时间和空间，
        因此当系统中抛出异常较频繁时，会对系统的性能造成一定的影响。
        在这段代码中，通过在构造函数中传入 false 两个参数来告诉虚拟机
        不生成堆栈跟踪信息，提高应用性能。由于在这个业务异常类中，
        异常的信息已经足够详细了，不需要生成栈追踪信息。
        如果需要生成栈追踪信息，可以在打印日志时手动打印。
         */
        // 构造器之间的调用必须在第一行
        super(errorCodeEnum.getMessage(), null, false, false);
        this.errorCodeEnum = errorCodeEnum;
    }

}
