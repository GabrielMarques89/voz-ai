package org.gmarques.model.enums;

public enum MediaAction {
    PLAY_PAUSE("play_pause"),
    NEXT_TRACK("proxima_musica"),
    MAX_VOLUME("volume_maximo"),
    INCREASE_VOLUME("aumentar_volume"),
    DECREASE_VOLUME("diminuir_volume");

    private final String action;

    MediaAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static MediaAction fromString(String action) {
        for (MediaAction ma : MediaAction.values()) {
            if (ma.getAction().equalsIgnoreCase(action)) {
                return ma;
            }
        }
        throw new IllegalArgumentException("Ação de mídia desconhecida: " + action);
    }
}
