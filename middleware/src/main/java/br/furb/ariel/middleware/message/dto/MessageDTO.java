package br.furb.ariel.middleware.message.dto;

import br.furb.ariel.middleware.message.model.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@JsonInclude(Include.NON_EMPTY)
public class MessageDTO {

    private String id;
    private String answerId;
    private Map<String, Object> data;
    private Date ttl;

    public static Builder ok(String answerId) {
        return new Builder().ok(answerId);
    }

    public static Builder from(Message message) {
        return new Builder().from(message);
    }

    public static class Builder {

        private String id;
        private String answerId;
        private Map<String, Object> data;

        private Builder() {

        }

        public MessageDTO build() {
            MessageDTO dto = new MessageDTO();
            dto.setId(this.id);
            dto.setAnswerId(this.answerId);
            dto.setData(new HashMap<>(this.data));
            return dto;
        }

        public Builder ok(String answerId) {
            this.id = UUID.randomUUID().toString();
            this.answerId = answerId;
            this.data = new HashMap<>();
            this.data.put("status", "ok");
            return this;
        }
    }
}
