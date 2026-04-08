package com.jiejie.rpc.core.annotation;

import java.lang.annotation.*;

/**
 * 客户端注解：标注在接口字段上，Spring 启动时会自动注入该接口的代理对象
 */
@Target(ElementType.FIELD) // 作用在字段上
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
}