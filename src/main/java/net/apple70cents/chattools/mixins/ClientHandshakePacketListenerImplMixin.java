package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.features.customjoinmessage.CustomJoinMessageSender;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if <1.19 {
/*import net.minecraft.client.Minecraft;
*///?}
//? if >=1.21.2 {
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
//?} else {
/*import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
*///?}


/**
 * @author 70CentsApple
 */
@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class ClientHandshakePacketListenerImplMixin {
//? if >=1.19 {
    @Shadow
    @Final
    private ServerData serverData;
//?}

//? if >=1.21.2 {
    @Inject(method = "handleLoginFinished", at = @At(value = "TAIL"))
    public void onServerLoginSuccess(ClientboundLoginFinishedPacket packet, CallbackInfo ci) {
//?} else {
    /*@Inject(method = "handleGameProfile", at = @At(value = "TAIL"))
    public void onServerLoginSuccess(ClientboundGameProfilePacket packet, CallbackInfo ci) {
*///?}

        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (!(boolean) ConfigUtils.get("customJoinMessage.Enabled")) {
            return;
        }
//? if >=1.19 {
        if (this.serverData != null) {
            CustomJoinMessageSender.work(this.serverData.ip);
        } else {
            CustomJoinMessageSender.work(ContextUtils.getSessionIdentifier());
        }
//?} else {
        /*if (Minecraft.getInstance().getCurrentServer() != null) {CustomJoinMessageSender.work(ContextUtils.getSessionIdentifier());}
*///?}
    }

}
