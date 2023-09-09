package net.apple70cents.chattools.features.chatresponser;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatResponser {
    public static class ResponserRuleUnit {
        private String address;
        private String pattern;

        private String message;
        private boolean forceDisableInjector;


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

        public String getMessage() {
            return message;
        }

        public void setMessage(String string) {
            this.message = string;
        }

        public boolean isForceDisableInjector() {
            return forceDisableInjector;
        }

        public void setForceDisableInjector(boolean forceDisableInjector) {
            this.forceDisableInjector = forceDisableInjector;
        }

        public ResponserRuleUnit() {
            this.address = "*";
            this.pattern = "Repeat my words:(?<word>.*)";
            this.message = "You said {word}.";
            this.forceDisableInjector = false;
        }

        public ResponserRuleUnit(String address, String pattern, String message, boolean forceDisableInjector) {
            this.address = address;
            this.pattern = pattern;
            this.message = message;
            this.forceDisableInjector = forceDisableInjector;
        }
    }

    static ModClothConfig config = ModClothConfig.get();
    static MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * 把`message`中的{GROUP}格式的组名替换成`rawMessageReceived`中的对应分组
     *
     * @param rawMessageReceived 收到的原始信息
     * @param rawPattern         原始模式串
     * @param message            待发消息
     * @return 处理后的待发消息
     */
    static String replaceAllGroupNames(String rawMessageReceived, String rawPattern, String message) {
        // 这个正则表达式能取出花括号中的组名。即能取出`{name}`中的`name`；`{\}\{}`中的`\}\{`；但不能取出`{name\}`或者`\{name}`。
        final String groupNamePattern = "(?<!\\\\)\\{(?<group>.*?)(?<!\\\\)}";
        Matcher matcher = Pattern.compile(groupNamePattern).matcher(message);
        Matcher rawMessageMatcher = Pattern.compile(rawPattern).matcher(rawMessageReceived);
        while (matcher.find()) {
            String groupName = matcher.group("group");
            if (rawMessageMatcher.find()) {
                String context = rawMessageMatcher.group(groupName);
                if (context != null && !context.isBlank()) { // 捕获到的组内容非空
                    message = message.replace("{" + groupName + "}", context);
                }
            }
        }
        return message;
    }

    public static void work(String messageReceived) {
        messageReceived = ChatTools.wash_message(messageReceived);
        boolean canResponse = false;
        String pattern = "";
        String message = "";
        boolean forceDisableInjector = false;
        for (var unit : config.responserRuleList) {
            if (mc.getCurrentServerEntry() == null) {
                if ("*".equals(unit.getAddress())) {
                    if (Pattern.compile(unit.getPattern()).matcher(messageReceived).matches()) {
                        canResponse = true;
                        pattern = unit.getPattern();
                        message = unit.getMessage();
                        forceDisableInjector = unit.isForceDisableInjector();
                        break;
                    }
                }
            } else if ("*".equals(unit.getAddress()) || Pattern.compile(unit.getAddress()).matcher(mc.getCurrentServerEntry().address).matches()) {
                if (Pattern.compile(unit.getPattern()).matcher(messageReceived).matches()) {
                    canResponse = true;
                    pattern = unit.getPattern();
                    message = unit.getMessage();
                    forceDisableInjector = unit.isForceDisableInjector();
                    break;
                }
            }
        }
        if (canResponse) {
            message = replaceAllGroupNames(messageReceived, pattern, message);
            if (mc.player != null) {
                message = message.replace("{pos}", String.format("(%d,%d,%d)", (int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ()));
            }
            ChatTools.LOGGER.info("[ChatTools] Respond to `" + pattern + "`, with message `" + message + "`");
            message = message.replace("\\{", "{").replace("\\}", "}");
            ChatTools.sendPlayerChat(message, forceDisableInjector);
        }
    }
}
