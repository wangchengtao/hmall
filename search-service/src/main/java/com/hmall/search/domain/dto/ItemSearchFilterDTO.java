package com.hmall.search.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ItemSearchFilterDTO {

    private List<String> category = new ArrayList<>();

    private List<String> brand = new ArrayList<>();


}
