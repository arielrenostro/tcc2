package br.furb.ariel.middleware.message;

import br.furb.ariel.middleware.message.model.Message;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MessageService {

    public List<Message> getPendingMessagesByClient(String clientId) {
        // TODO
        return null;
    }
}
