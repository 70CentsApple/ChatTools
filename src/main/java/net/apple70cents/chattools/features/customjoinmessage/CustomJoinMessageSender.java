package net.apple70cents.chattools.features.customjoinmessage;

import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.RegExUtils;

import java.util.List;

public class CustomJoinMessageSender {
    public static void work(String currentAddress) {
        LoggerUtils.info("[ChatTools] Trying to send join message as session identifier: " + currentAddress);
        for (SpecialUnits.CustomJoinMessageRuleUnit unit : SpecialUnits.CustomJoinMessageRuleUnit.fromList(
                (List) ConfigUtils.get("customJoinMessage.List"))) {
            if ("*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address).matcher(currentAddress)
                    .matches()) {
                LoggerUtils.info(
                        "[ChatTools] Will send custom join message '" + unit.message + "' within " + unit.delayInMilliseconds + "ms");
                MessageUtils.sendToPublicChatScheduled(unit.message, unit.forceDisableFormatter,
                        unit.delayInMilliseconds);
            }
        }
    }
}
