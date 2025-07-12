package com.bitartisan1.playeresp.integration;

import com.bitartisan1.playeresp.gui.PlayerESPConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PlayerESPConfigScreen::new;
    }
}
