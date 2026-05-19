package cn.zhongjiating.canteen.common.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PageResult<T> implements Serializable {

    private List<T> records;

    private Long total;

    private Long size;

    private Long current;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long size, Long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        return new PageResult<>(records, total, size, current);
    }

    public static <T> PageResult<T> empty(Long size, Long current) {
        return new PageResult<>(Collections.emptyList(), 0L, size, current);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }
}
