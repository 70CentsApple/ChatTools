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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

//? if <26.2 {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}

//? if >=1.21.11 {
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
        Component text;
        long startTime;

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

        public String toString() {
            return "BubbleUnit{" + "text=" + text + ", startTime=" + startTime + '}';
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
            Component renderComponent = ConfigUtils.NICK_HIDER_ENABLED ? NickHider.work(text) : text;
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
            poseStack.mulPose(
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
            int maxLineWidth = ConfigUtils.getInt("bubble.MaxLineWidth");
            List<FormattedCharSequence> lines = font.split(renderComponent, maxLineWidth);
            int linesAmount = lines.size();

//? if >=1.21 {
            // draw background
            int maxWidth = 0;
            for (FormattedCharSequence line : lines) {
                maxWidth = Math.max(maxWidth, font.width(line));
            }
            float x1 = -maxWidth / 2.0F - 3;
            float y1 = -linesAmount * 9 - 3;
            float x2 = maxWidth / 2.0F + 3;
            float y2 = 1;

//? if >=1.21.11 {
            renderQueue.order(0).submitCustomGeometry(poseStack, RenderTypes.textBackgroundSeeThrough(), (pose1, buffer) -> {
//?} elif >=1.21.9 {
            /*renderQueue.order(0).submitCustomGeometry(poseStack, RenderType.textBackgroundSeeThrough(), (pose1, buffer) -> {
*///?} else {
            /*VertexConsumer buffer = multiBufferSource.getBuffer(RenderType.textBackgroundSeeThrough());
            Matrix4f pose1 = pose;
*///?}
                buffer.addVertex(pose1, x1, y1, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
                buffer.addVertex(pose1, x1, y2, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
                buffer.addVertex(pose1, x2, y2, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
                buffer.addVertex(pose1, x2, y1, -0.1F).setColor(0F, 0F, 0F, 0.18F).setUv2(15, 15);
//? if >=1.21.9 {
            });
//?}

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

    private static Map<String, BubbleUnit> bubbleMap = new HashMap<>();

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
        if (bubbleMap.isEmpty() || mc.level == null || entity == null) {
            return;
        }

        Component renderedEntityNameComponent = entity.hasCustomName() ? entity.getCustomName() : entity.getDisplayName();
        if (renderedEntityNameComponent == null) {
            return;
        }
        String washedEntityName = TextUtils.wash(renderedEntityNameComponent.getString());
        long maxBubbleLifetimeMs = ConfigUtils.getInt("bubble.Lifetime") * 1000L;

        // Iterate through the players in the current world to find which player's
        // chat bubble corresponds to this rendered entity.
        for (AbstractClientPlayer player : mc.level.players()) {
            String playerDisplayName = player.getDisplayName().getString();
            String playerProfileName = player.getGameProfile()
//? if >=1.21.10 {
                    .name();
//?} else {
                    /*.getName();
*///?}
            if (playerDisplayName == null) {
                continue;
            }

            // The key used to fetch the bubble from the bubbleMap.
            String matchedBubbleKey = null;

            // Try to match by the player's display name.
            if (bubbleMap.containsKey(playerDisplayName) && washedEntityName.equals(playerDisplayName)) {
                matchedBubbleKey = playerDisplayName;
            }
            // Try to match by the player's profile name or UUID
            if (bubbleMap.containsKey(playerProfileName) && (washedEntityName.equals(playerProfileName)
                    || entity.getUUID().equals(player.getUUID()))) {
                matchedBubbleKey = playerProfileName;
            }

            // If there is no matching bubble for this player, skip to the next player.
            if (matchedBubbleKey == null) {
                continue;
            }

            if (ConfigUtils.BUBBLE_HIDE_IF_INVISIBLE_ENABLED &&
                    mc.player != null && entity.isInvisibleTo(mc.player)) {
                continue;
            }

            // Check if the bubble's lifetime is over.
            if (bubbleMap.get(matchedBubbleKey).getLifetime() >= maxBubbleLifetimeMs) {
                bubbleMap.remove(matchedBubbleKey);
                continue;
            }

            // Now we can confirm that this entity corresponds to this player and the bubble is valid.
            // Check the rendering distance.
            double distanceToPlayerSqr = mc.getEntityRenderDispatcher().distanceToSqr(player);
            if (distanceToPlayerSqr <= 4096.0) { // Distance is within 64 blocks (64^2 = 4096)
                bubbleMap.get(matchedBubbleKey).render(entity, poseStack
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
                    bubbleMap.put(name, new BubbleUnit(messageContent, System.currentTimeMillis()));
                    found = true;
                }
            }
            if (fallback && !found) {
                String sender = MessageUtils.findTheFirstPlayerName(message, profile);
                if (sender == null) {
                    return;
                }
                bubbleMap.put(sender, new BubbleUnit(text, System.currentTimeMillis()));
            }
        }
    }
}



