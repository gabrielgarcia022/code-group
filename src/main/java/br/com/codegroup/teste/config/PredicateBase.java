package br.com.codegroup.teste.config;

import com.querydsl.core.BooleanBuilder;
import org.springframework.util.ObjectUtils;
import org.thymeleaf.util.ListUtils;
import org.thymeleaf.util.StringUtils;
import java.util.List;

public class PredicateBase {

    protected BooleanBuilder builder;

    public PredicateBase() {
        this.builder = new BooleanBuilder();
    }

    public BooleanBuilder build() {
        return this.builder;
    }

    protected boolean isEmpty(Object object) {
        return ObjectUtils.isEmpty(object);
    }

    protected boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    protected boolean isEmpty(List<?> list) {
        return ListUtils.isEmpty(list);
    }

    protected boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    public void add(BooleanBuilder booleanBuilder) {
        builder.and(booleanBuilder);
    }
}

