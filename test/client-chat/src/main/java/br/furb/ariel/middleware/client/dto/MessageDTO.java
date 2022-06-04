package br.furb.ariel.middleware.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO {

    private String id;
    private String answerId;
    private String route;
    private String from;
    private Map<String, Object> data;
    private Date ttl;

    public static Builder ok(String answerId) {
        return new Builder().ok(answerId);
    }

    public static Builder error(String answerId, String message) {
        return new Builder().error(answerId, message);
    }

    public static class Builder {

        private String id;
        private String answerId;
        private String route;
        private String from;
        private Map<String, Object> data;
        private Date ttl;

        private Builder() {

        }

        public MessageDTO build() {
            MessageDTO dto = new MessageDTO();
            dto.setId(this.id);
            dto.setAnswerId(this.answerId);
            dto.setRoute(this.route);
            dto.setFrom(this.from);
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

    }
}
