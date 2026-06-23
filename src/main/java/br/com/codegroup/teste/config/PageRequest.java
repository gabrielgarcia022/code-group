package br.com.codegroup.teste.config;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageRequest implements Pageable {

    private static final Integer DEFAULT_PAGE = 0;
    private static final Integer DEFAULT_SIZE = 10;

    private int page;
    private int size;
    private String orderBy;
    private String orderDirection;

    public PageRequest() {
        this.page = DEFAULT_PAGE;
        this.size = DEFAULT_SIZE;
        this.orderBy = "id";
        this.orderDirection = "DESC";
    }

    @Override
    public long getOffset() {
        return page * size;
    }

    @Override
    public int getPageNumber() {
        return page;
    }

    public OrderSpecifier getOrderBy(String table) {
        var orderByExpression = new PathBuilder<Object>(Object.class, table);

        return new OrderSpecifier(this.orderDirection.equalsIgnoreCase("ASC")
            ? Order.ASC
            : Order.DESC, orderByExpression.get(orderBy));
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public Sort getSort() {
        return Sort.by(
            Sort.Direction.fromString(Optional.ofNullable(this.orderDirection).orElse("DESC")),
            Optional.ofNullable(this.orderBy).orElse("id"));
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return null;
    }

    @Override
    public Pageable first() {
        return null;
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }
}
