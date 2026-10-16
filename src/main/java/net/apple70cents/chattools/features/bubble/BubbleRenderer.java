package net.apple70cents.chattools.features.bubble;

import com.mojang.blaze3d.vertex.PoseStack;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.features.general.NickHider;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

//? if >=26.3 {
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
//?}

//? if <26.2 {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}

//? if >=26.3 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
//?} elif >=1.21.11 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.rendertype.RenderTypes;
//?} elif >=1.21.9 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.RenderType;
*///?} elif >=1.21 {
/*import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
*///?}

//? if >=1.19 {
import org.joml.Matrix4f;
//?} else {
/*import com.mojang.math.Matrix4f;
*///?}

/**
 * @author 70CentsApple
 */
public class BubbleRenderer {

    protected static class BubbleUnit {
        final Component text;
        final long startTime;
        Component cachedRenderComponent;
        List<FormattedCharSequence> cachedLines;
        int cachedMaxWidth = -1;
        int cachedLinesAmount = 0;
        int lastMaxLineWidth = -1;
        boolean lastNickHiderEnabled = false;

        BubbleUnit(Component text, long startTime) {
            this.text = text;
            this.startTime = startTime;
        }

        BubbleUnit(String str, long startTime) {
            this.text = TextUtils.of(str);
            this.startTime = startTime;
        }

        /**
         * get lifetime in milliseconds
         *
         * @return lifetime
         */
        public long getLifetime() {
            return System.currentTimeMillis() - startTime;
        }

        public boolean isExpired(long maxLifetimeMs) {
            return getLifetime() >= maxLifetimeMs;
        }

        public String toString() {
            return "BubbleUnit{" + "text=" + text + ", startTime=" + startTime + '}';
        }

        private void ensureLayout(Font font) {
            boolean nickHiderEnabled = ConfigUtils.NICK_HIDER_ENABLED;
            int maxLineWidth = ConfigUtils.getInt("bubble.MaxLineWidth");
            if (cachedLines == null || maxLineWidth != lastMaxLineWidth
                    || nickHiderEnabled != lastNickHiderEnabled) {
                lastMaxLineWidth = maxLineWidth;
                lastNickHiderEnabled = nickHiderEnabled;
                cachedRenderComponent = nickHiderEnabled ? NickHider.work(text) : text;
                cachedLines = font.split(cachedRenderComponent, maxLineWidth);
                cachedLinesAmount = cachedLines.size();
                int width = 0;
                for (FormattedCharSequence line : cachedLines) {
                    width = Math.max(width, font.width(line));
                }
                cachedMaxWidth = width;
            }
        }

        public void render(Entity entity, PoseStack poseStack
//? if <26.2 {
                /*, MultiBufferSource multiBufferSource
*///?}
                , float tickDelta
//? if >=1.21.9 {
                , SubmitNodeCollector renderQueue
//?}
        ) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            if (mc.player == null) {
                return;
            }
            ensureLayout(font);
            List<FormattedCharSequence> lines = cachedLines;
            int linesAmount = cachedLinesAmount;
            int maxWidth = cachedMaxWidth;
            int yOffset = ConfigUtils.getInt("bubble.YOffset");

            poseStack.pushPose();

//? if >=1.21 {
            Vec3 vec3d = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot());
            if (vec3d != null) {
                poseStack.translate(vec3d.x, vec3d.y + 0.5F + yOffset / 10.0F, vec3d.z);
            }
//?} else {
            /*poseStack.translate(0.0F, entity.getBbHeight() + 0.5F + yOffset / 10.0F, 0.0F);
*///?}
//? if >=26.3 {
            poseStack.rotate(
//?} else {
            /*poseStack.mulPose(
 *///?}
//? if >=26.2 {
                    mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.orientation
//?} elif >=26.1 {
                    /*mc.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState.orientation
*///?} elif >=1.21.9 {
                    /*mc.gameRenderer.getLevelRenderState().cameraRenderState.orientation
*///?} else {
                    /*mc.getEntityRenderDispatcher().cameraOrientation()
*///?}
            );
            poseStack.scale(
//? if >=1.21 {
                    0.025F
//?} else {
                    /*-0.025F
*///?}
                    , -0.025F, 0.025F);
            Matrix4f pose = poseStack.last().pose();

//? if >=1.21 {
            // draw background
            float x1 = -maxWidth / 2.0F - 3;
            float y1 = -linesAmount * 9 - 3;
            float x2 = maxWidth / 2.0F + 3;
            float y2 = 1;

//? if >=26.3 {
            ((SubmitNodeCollection)renderQueue.order(0)).solid.submit(
                    new TextFeatureRenderer.Submit(
                            new Matrix4f(poseStack.last().pose()),
                            Font.DisplayMode.SEE_THROUGH,
                            0xF000F0,
                            new TextFeatureRenderer.Content.StandaloneBackground(
                                    x1, y1, x2, y2,
                                    ARGB.color(0.18F, 0xFF000000)
                            )
                    )
            );
//?} else {
/*//? if >=1.21.11 {
            renderQueue.order(0).submitCustomGeometry(poseStack, RenderTypes.textBackgroundSeeThrough(), (pose1, buffer) -> {
//?} elif >=1.21.9 {
            /^renderQueue.order(0).submitCustomGeometry(poseStack, RenderType.textBackgroundSeeThrough(), (pose1, buffer) -> {
^///?} else {
            /^VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.textBackgroundSeeThrough());
            Matrix4f pose1 = pose;
^///?}
                buffer.addVertex(pose1, x1, y1, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
                buffer.addVertex(pose1, x1, y2, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
                buffer.addVertex(pose1, x2, y2, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
                buffer.addVertex(pose1, x2, y1, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
//? if >=1.21.9 {
            });
//?}
*///?}

//?}

            // draw text
            for (int i = 0; i < linesAmount; i++) {
                int y = 9 * (i - linesAmount);
                FormattedCharSequence line = lines.get(i);
                float xOffset = -font.width(line) / 2.0F;

                // draw text background for versions 1.16 ~ 1.20.6
//? if >=1.21 {
//?} elif >=1.19 {
                /*font.drawInBatch(line, xOffset, y, 0xFFFFFFFF, false, pose, multiBufferSource, Font.DisplayMode.SEE_THROUGH, 0x3F000000, 0xF000F0);
*///?} else {
                /*font.drawInBatch(line, xOffset, y, 0xFFFFFFFF, false, pose, multiBufferSource, true, 0x3F000000, 0xF000F0);
*///?}

//? if >=1.21.9 {
                renderQueue.order(1).submitText(poseStack, xOffset, y, line, false, Font.DisplayMode.NORMAL, 0xF000F0, 0xFFFFFFFF, 0, 0);
//?} elif >=1.19 {
                /*font.drawInBatch(line, xOffset, y, 0xFFFFFFFF, false, pose, multiBufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
*///?} else {
                /*font.drawInBatch(line, xOffset, y, 0xFFFFFFFF, false, pose, multiBufferSource, false, 0, 0xF000F0);
*///?}


            }

            poseStack.popPose();
        }
    }

    private static final Map<java.util.UUID, BubbleUnit> bubbleByUuid = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, BubbleUnit> pendingByName = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clear() {
        bubbleByUuid.clear();
        pendingByName.clear();
    }

    public static void render(Entity entity, PoseStack poseStack
//? if <26.2 {
            /*, MultiBufferSource multiBufferSource
*///?}
            , float tickDelta
//? if >=1.21.9 {
            , SubmitNodeCollector submitNodeCollector
//?}
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == null || mc.level == null || mc.player == null) {
            return;
        }
        if (bubbleByUuid.isEmpty() && pendingByName.isEmpty()) {
            return;
        }

        java.util.UUID entityUuid = entity.getUUID();
        long maxBubbleLifetimeMs = ConfigUtils.getInt("bubble.Lifetime") * 1000L;
        BubbleUnit unit = bubbleByUuid.get(entityUuid);

        // if UUID cache missed (have pending names to deal with)
        if (unit == null && !pendingByName.isEmpty()) {
            Component renderedEntityNameComponent = entity.hasCustomName() ? entity.getCustomName() : entity.getDisplayName();
            if (renderedEntityNameComponent != null) {
                String washedEntityName = TextUtils.wash(renderedEntityNameComponent.getString());
                unit = pendingByName.get(washedEntityName);
                if (unit == null && entity instanceof AbstractClientPlayer) {
                    String playerProfileName = ((AbstractClientPlayer) entity).getGameProfile()
//? if >=1.21.10 {
                            .name();
//?} else {
                            /*.getName();
*///?}
                    if (playerProfileName != null) {
                        unit = pendingByName.get(playerProfileName);
                    }
                }
                if (unit != null) {
                    if (unit.isExpired(maxBubbleLifetimeMs)) {
                        pendingByName.values().remove(unit);
                        unit = null;
                    } else {
                        bubbleByUuid.put(entityUuid, unit);
                    }
                }
            }
        }

        if (unit == null) {
            return;
        }

        // Check lifecycle
        if (unit.isExpired(maxBubbleLifetimeMs)) {
            bubbleByUuid.remove(entityUuid);
            pendingByName.values().remove(unit);
            return;
        }

        if (ConfigUtils.BUBBLE_HIDE_IF_INVISIBLE_ENABLED && entity.isInvisibleTo(mc.player)) {
            return;
        }

        // Check the rendering distance
        double distanceSqr = mc.getEntityRenderDispatcher().distanceToSqr(entity);
        if (distanceSqr <= 4096.0) {
            unit.render(entity, poseStack
//? if <26.2 {
                    /*, multiBufferSource
*///?}
                    , tickDelta
//? if >=1.21.9 {
                    , submitNodeCollector
//?}
            );
        }
    }

    public static void addChatBubble(Component text) {
        String message = TextUtils.wash(text.getString());
        if (Minecraft.getInstance().level == null) {
            return;
        }
        String pattern = "";
        boolean serverAddressPass = false;
        boolean partial = false;
        boolean profile = false;
        boolean fallback = false;
        for (SpecialUnits.BubbleRuleUnit unit : SpecialUnits.BubbleRuleUnit.fromList(
                (List) ConfigUtils.get("bubble.List"))) {
            if ("*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                    .matcher(ContextUtils.getSessionIdentifier()).matches()) {
                serverAddressPass = true;
                pattern = unit.pattern;
                partial = unit.partial;
                profile = unit.profile;
                fallback = unit.fallback;
                break;
            }
        }

        boolean found = false;
        if (serverAddressPass && !pattern.isEmpty()) {
            Matcher matcher = RegExUtils.getOrCompilePattern(pattern).matcher(message);
            if (matcher.find()) {
                String nameContainer = matcher.group("name");
                String name = partial ? MessageUtils.findTheFirstPlayerName(nameContainer, profile) : nameContainer;

                if (name != null) {
                    String messageContent = matcher.group("message");
                    pendingByName.put(name, new BubbleUnit(messageContent, System.currentTimeMillis()));
                    found = true;
                }
            }
            if (fallback && !found) {
                String sender = MessageUtils.findTheFirstPlayerName(message, profile);
                if (sender == null) {
                    return;
                }
                pendingByName.put(sender, new BubbleUnit(text, System.currentTimeMillis()));
            }
        }
    }
}



