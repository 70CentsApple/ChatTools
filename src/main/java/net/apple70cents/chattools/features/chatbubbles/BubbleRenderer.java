package net.apple70cents.chattools.features.chatbubbles;

import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BubbleRenderer {
    static ModClothConfig config = ModClothConfig.get();
    static MinecraftClient mc = MinecraftClient.getInstance();
    static TextRenderer textRenderer = mc.textRenderer;

    protected static class BubbleUnit {
        Text text;
        long startTime;

        BubbleUnit(Text text, long startTime) {
            this.text = text;
            this.startTime = startTime;
        }

        /**
         * 获取存在时间（毫秒）
         * @return 气泡存在时间
         */
        public long getLifetime() {
            return System.currentTimeMillis() - startTime;
        }

        public String toString() {
            return "Lifetime:" + getLifetime() + ", Text:" + text;
        }

        /**
         * 渲染单个气泡
         * @param matrixStack 矩阵栈
         * @param partialTicks 平滑插值用参数（tickDelta）
         * @param camera 摄像机视角
         * @param sender 信息发送者（应该渲染到谁头上）
         */
        public void render(MatrixStack matrixStack, float partialTicks, Camera camera, PlayerEntity sender) {
            EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
            matrixStack.push();
            // 平滑插值
            double x = sender.prevX + (sender.getX() - sender.prevX) * partialTicks;
            double y = sender.prevY + (sender.getY() - sender.prevY) * partialTicks;
            double z = sender.prevZ + (sender.getZ() - sender.prevZ) * partialTicks;
            matrixStack.translate(x - renderDispatcher.camera.getPos().x,
                    y - renderDispatcher.camera.getPos().y + sender.getHeight() + config.chatBubblesYOffset/10.0F,
                    z - renderDispatcher.camera.getPos().z);
            VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
            Quaternion rotation = camera.getRotation().copy();
            rotation.scale(-1.0F);
            matrixStack.multiply(rotation);
            matrixStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();


            float xOffset = -textRenderer.getWidth(text) / 2.0F;
            textRenderer.draw(text, xOffset, 0, 553648127, false, matrix4f, immediate, true, 1056964608, 15728640);
            textRenderer.draw(text, xOffset, 0, -1, false, matrix4f, immediate, false, 0, 15728640);

            matrixStack.pop();
        }
    }

    private static Map<String, BubbleUnit> bubbleMap = new HashMap<>();

    /**
     * 负责渲染气泡的总逻辑
     * @param matrixStack 矩阵栈
     * @param partialTicks 平滑插值用参数（tickDelta）
     * @param camera 摄像机视角
     */
    public static void render(MatrixStack matrixStack, float partialTicks, Camera camera) {
        if (bubbleMap.isEmpty()) {
            return;
        }

        for (AbstractClientPlayerEntity sender : mc.world.getPlayers()) {
            String name = sender.getDisplayName().getString();
            if (!bubbleMap.containsKey(name)) {
                continue;
            } else if (bubbleMap.get(name).getLifetime() >= config.chatBubblesLifetime * 1000) {
                bubbleMap.remove(name);
                continue;
            }
            double d = mc.getEntityRenderDispatcher().getSquaredDistanceToCamera(sender);
            if (d <= 4096.0) {
                bubbleMap.get(name).render(matrixStack, partialTicks, camera, sender);
            }
        }
    }

    /**
     * 添加聊天气泡
     * @param text 消息
     */
    public static void addChatBubble(Text text) {
        String message = text.getString();
        String sender = findPlayerName(MinecraftClient.getInstance().world.getPlayers(), message);
        if (sender == null) {
            return;
        }
        bubbleMap.put(sender, new BubbleUnit(text, System.currentTimeMillis()));
    }

    /**
     * 获取消息中出现的第一个玩家名称
     * （大多数情况下是发送者）
     * @param playerNameList 玩家列表（通常是`MinecraftClient.getInstance().world.getPlayers()`）
     * @param inputString 消息
     * @return 玩家名称 或 null
     */
    public static String findPlayerName(List<AbstractClientPlayerEntity> playerNameList, String inputString) {
        int minIndex = inputString.length();
        String firstPlayerName = null;
        for (AbstractClientPlayerEntity player : playerNameList) {
            String playerName = player.getDisplayName().getString();
            String regex = "\\b" + Pattern.quote(playerName) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(inputString);

            if (matcher.find() && matcher.start() < minIndex) {
                minIndex = matcher.start();
                firstPlayerName = matcher.group();
            }
        }
        return firstPlayerName;
    }
}
