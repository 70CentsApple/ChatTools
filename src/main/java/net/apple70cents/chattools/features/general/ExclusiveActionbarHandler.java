package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? } elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

public class ExclusiveActionbarHandler {
    protected static class ExclusiveActionbarMessageUnit {
        Component text;
        long startTime;
        long lifeTimeInMillis;

        ExclusiveActionbarMessageUnit(Component text, long startTime, long lifeTimeInMillis) {
            this.text = text;
            this.startTime = startTime;
            this.lifeTimeInMillis = lifeTimeInMillis;
        }

        ExclusiveActionbarMessageUnit(Component text, long lifeTimeInMillis) {
            this(text, System.currentTimeMillis(), lifeTimeInMillis);
        }
    }

    private static List<ExclusiveActionbarMessageUnit> messageUnitList = new CopyOnWriteArrayList<>();

    public static void addToRenderQueue(Component message, long lifeTimeInMillis) {
        messageUnitList.add(new ExclusiveActionbarMessageUnit(message, lifeTimeInMillis));
    }

    public static void tick() {
        long currentTime = System.currentTimeMillis();
        messageUnitList.removeIf(unit -> (currentTime - unit.startTime) >= unit.lifeTimeInMillis);
    }

//? if >=26.1 {
    public static void render(GuiGraphicsExtractor context) {
//?} elif >=1.20 {
//    public static void render(GuiGraphics context) {
//?} else {
        /*public static void render(PoseStack pose) {
*///?}
        Font font = Minecraft.getInstance().font;

        float baseSize = ((Number) ConfigUtils.get("general.ExclusiveActionbar.Size")).floatValue();
        int baseXOffset = ((Number) ConfigUtils.get("general.ExclusiveActionbar.XOffset")).intValue();
        int baseYOffset = ((Number) ConfigUtils.get("general.ExclusiveActionbar.YOffset")).intValue();

        long currentTime = System.currentTimeMillis();
        int index = 0;
        for (ExclusiveActionbarMessageUnit ele : messageUnitList) {
            long elapsedTime = currentTime - ele.startTime;
            float progress = (float) elapsedTime / ele.lifeTimeInMillis;
            if (progress < 0 || progress > 1) continue;

            int opacity = Mth.clamp(calculateSmoothOpacity(elapsedTime, ele.lifeTimeInMillis), 0, 255);
            if (opacity <= 2) continue;

            float entryProgress = Math.min(1.0F, (float) elapsedTime / 400.0F);
            float bounceOffset = easeOutBack(entryProgress) * 12.0F;

            final float finalX = baseXOffset;
            final float finalY = baseYOffset + index * 12.0F + 12.0F - bounceOffset;
            final float scale = baseSize * (0.7F + 0.3F * easeOutQuart(entryProgress));
            final int color = opacity << 24 | 0xFFFFFF;

//? if >=26.1 {
            context.pose().pushMatrix();
            context.pose().translate(context.guiWidth() / 2.0F, context.guiHeight() - 68.0F - 4.0F);
            // we translate the whole stack instead of drawing at specific position, so we can use floats for better precision.
            context.pose().translate(finalX, finalY);
            context.pose().scale(scale, scale);
            context.centeredText(font, ele.text, 0, 0, color);
            context.pose().popMatrix();
//?} elif >=1.21.6 {
            /*context.pose().pushMatrix();
            context.pose().translate(context.guiWidth() / 2.0F, context.guiHeight() - 68.0F - 4.0F);
            // we translate the whole stack instead of drawing at specific position, so we can use floats for better precision.
            context.pose().translate(finalX, finalY);
            context.pose().scale(scale, scale);
            context.drawCenteredString(font, ele.text, 0, 0, color);
            context.pose().popMatrix();
*///?} elif >=1.20 {
            /*context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2.0F, context.guiHeight() - 68.0F - 4.0F, 0.0F);
            context.pose().translate(finalX, finalY, 0.0F);
            context.pose().scale(scale, scale, 1);
            context.drawCenteredString(font, ele.text, 0, 0, color);
            context.pose().popPose();
*///?} else {
            /*pose.pushPose();
            pose.translate(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2.0F, Minecraft
                    .getInstance().getWindow().getGuiScaledHeight() - 68.0F - 4.0F, 0.0F);
            pose.translate(finalX, finalY, 0.0F);
            pose.scale(scale, scale, 1);
            int textWidth = font.width(ele.text);
            font.drawShadow(pose, ele.text, (-textWidth / 2.0F), 0, color);
            pose.popPose();
*///?}
            index++;
        }
    }

    private static int calculateSmoothOpacity(long elapsedTime, long lifeTime) {
        if (elapsedTime < 300) { // fade in
            return (int) (easeOutQuart(elapsedTime / 300.0F) * 255);
        } else if (elapsedTime > lifeTime - 500) { // fade out
            return (int) ((1.0F - (elapsedTime - (lifeTime - 500)) / 500.0F) * 255);
        }
        return 255;
    }

    private static float easeOutBack(float x) {
        float c1 = 2.7F;
        float c3 = c1 + 1;
        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }

    private static float easeOutQuart(float x) {
        return (float) (1 - Math.pow(1 - x, 4));
    }
}
