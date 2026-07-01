package com.wjh.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResult implements Serializable {

    private Long total;
    private List records;
    private Integer pages;
    private Integer pageSize;

}
