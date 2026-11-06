package io.mopesbox.Utils;

public class Emoji {
    private String placeholder;
    private String emoji;

    public Emoji(String placeholder, String emoji) {
        this.placeholder = placeholder;
        this.emoji = emoji;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getEmoji() {
        return emoji;
    }
}
