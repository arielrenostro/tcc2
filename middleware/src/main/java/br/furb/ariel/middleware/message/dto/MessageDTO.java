package br.furb.ariel.middleware.message.dto;

import br.furb.ariel.middleware.message.model.Message;
import br.furb.ariel.middleware.service.dto.ServiceNotificationDTO;
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
    private String route;
    private Map<String, Object> data;
    private Date ttl;

    public static Builder ok(String answerId) {
        return new Builder().ok(answerId);
    }

    public static Builder error(String answerId, String message) {
        return new Builder().error(answerId, message);
    }

    public static Builder from(Message message) {
        return new Builder().from(message);
    }

    public static Builder from(ServiceNotificationDTO dto) {
        return new Builder().from(dto);
    }

    public static class Builder {

        private String id;
        private String answerId;
        private Map<String, Object> data;
        private Date ttl;

        private Builder() {

        }

        public MessageDTO build() {
            MessageDTO dto = new MessageDTO();
            dto.setId(this.id);
            dto.setAnswerId(this.answerId);
            dto.setData(new HashMap<>(this.data));
            dto.setTtl(this.ttl);
            return dto;
        }

        public Builder ok(String answerId) {
            this.id = UUID.randomUUID().toString();
            this.answerId = answerId;
            this.data = new HashMap<>();
            this.data.put("status", "OK");
            return this;
        }

        public Builder error(String answerId, String message) {
            this.id = UUID.randomUUID().toString();
            this.answerId = answerId;
            this.data = new HashMap<>();
            this.data.put("status", "ERROR");
            this.data.put("message", message);
            return this;
        }

        public Builder from(Message message) {
            this.id = message.getId();
            this.answerId = message.getAnswerId();
            this.data = new HashMap<>(message.getData());
            return this;
        }

        public Builder from(ServiceNotificationDTO dto) {
            this.id = dto.getId();
            this.answerId = dto.getAnswerId();
            this.data = new HashMap<>(dto.getData());
            this.ttl = dto.getTtl();
            return this;
        }
    }
}
