package com.xiaofei.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValid, Integer> {

    private Set<Integer> hashset = new HashSet<>();
    //初始化方法,先执行这个方法,在执行下面的方法
    @Override
    public void initialize(ListValid constraintAnnotation) {
        //这是在程序员定义有效的范围
        int[] vals = constraintAnnotation.vals();
        for (Integer val:vals) {
            hashset.add(val);
        }

    }
    //这个方法的作用是:返回是否校验成功;
    @Override
    //这里的Internet是用户从前段传入的值
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return hashset.contains(value);
    }
}
