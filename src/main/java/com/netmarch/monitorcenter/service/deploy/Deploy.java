package com.netmarch.monitorcenter.service.deploy;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Deploy {
}
