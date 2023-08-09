package edu.unimelb.jarvis.javafx;

import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.manager.font.FontProvider;
import io.vproxy.vfx.theme.impl.DarkTheme;
import io.vproxy.vfx.theme.impl.DarkThemeFontProvider;

public class MyTheme extends DarkTheme {

    public static final String COLOR_BACKGROUND = "#24292EFF";
    public static final String COLOR_TEXT = "#ECECECFF";
    public static final String SIZE_TEXT = "10pt";
    public static final String COLOR_BORDER = "#FFFFFF";

    @Override
    public FontProvider fontProvider() {
        return new IntroFontProvider();
    }

    public static class IntroFontProvider extends DarkThemeFontProvider {
        @Override
        protected String defaultFont() {
            return FontManager.FONT_NAME_JetBrainsMono;
        }
    }
}
