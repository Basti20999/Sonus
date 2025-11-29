
package dev.minceraft.sonus.web.adapter.messages;

public class TextWebSocketMessage extends AbstractWebSocketMessage {

    private final String text;

    public TextWebSocketMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}