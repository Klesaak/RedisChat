package ua.klesaak.vaultchat.manager;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageData {
    ChatType chatType;
    String sender, receiver, message;
}
