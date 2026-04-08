package com.jiejie.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * 服务端注解：标注在实现类上，表示该类是一个需要暴露的 RPC 服务
 */
@Target(ElementType.TYPE) // 作用在类上
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcService {
    /** 暴露服务的接口 Class */
    Class<?> interfaceClass() default Object.class;
}