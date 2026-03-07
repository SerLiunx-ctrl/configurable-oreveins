package com.serliunx.configurableoreveins.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 矿脉 JSON 根配置对象。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class ModConfigData {
    private List<VeinDefinition> veins = new ArrayList<>();

    /**
     * 获取当前矿脉配置列表。
     *
     * @return 处理结果。
    */
    public List<VeinDefinition> getVeins() {
        return veins == null ? new ArrayList<VeinDefinition>() : veins;
    }

    /**
     * 设置 Veins。
     *
     * @param veins 参数 veins。
    */
    public void setVeins(List<VeinDefinition> veins) {
        this.veins = veins;
    }
}
