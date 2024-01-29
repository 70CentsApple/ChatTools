package net.apple70cents.chattools.config;

import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpecialUnits {
    public enum ToastModes {
        ADDON, POWERSHELL, AWT
    }

    public enum KeyModifiers {
        SHIFT, ALT, CTRL, NONE
    }

    public enum MacroModes {
        LAZY, GREEDY
    }

    public static class BubbleRuleUnit {
        public String address;
        public String pattern;
        public boolean fallback;

        public BubbleRuleUnit() {
            this.address = "*";
            this.pattern = "<(?<name>.*)> (?<message>.*)";
            this.fallback = false;
        }

        public BubbleRuleUnit(String address, String pattern, boolean fallback) {
            this.address = address;
            this.pattern = pattern;
            this.fallback = fallback;
        }

        public static BubbleRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                return new BubbleRuleUnit((String) ((Map) ele).get("address"), (String) ((Map) ele).get("pattern"),
                        (boolean) ((Map) ele).get("fallback"));
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
    }

    public static class ResponserRuleUnit {
        public String address;
        public String pattern;
        public String message;
        public boolean forceDisableFormatter;

        public ResponserRuleUnit() {
            this.address = "*";
            this.pattern = "Repeat my words:(?<word>.*)";
            this.message = "You said {word}.";
            this.forceDisableFormatter = false;
        }

        public ResponserRuleUnit(String address, String pattern, String message, boolean forceDisableFormatter) {
            this.address = address;
            this.pattern = pattern;
            this.message = message;
            this.forceDisableFormatter = forceDisableFormatter;
        }

        public static ResponserRuleUnit of(Object ele) {
            if (ele instanceof Map) {
                return new ResponserRuleUnit((String) ((Map) ele).get("address"), (String) ((Map) ele).get("pattern"),
                        (String) ((Map) ele).get("message"), (boolean) ((Map) ele).get("forceDisableFormatter"));
            } else if (ele instanceof ResponserRuleUnit) {
                return (ResponserRuleUnit) ele;
            } else {
                throw new IllegalArgumentException("Unexpected element type of Object: " + ele);
            }
        }

        public static List<ResponserRuleUnit> fromList(List list) {
            List<ResponserRuleUnit> arr = new ArrayList<>();
            for (Object ele : list) {
                arr.add(ResponserRuleUnit.of(ele));
            }
            return arr;
        }
    }

    public static class MacroUnit {
        public String key;
        public KeyModifiers modifier;
        public MacroModes mode;
        public String command;

        public MacroUnit() {
            this.key = InputUtil.UNKNOWN_KEY.getTranslationKey();
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
                return new MacroUnit((String) ((Map) ele).get("key"), KeyModifiers.valueOf((String) ((Map) ele).get("modifier")),
                        MacroModes.valueOf((String) ((Map) ele).get("mode")), (String) ((Map) ele).get("command"));
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
                return new FormatterUnit((String) ((Map) ele).get("address"), (String) ((Map) ele).get("formatter"));
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
    }

}

