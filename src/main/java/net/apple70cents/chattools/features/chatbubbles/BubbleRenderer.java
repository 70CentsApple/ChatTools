package net.apple70cents.chattools.features.chatbubbles;

import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.apple70cents.chattools.ChatTools.replaceText;

public class BubbleRenderer {
    public static class BubbleRuleUnit {
        private String address;
        private String pattern;
        private boolean fallback;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isFallback() {
            return fallback;
        }

        public void setFallback(boolean fallback) {
            this.fallback = fallback;
        }

        public BubbleRuleUnit() {
            this.address = "*";
            this.pattern = "<(?<name>.*)> (?<message>.*)";
            this.fallback = false;
        }
    }

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

        BubbleUnit(String str, long startTime) {
            this.text = Text.of(str);
            this.startTime = startTime;
        }

        /**
         * 获取存在时间（毫秒）
         *
         * @return 气泡存在时间
         */
        public long getLifetime() {
            return System.currentTimeMillis() - startTime;
        }

        public String toString() {
            return "[\"lifetime\": " + getLifetime() + ", \"text\": " + text + "]";
        }

        /**
         * 渲染单个气泡
         *
         * @param entity          玩家（用来获取身高用）（趴下时会改变名牌高度）
         * @param matrixStack     矩阵栈
         * @param vertexConsumers 顶点（？
         */
        public void render(Entity entity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers) {
            if (mc.player == null) {
                return;
            }
            Text renderText = text;
            if (config.nickHiderSettings.nickHiderEnabled) {
                renderText = replaceText((MutableText) renderText, mc.player.getDisplayName().getString(), config.nickHiderSettings.nickHiderText.replace('&', '§').replace("\\§", "&"));
            }
            int yOffset = "deadmau5".equals(text.getString()) ? -10 : 0; // 保留Minecraft原本的致敬彩蛋，这样才知道我玩的是MC
            EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();
            matrixStack.push();
            matrixStack.translate(0.0F, entity.getNameLabelHeight() + config.chatBubblesYOffset / 10.0F, 0.0F);
            matrixStack.multiply(renderDispatcher.getRotation());
            matrixStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            float xOffset = -textRenderer.getWidth(renderText) / 2.0F;
            textRenderer.draw(renderText, xOffset, yOffset, 553648127, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 1056964608, 15728640);
            textRenderer.draw(renderText, xOffset, yOffset, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728640);
            matrixStack.pop();
        }
    }

    private static final Map<String, BubbleUnit> bubbleMap = new HashMap<>();

    /**
     * 负责渲染气泡的总逻辑
     *
     * @param entity   玩家
     * @param matrices 矩阵栈
     * @param vertex   vertex
     */
    public static void render(Entity entity, MatrixStack matrices, VertexConsumerProvider vertex) {
        if (bubbleMap.isEmpty()) {
            return;
        } else if (mc.world == null) {
            return;
        }
        for (AbstractClientPlayerEntity sender : mc.world.getPlayers()) {
            String name = sender.getDisplayName().getString();
            if (!bubbleMap.containsKey(name)) {
                continue;
            } else if (!entity.getDisplayName().getString().equals(name)) {
                continue;// 不是渲染对象
            } else if (bubbleMap.get(name).getLifetime() >= config.chatBubblesLifetime * 1000) {
                bubbleMap.remove(name); // 超时被移除
                continue;
            }
            double d = mc.getEntityRenderDispatcher().getSquaredDistanceToCamera(sender);
            if (d <= 4096.0) {
                bubbleMap.get(name).render(entity, matrices, vertex);
            }
        }
    }

    /**
     * 添加聊天气泡
     *
     * @param text 消息
     */
    public static void addChatBubble(Text text) {
        String message = text.getString();
        if (mc.world == null) {
            return;
        }
        String pattern = "";
        boolean serverAddressPass = false;
        boolean fallback = false;
        for (BubbleRuleUnit unit : config.bubbleRuleList) {
            if (mc.getCurrentServerEntry() == null) {
                if ("*".equals(unit.getAddress())) {
                    serverAddressPass = true;
                    pattern = unit.getPattern();
                    fallback = unit.isFallback();
                    break;
                }
            } else if ("*".equals(unit.getAddress()) || Pattern.compile(unit.getAddress()).matcher(mc.getCurrentServerEntry().address).matches()) {
                serverAddressPass = true;
                pattern = unit.getPattern();
                fallback = unit.isFallback();
                break;
            }
        }
        if (serverAddressPass && !pattern.isEmpty()) {
            Matcher matcher = Pattern.compile(pattern).matcher(message);
            if (matcher.find()) {
                String name = matcher.group("name");
                String messageContext = matcher.group("message");
                bubbleMap.put(name, new BubbleUnit(messageContext, System.currentTimeMillis()));
            } else if (fallback) {
                String sender = findPlayerName(mc.world.getPlayers(), message);
                if (sender == null) {
                    return;
                }
                bubbleMap.put(sender, new BubbleUnit(text, System.currentTimeMillis()));
            }
        }
    }

    /**
     * 获取消息中出现的第一个玩家名称
     * （大多数情况下是发送者）
     *
     * @param playerNameList 玩家列表（通常是<code>MinecraftClient.getInstance().world.getPlayers()</code>）
     * @param inputString    消息
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
