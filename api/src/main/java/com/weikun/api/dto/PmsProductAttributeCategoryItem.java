package com.weikun.api.dto;


import com.weikun.api.model.PmsProductAttribute;
import com.weikun.api.model.PmsProductAttributeCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 包含有分类下属性的dto
 */
@Data
public class PmsProductAttributeCategoryItem extends PmsProductAttributeCategory implements Serializable {



    private List<PmsProductAttribute> productAttributeList;//每个种类下都有多个PmsProductAttribute


}
