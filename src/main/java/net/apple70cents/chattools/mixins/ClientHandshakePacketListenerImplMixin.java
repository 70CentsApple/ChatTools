package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.customjoinmessage.CustomJoinMessageSender;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC<11900
//$$ import net.minecraft.client.Minecraft;
//#endif

/**
 * @author 70CentsApple
 */
@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class ClientHandshakePacketListenerImplMixin {
//#if MC>=11900
    @Shadow
    @Final
    private ServerData serverData;
//#endif

    @Inject(method = "handleLoginFinished", at = @At(value = "TAIL"))
    public void onServerLoginSuccess(ClientboundLoginFinishedPacket clientboundLoginFinishedPacket, CallbackInfo ci) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!(boolean) ConfigUtils.get("customJoinMessage.Enabled")) {
            return;
        }
//#if MC>=11900
        if (this.serverData != null) {
            CustomJoinMessageSender.work(this.serverData.ip);
        } else {
            CustomJoinMessageSender.work(ContextUtils.getSessionIdentifier());
        }
//#else
//$$    if (Minecraft.getInstance().getCurrentServer() != null) {CustomJoinMessageSender.work(ContextUtils.getSessionIdentifier());}
//#endif
    }

}
