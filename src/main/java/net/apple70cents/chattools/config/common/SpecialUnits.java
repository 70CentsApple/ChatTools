package net.apple70cents.chattools.config.common;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpecialUnits {
    protected static <T> List<T> map(List raw, Function<Object, T> mapper) {
        return (List<T>) raw.stream().map(mapper).collect(Collectors.toList());
    }

    public enum ToastModes {
        ADDON, POWERSHELL, AWT, TWO_SLICES
    }

    public enum KeyModifiers {
        SHIFT, ALT, CTRL, NONE
    }

    public enum MacroModes {
        LAZY, GREEDY
    }

    public enum TranslatorModes {
        BUILTIN, BAIDU, MICROSOFT_FREE, GOOGLE_FREE
    }

    public static class BubbleRuleUnit {
        public String address;
        public String pattern;
        public boolean fallback;

        public BubbleRuleUnit() {
            this.address = "*";
            this.pattern = "<(?<name>.*?)> (?<message>.*)";
            this.fallback = false;
        }

        public BubbleRuleUnit(String address, String pattern, boolean fallback) {
            this.address = address;
            this.pattern = pattern;
            this.fallback = fallback;
        }

        public static BubbleRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                String address = (String) ((Map) ele).getOrDefault("address", "*");
                String pattern = (String) ((Map) ele).getOrDefault("pattern", "<(?<name>.*?)> (?<message>.*)");
                boolean fallback = (boolean) ((Map) ele).getOrDefault("fallback", false);
                return new BubbleRuleUnit(address, pattern, fallback);
            } else if (ele instanceof BubbleRuleUnit) {
                return (BubbleRuleUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<BubbleRuleUnit> fromList(List list) {
            return SpecialUnits.map(list, BubbleRuleUnit::of);
        }

        @Override
        public String toString() {
            return "BubbleRuleUnit{address='" + address + "', pattern='" + pattern + "', fallback=" + fallback + '}';
        }
    }

    public static class ResponderRuleUnit {
        public String address;
        public String pattern;
        public String message;
        public long minDelayInMilliseconds;
        public long maxDelayInMilliseconds;
        public boolean forceDisableFormatter;

        public ResponderRuleUnit() {
            this.address = "*";
            this.pattern = "Repeat my words:(?<word>.*)";
            this.message = "You said {word}.";
            this.minDelayInMilliseconds = 50;
            this.maxDelayInMilliseconds = 50;
            this.forceDisableFormatter = false;
        }

        public ResponderRuleUnit(String address, String pattern, String message, long minDelayInMilliseconds,
                 long maxDelayInMilliseconds, boolean forceDisableFormatter) {
            this.address = address;
            this.pattern = pattern;
            this.message = message;
            this.minDelayInMilliseconds = minDelayInMilliseconds;
            this.maxDelayInMilliseconds = maxDelayInMilliseconds;
            this.forceDisableFormatter = forceDisableFormatter;
        }

        public static ResponderRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                String address = (String) ((Map) ele).getOrDefault("address", "*");
                String pattern = (String) ((Map) ele).getOrDefault("pattern", "Repeat my words:(?<word>.*)");
                String message = (String) ((Map) ele).getOrDefault("message", "You said {word}.");
                long minDelayInMilliseconds = ((Number) ((Map) ele).getOrDefault("minDelayInMilliseconds", 50)).longValue();
                long maxDelayInMilliseconds = ((Number) ((Map) ele).getOrDefault("maxDelayInMilliseconds", minDelayInMilliseconds)).longValue();
                boolean forceDisableFormatter = (boolean) ((Map) ele).getOrDefault("forceDisableFormatter", false);
                return new ResponderRuleUnit(address, pattern, message, minDelayInMilliseconds, maxDelayInMilliseconds, forceDisableFormatter);
            } else if (ele instanceof ResponderRuleUnit) {
                return (ResponderRuleUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<ResponderRuleUnit> fromList(List list) {
            return SpecialUnits.map(list, ResponderRuleUnit::of);
        }

        @Override
        public String toString() {
            return "ResponderRuleUnit{address='" + address + "', pattern='" + pattern + "', message='" + message
                    + "', minDelayInMilliseconds=" + minDelayInMilliseconds + ", maxDelayInMilliseconds=" + maxDelayInMilliseconds
                    + ", forceDisableFormatter=" + forceDisableFormatter + '}';
        }
    }

    public static class MacroCommandEntry {
        public String command;
        public long delayInMilliseconds;
        public boolean forceDisableFormatter;

        public MacroCommandEntry() {
            this.command = "";
            this.delayInMilliseconds = 0;
            this.forceDisableFormatter = false;
        }

        public MacroCommandEntry(String command, long delayInMilliseconds, boolean forceDisableFormatter) {
            this.command = command;
            this.delayInMilliseconds = delayInMilliseconds;
            this.forceDisableFormatter = forceDisableFormatter;
        }

        public static MacroCommandEntry of(Object ele) {
            if (ele instanceof Map) {
                String command = (String) ((Map) ele).getOrDefault("command", "");
                long delayInMilliseconds = ((Number) ((Map) ele).getOrDefault("delayInMilliseconds", 0)).longValue();
                boolean forceDisableFormatter = (boolean) ((Map) ele).getOrDefault("forceDisableFormatter", false);
                return new MacroCommandEntry(command, delayInMilliseconds, forceDisableFormatter);
            } else if (ele instanceof MacroCommandEntry) {
                return (MacroCommandEntry) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<MacroCommandEntry> fromList(List list) {
            return SpecialUnits.map(list, MacroCommandEntry::of);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MacroCommandEntry that = (MacroCommandEntry) o;
            return delayInMilliseconds == that.delayInMilliseconds && command.equals(that.command)
                    && forceDisableFormatter == that.forceDisableFormatter;
        }

        @Override
        public int hashCode() {
            return Objects.hash(command, delayInMilliseconds, forceDisableFormatter);
        }

        @Override
        public String toString() {
            return "MacroCommandEntry{command='" + command + "', delayInMilliseconds=" + delayInMilliseconds
                    + ", forceDisableFormatter=" + forceDisableFormatter + '}';
        }
    }

    public static class MacroUnit {
        public String key;
        public KeyModifiers modifier;
        public MacroModes mode;
        public List<MacroCommandEntry> commands;

        public MacroUnit() {
            this.key = InputConstants.UNKNOWN.getName();
            this.modifier = KeyModifiers.NONE;
            this.mode = MacroModes.LAZY;
            this.commands = new ArrayList<>();
            this.commands.add(new MacroCommandEntry());
        }

        public MacroUnit(String key, KeyModifiers modifier, MacroModes mode, List<MacroCommandEntry> commands) {
            this.key = key;
            this.modifier = modifier;
            this.mode = mode;
            this.commands = commands;
        }

        public static MacroUnit of(Object ele) {
            if (ele instanceof Map) {
                String key = (String) ((Map) ele).getOrDefault("key", InputConstants.UNKNOWN.getName());
                KeyModifiers modifier = KeyModifiers
                        .valueOf((String) ((Map) ele).getOrDefault("modifier", KeyModifiers.NONE));
                MacroModes mode = MacroModes.valueOf((String) ((Map) ele).getOrDefault("mode", MacroModes.LAZY));
                List<MacroCommandEntry> commands = MacroCommandEntry
                        .fromList((List) ((Map) ele).getOrDefault("commands", new ArrayList<>()));
                return new MacroUnit(key, modifier, mode, commands);
            } else if (ele instanceof MacroUnit) {
                return (MacroUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<MacroUnit> fromList(List list) {
            return SpecialUnits.map(list, MacroUnit::of);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MacroUnit macroUnit = (MacroUnit) o;
            return key.equals(macroUnit.key) && modifier == macroUnit.modifier && mode == macroUnit.mode
                    && commands.equals(macroUnit.commands);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, modifier, mode, commands);
        }

        @Override
        public String toString() {
            return "MacroUnit{key='" + key + "', modifier=" + modifier + ", mode=" + mode + ", commands=" + commands
                    + '}';
        }
    }

    public static class FormatterUnit {
        public String address;
        public String formatter;

        public FormatterUnit() {
            this.address = "*";
            this.formatter = "{text}";
        }

        public FormatterUnit(String address, String formatter) {
            this.address = address;
            this.formatter = formatter;
        }

        public static FormatterUnit of(Object ele) {
            if (ele instanceof Map) {
                String address = (String) ((Map) ele).getOrDefault("address", "*");
                String formatter = (String) ((Map) ele).getOrDefault("formatter", "{text}");
                return new FormatterUnit(address, formatter);
            } else if (ele instanceof FormatterUnit) {
                return (FormatterUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object:" + ele);
            }
        }

        public static List<FormatterUnit> fromList(List list) {
            return SpecialUnits.map(list, FormatterUnit::of);
        }

        @Override
        public String toString() {
            return "FormatterUnit{address='" + address + "', formatter='" + formatter + "'}";
        }
    }

    public static class CustomJoinMessageRuleUnit {
        public String address;
        public String message;
        public long delayInMilliseconds;
        public boolean forceDisableFormatter;

        public CustomJoinMessageRuleUnit() {
            this.address = "*";
            this.message = "/login xxx";
            this.delayInMilliseconds = 1000;
            this.forceDisableFormatter = false;
        }

        public CustomJoinMessageRuleUnit(String address, String message, long delayInMilliseconds,
                boolean forceDisableFormatter) {
            this.address = address;
            this.message = message;
            this.delayInMilliseconds = delayInMilliseconds;
            this.forceDisableFormatter = forceDisableFormatter;
        }

        public static CustomJoinMessageRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                String address = (String) ((Map) ele).getOrDefault("address", "*");
                String message = (String) ((Map) ele).getOrDefault("message", "/login xxx");
                long delayInMilliseconds = ((Number) ((Map) ele).getOrDefault("delayInMilliseconds", 1000)).longValue();
                boolean forceDisableFormatter = (boolean) ((Map) ele).getOrDefault("forceDisableFormatter", false);
                return new CustomJoinMessageRuleUnit(address, message, delayInMilliseconds, forceDisableFormatter);
            } else if (ele instanceof CustomJoinMessageRuleUnit) {
                return (CustomJoinMessageRuleUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<CustomJoinMessageRuleUnit> fromList(List list) {
            return SpecialUnits.map(list, CustomJoinMessageRuleUnit::of);
        }

        @Override
        public String toString() {
            return "CustomJoinMessageRuleUnit{address='" + address + "', message='" + message
                    + "', delayInMilliseconds=" + delayInMilliseconds + ", forceDisableFormatter="
                    + forceDisableFormatter + '}';
        }
    }

    public static class NotifierRuleUnit {
        public String address;
        public String pattern;
        public boolean toast;
        public boolean sound;
        public boolean actionbar;
        public boolean highlight;

        public NotifierRuleUnit() {
            this.address = "*";
            this.pattern = "";
            this.toast = true;
            this.sound = true;
            this.actionbar = true;
            this.highlight = true;
        }

        public NotifierRuleUnit(String address, String pattern, boolean toast, boolean sound, boolean actionbar,
                boolean highlight) {
            this.address = address;
            this.pattern = pattern;
            this.toast = toast;
            this.sound = sound;
            this.actionbar = actionbar;
            this.highlight = highlight;
        }

        public static NotifierRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                String address = (String) ((Map) ele).getOrDefault("address", "*");
                String pattern = (String) ((Map) ele).getOrDefault("pattern", "");
                boolean toast = (boolean) ((Map) ele).getOrDefault("toast", true);
                boolean sound = (boolean) ((Map) ele).getOrDefault("sound", true);
                boolean actionbar = (boolean) ((Map) ele).getOrDefault("actionbar", true);
                boolean highlight = (boolean) ((Map) ele).getOrDefault("highlight", true);
                return new NotifierRuleUnit(address, pattern, toast, sound, actionbar, highlight);
            } else if (ele instanceof NotifierRuleUnit) {
                return (NotifierRuleUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<NotifierRuleUnit> fromList(List list) {
            return SpecialUnits.map(list, NotifierRuleUnit::of);
        }

        @Override
        public String toString() {
            return "NotifierRuleUnit{address='" + address + "', pattern='" + pattern + "', toast=" + toast + ", sound="
                    + sound + ", actionbar=" + actionbar + ", highlight=" + highlight + '}';
        }
    }
}
