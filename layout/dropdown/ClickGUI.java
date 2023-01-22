/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.input.Cursor
 *  org.lwjgl.input.Mouse
 */
package vip.astroline.client.layout.dropdown;

import java.awt.Color;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import optifine.Config;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import vip.astroline.client.Astroline;
import vip.astroline.client.layout.dropdown.components.Component;
import vip.astroline.client.layout.dropdown.components.impl.Box;
import vip.astroline.client.layout.dropdown.components.impl.Checkbox;
import vip.astroline.client.layout.dropdown.components.impl.ColorPicker;
import vip.astroline.client.layout.dropdown.components.impl.ComboBox;
import vip.astroline.client.layout.dropdown.components.impl.Label;
import vip.astroline.client.layout.dropdown.components.impl.ModeLabel;
import vip.astroline.client.layout.dropdown.components.impl.Spacer;
import vip.astroline.client.layout.dropdown.components.impl.config.PresetList;
import vip.astroline.client.layout.dropdown.components.impl.module.ModuleButton;
import vip.astroline.client.layout.dropdown.components.impl.slider.Slider;
import vip.astroline.client.layout.dropdown.panel.Panel;
import vip.astroline.client.layout.dropdown.utils.BlurUtil;
import vip.astroline.client.service.font.FontManager;
import vip.astroline.client.service.module.Category;
import vip.astroline.client.service.module.Module;
import vip.astroline.client.service.module.impl.render.Hud;
import vip.astroline.client.service.module.value.BooleanValue;
import vip.astroline.client.service.module.value.ColorValue;
import vip.astroline.client.service.module.value.FloatValue;
import vip.astroline.client.service.module.value.ModeValue;
import vip.astroline.client.service.module.value.Value;
import vip.astroline.client.service.module.value.ValueManager;

public class ClickGUI
extends GuiScreen {
    public List<Panel> panels = new ArrayList<Panel>();
    private int mouseX = 0;
    private int mouseY = 0;
    public static int defaultWidth = 90;
    public static int settingsWidth = 90;
    public static int defaultHeight = 12;
    public static int buttonHeight = 13;
    public static int scrollbarWidth = 2;
    public static int scrollbarHeight = 100;
    public static int maxWindowHeight = 330;
    public static int backgroundColor = -14145496;
    public static int mainColor = Hud.hudColor1.getColorInt();
    public static int scrollbarColor = new Color(150, 150, 150, 180).getRGB();
    public static int lightBackgroundColor = new Color(255, 255, 255, 175).getRGB();
    public static int lightMainColor = new Color(29, 160, 255).getRGB();
    public NumberFormat nf = new DecimalFormat("0000");
    private ColorPicker currentColourPicker;

    public ClickGUI() {
        this.allowUserInput = true;
        this.setup();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!Hud.NoShader.getValueState() && !Config.isFastRender()) {
            BlurUtil.blurAll(3.0f);
        }
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.update();
        this.render();
        float xx = (float)mouseX - 0.5f;
        float yy = (float)mouseY - 0.5f;
        FontManager.icon18.drawString("p", xx + 0.6f - 1.0f, yy + 0.3f, -16777216);
        FontManager.icon18.drawString("p", xx - 1.0f, yy, Hud.hudColor1.getColorInt());
    }

    @Override
    public void handleInput() throws IOException {
        if (!Mouse.isCreated()) {
            return;
        }
        while (Mouse.next()) {
            this.handleMouseInput();
        }
    }

    public void update() {
        this.panels.forEach(w -> w.update(this.mouseX, this.mouseY));
        this.panels.forEach(w -> w.handleMouseUpdates(this.mouseX, this.mouseY, Mouse.isButtonDown((int)0) || Mouse.isButtonDown((int)1)));
    }

    public int rainbow(int delay) {
        double rainbow = Math.ceil((double)(System.currentTimeMillis() + (long)delay) / 10.0);
        return Color.getHSBColor((float)(rainbow % 360.0 / 360.0), 0.35f, 1.0f).getRGB();
    }

    @Override
    public void onGuiClosed() {
        this.panels.forEach(w -> w.feedTimer.reset());
        try {
            Mouse.setNativeCursor(null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initGui() {
        try {
            int min = Cursor.getMinCursorSize();
            IntBuffer tmp = BufferUtils.createIntBuffer((int)(min * min));
            Cursor emptyCursor = new Cursor(min, min, min / 2, min / 2, 1, tmp, null);
            Mouse.setNativeCursor((Cursor)emptyCursor);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (Mouse.getEventDWheel() != 0) {
            this.panels.forEach(w -> w.handleWheelUpdates(this.mouseX, this.mouseY, Mouse.isButtonDown((int)0)));
        } else {
            this.panels.forEach(Panel::noWheelUpdates);
        }
        super.handleMouseInput();
    }

    public void render() {
        this.panels.stream().filter(w -> w.isEnabled).forEach(w -> w.render(this.mouseX, this.mouseY));
        if (this.currentColourPicker != null && this.currentColourPicker.isExpanded()) {
            this.currentColourPicker.renderColourPicker();
        }
    }

    public void setCurrentColourPicker(ColorPicker colourPicker) {
        this.currentColourPicker = colourPicker;
    }

    public ColorPicker getCurrentColourPicker() {
        return this.currentColourPicker;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.getCurrentColourPicker() != null && !this.getCurrentColourPicker().isHoveredPicker(mouseX, mouseY)) {
            this.setCurrentColourPicker(null);
        }
        if (this.getCurrentColourPicker() != null) {
            this.getCurrentColourPicker().mouseHeldColour(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.getCurrentColourPicker() != null) {
            this.getCurrentColourPicker().mouseHeldColour(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.getCurrentColourPicker() != null) {
            this.getCurrentColourPicker().mouseReleased();
        }
    }

    public void rePositionWindows() {
        int maxHeight = ((Panel[])this.panels.stream().sorted(Comparator.comparing((Function<Panel, Integer>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, getHeight(), (Lvip/astroline/client/layout/dropdown/panel/Panel;)Ljava/lang/Integer;)()).reversed()).toArray((IntFunction<Panel[]>)LambdaMetafactory.metafactory(null, null, null, (I)Ljava/lang/Object;, lambda$rePositionWindows$6(int ), (I)[Lvip/astroline/client/layout/dropdown/panel/Panel;)()))[0].height;
        int y = 10;
        int x = 10;
        for (Panel panel : this.panels) {
            panel.x = x;
            panel.y = y;
            x += panel.width + 5;
        }
        this.panels.forEach(Panel::repositionComponents);
    }

    public void setup() {
        this.panels.clear();
        for (Category category : Category.values()) {
            if (category == Category.Global) continue;
            Panel modulePanel = new Panel(category.name(), 0, 0);
            modulePanel.children.add(new Spacer(modulePanel, 0, 0, 3));
            Box lastBox = null;
            for (Module module : Astroline.INSTANCE.moduleManager.getModules().stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList())) {
                if (module.getCategory() != category) continue;
                modulePanel.children.add(new ModuleButton(modulePanel, 0, 0, module.getName(), "", module));
                if (ValueManager.getValueByModName(module.getName()).isEmpty()) continue;
                Box box = new Box(modulePanel, 0, 0, module.getName() + "_box");
                box.group = module.getName() + "_setting";
                lastBox = box;
                modulePanel.children.add(box);
                modulePanel.repositionComponents();
                for (Value v : ValueManager.getValueByModName(module.getName())) {
                    if (!(v instanceof ModeValue)) continue;
                    ModeLabel l = new ModeLabel(modulePanel, 0, 0, ((ModeValue)v).getKey());
                    box.addChild(l);
                    ComboBox comboBox = new ComboBox((ModeValue)v, modulePanel, 0, 0);
                    comboBox.group = module.getName() + "_setting";
                    box.addChild(comboBox);
                }
                for (Value value : ValueManager.getValueByModName(module.getName())) {
                    if (value instanceof FloatValue) {
                        FloatValue floatValue = (FloatValue)value;
                        Slider slider = new Slider(floatValue, modulePanel, 0, 0, floatValue.getKey());
                        slider.group = module.getName() + "_setting";
                        box.addChild(slider);
                        continue;
                    }
                    if (value instanceof BooleanValue) {
                        BooleanValue booleanValue = (BooleanValue)value;
                        Checkbox checkbox = new Checkbox(booleanValue, modulePanel, 0, 0);
                        checkbox.group = module.getName() + "_setting";
                        box.addChild(checkbox);
                        continue;
                    }
                    if (!(value instanceof ColorValue)) continue;
                    ColorValue colorProperty = (ColorValue)value;
                    ColorPicker colorPicker = new ColorPicker(colorProperty, modulePanel, 0, 0, colorProperty.getKey());
                    box.addChild(colorPicker);
                }
                box.recalcHeight();
            }
            if (lastBox != null) {
                lastBox.setLastBox(true);
                lastBox.recalcHeight();
            }
            modulePanel.isEnabled = true;
            modulePanel.repositionComponents();
            this.panels.add(modulePanel);
        }
        Panel presetWindow = new Panel("Preset", 5, 5);
        presetWindow.children.add(new PresetList(presetWindow, 0, 0));
        presetWindow.isEnabled = true;
        presetWindow.repositionComponents();
        this.panels.add(presetWindow);
        Panel commonSettings = new Panel("Common", 5, 5);
        for (Value value : ValueManager.getValueByModName("Global")) {
            if (value instanceof FloatValue) {
                commonSettings.children.add(new Slider((FloatValue)value, commonSettings, -5, 0, value.getKey()));
            }
            if (value instanceof BooleanValue) {
                commonSettings.children.add(new Checkbox((BooleanValue)value, commonSettings, -5, 0, -3));
            }
            if (!(value instanceof ModeValue)) continue;
            commonSettings.children.add(new Label(commonSettings, 0, 0, value.getKey()));
            commonSettings.children.add(new ComboBox((ModeValue)value, commonSettings, -5, 0, -3));
        }
        commonSettings.isEnabled = true;
        commonSettings.repositionComponents();
        this.panels.add(commonSettings);
        this.rePositionWindows();
    }

    public void toggleBox(String group, boolean value) {
        for (Panel panel : this.panels) {
            for (Component child : panel.children) {
                if (!child.group.equals(group) || !(child instanceof Box)) continue;
                ((Box)child).setVirtual_visible(value);
            }
            panel.repositionComponents();
        }
    }

    public boolean isVisibleComponetsByGroup(String group) {
        for (Panel panel : this.panels) {
            for (Component child : panel.children) {
                if (!child.group.equals(group) || !(child instanceof Box) || !((Box)child).isVirtual_visible()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(startColor & 0xFF) / 255.0f;
        float f4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(endColor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0.0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, 0.0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, 0.0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private static /* synthetic */ Panel[] lambda$rePositionWindows$6(int x$0) {
        return new Panel[x$0];
    }
}

