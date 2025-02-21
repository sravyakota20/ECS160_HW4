package com.ecs160.hw2.persistence;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LazyLoad {
}
