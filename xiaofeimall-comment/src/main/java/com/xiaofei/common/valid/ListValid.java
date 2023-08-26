package com.xiaofei.common.valid;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { ListValueConstraintValidator.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ListValid {

    String message() default "{com.xiaofei.common.valid.ListValid.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
    //数据类型, 返回数据的get方法, 默认值;
    int[] vals() default { };
}
