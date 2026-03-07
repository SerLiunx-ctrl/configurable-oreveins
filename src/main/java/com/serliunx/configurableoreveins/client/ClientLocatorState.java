package com.serliunx.configurableoreveins.client;

import com.serliunx.configurableoreveins.network.LocatorVeinInfo;
import javax.annotation.Nullable;

/**
 * 客户端矿脉定位状态容器。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public final class ClientLocatorState {

    @Nullable private static LocatorVeinInfo highlightedVein;

    /** 构造 ClientLocatorState 实例。 */
    private ClientLocatorState() {}

    /**
     * 设置当前客户端高亮矿脉。
     *
     * @param vein 参数 vein。
    */
    public static void setHighlightedVein(@Nullable LocatorVeinInfo vein) {
        highlightedVein = vein;
    }

    /**
     * 获取当前客户端高亮矿脉。
     *
     * @return 处理结果。
    */
    @Nullable
    public static LocatorVeinInfo getHighlightedVein() {
        return highlightedVein;
    }
}
