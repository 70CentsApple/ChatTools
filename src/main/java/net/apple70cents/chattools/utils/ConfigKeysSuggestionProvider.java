package net.apple70cents.chattools.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

//#if MC>=11900
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//#else
// Fabric v2 begins to work since 1.19
//$$ import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
//#endif

public class ConfigKeysSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    int level;

    public ConfigKeysSuggestionProvider(int level) {
        // level 1: only booleans
        // level 2: boolean, string, number, enums
        // level 3: anything except for FAQ and sub
        this.level = level;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        for (Map.Entry<String, String> ele : ConfigScreenUtils.getKey2TypeMappings().entrySet()) {
            if (level == 3) {
                switch (String.valueOf(ele.getValue())) {
                    case "FAQ":
                    case "sub":
                        continue;
                    default:
                        builder.suggest(ele.getKey());
                }
            } else if (level == 2) {
                switch (String.valueOf(ele.getValue())) {
                    case "FAQ":
                    case "sub":
                    case "keycode":
                    case "StringList":
                    case "FormatterList":
                    case "MacroList":
                    case "BubbleList":
                    case "ResponderList":
                    case "CustomJoinMessageList":
                        continue;
                    default:
                        builder.suggest(ele.getKey());
                }
            } else {
                switch (String.valueOf(ele.getValue())) {
                    case "boolean":
                        builder.suggest(ele.getKey());
                    default:
                        continue;
                }
            }
        }
        return builder.buildFuture();
    }
}
