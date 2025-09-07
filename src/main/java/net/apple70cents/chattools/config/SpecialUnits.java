package net.apple70cents.chattools.config;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpecialUnits {
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
        BUILTIN, BAIDU
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
            List<BubbleRuleUnit> arr = new ArrayList<>();
            for (Object ele : list) {
                arr.add(BubbleRuleUnit.of(ele));
            }
            return arr;
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
        public long delayInMilliseconds;
        public boolean forceDisableFormatter;

        public ResponderRuleUnit() {
            this.address = "*";
            this.pattern = "Repeat my words:(?<word>.*)";
            this.message = "You said {word}.";
            this.delayInMilliseconds = 50;
            this.forceDisableFormatter = false;
        }

        public ResponderRuleUnit(String address, String pattern, String message, long delayInMilliseconds, boolean forceDisableFormatter) {
            this.address = address;
            this.pattern = pattern;
            this.message = message;
            this.delayInMilliseconds = delayInMilliseconds;
            this.forceDisableFormatter = forceDisableFormatter;
        }

        public static ResponderRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                String address = (String) ((Map) ele).getOrDefault("address", "*");
                String pattern = (String) ((Map) ele).getOrDefault("pattern", "Repeat my words:(?<word>.*)");
                String message = (String) ((Map) ele).getOrDefault("message", "You said {word}.");
                long delayInMilliseconds = ((Number) ((Map) ele).getOrDefault("delayInMilliseconds", 50)).longValue();
                boolean forceDisableFormatter = (boolean) ((Map) ele).getOrDefault("forceDisableFormatter", false);
                return new ResponderRuleUnit(address, pattern, message, delayInMilliseconds, forceDisableFormatter);
            } else if (ele instanceof ResponderRuleUnit) {
                return (ResponderRuleUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<ResponderRuleUnit> fromList(List list) {
            List<ResponderRuleUnit> arr = new ArrayList<>();
            for (Object ele : list) {
                arr.add(ResponderRuleUnit.of(ele));
            }
            return arr;
        }

        @Override
        public String toString() {
            return "ResponderRuleUnit{address='" + address + "', pattern='" + pattern + "', message='" + message + "', delayInMilliseconds=" + delayInMilliseconds + ", forceDisableFormatter=" + forceDisableFormatter + '}';
        }
    }

    public static class MacroUnit {
        public String key;
        public KeyModifiers modifier;
        public MacroModes mode;
        public String command;

        public MacroUnit() {
            this.key = InputConstants.UNKNOWN.getName();
            this.modifier = KeyModifiers.NONE;
            this.mode = MacroModes.LAZY;
            this.command = "";
        }

        public MacroUnit(String key, KeyModifiers modifier, MacroModes mode, String command) {
            this.key = key;
            this.modifier = modifier;
            this.mode = mode;
            this.command = command;
        }

        public static MacroUnit of(Object ele) {
            if (ele instanceof Map) {
                String key = (String) ((Map) ele).getOrDefault("key", InputConstants.UNKNOWN.getName());
                KeyModifiers modifier = KeyModifiers.valueOf((String) ((Map) ele).getOrDefault("modifier", KeyModifiers.NONE));
                MacroModes mode = MacroModes.valueOf((String) ((Map) ele).getOrDefault("mode", MacroModes.LAZY));
                String command = (String) ((Map) ele).getOrDefault("command", "");
                return new MacroUnit(key, modifier, mode, command);
            } else if (ele instanceof MacroUnit) {
                return (MacroUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<MacroUnit> fromList(List list) {
            List<MacroUnit> arr = new ArrayList<>();
            for (Object ele : list) {
                arr.add(MacroUnit.of(ele));
            }
            return arr;
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
            return key.equals(macroUnit.key) && modifier == macroUnit.modifier && mode == macroUnit.mode && command.equals(macroUnit.command);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, modifier, mode, command);
        }

        @Override
        public String toString() {
            return "MacroUnit{key='" + key + "', modifier=" + modifier + ", mode=" + mode + ", command='" + command + "'}";
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
            List<FormatterUnit> arr = new ArrayList<>();
            for (Object ele : list) {
                arr.add(FormatterUnit.of(ele));
            }
            return arr;
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

        public CustomJoinMessageRuleUnit(String address, String message, long delayInMilliseconds, boolean forceDisableFormatter) {
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
            List<CustomJoinMessageRuleUnit> arr = new ArrayList<>();
            for (Object ele : list) {
                arr.add(CustomJoinMessageRuleUnit.of(ele));
            }
            return arr;
        }

        @Override
        public String toString() {
            return "CustomJoinMessageRuleUnit{address='" + address + "', message='" + message + "', delayInMilliseconds=" + delayInMilliseconds + ", forceDisableFormatter=" + forceDisableFormatter + '}';
        }
    }
}

