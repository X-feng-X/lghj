package com.lghj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "股票实时资讯VO")
public class StockNewsVO implements Serializable {

    @ApiModelProperty("关键词")
    private String keyword;

    @ApiModelProperty("新闻标题")
    private String title;

    @ApiModelProperty("新闻内容")
    private String content;

    @ApiModelProperty("发布时间")
    private String publishTime;

    @ApiModelProperty("文章来源")
    private String source;

    @ApiModelProperty("新闻链接")
    private String url;
}
